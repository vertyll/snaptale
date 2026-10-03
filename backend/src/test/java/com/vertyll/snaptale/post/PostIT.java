package com.vertyll.snaptale.post;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.vertyll.snaptale.IntegrationTest;
import com.vertyll.snaptale.TestMedia;
import com.vertyll.snaptale.TestUsers;
import com.vertyll.snaptale.TestUsers.TestUser;
import com.vertyll.snaptale.common.MessageKeys;

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.vertyll.snaptale.TestUsers.as;

@IntegrationTest
class PostIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestUsers users;

    @Value("${application.media.directory}")
    Path mediaDirectory;

    @Test
    void newPostAppearsInTheFeedWithItsVideoServedFromMedia() throws Exception {
        TestUser author = users.create("Autor");
        long postId = createPost(author, "Pierwszy film");

        String videoUrl = JsonPath.read(content(get("/api/posts/" + postId)), "$.post.videoUrl");
        assertThat(videoUrl).startsWith("/media/videos/").endsWith(".mp4");
        mvc.perform(get(videoUrl)).andExpect(status().isOk());
        mvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.posts[?(@.id == %d)].author.name".formatted(postId)).value("Autor"))
            .andExpect(jsonPath("$.hasMore").isBoolean());
    }

    @Test
    void likesAndCommentsAreCountedPerViewer() throws Exception {
        TestUser author = users.create("Autor");
        TestUser fan = users.create("Fan");
        long postId = createPost(author, "Do polubienia");

        mvc.perform(put("/api/posts/%d/like".formatted(postId)).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(put("/api/posts/%d/like".formatted(postId)).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(
            post("/api/posts/%d/comments".formatted(postId)).with(as(fan))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Świetne!\"}")
        ).andExpect(status().isCreated()).andExpect(jsonPath("$.author.name").value("Fan"));

        mvc.perform(get("/api/posts/" + postId).with(as(fan)))
            .andExpect(jsonPath("$.post.likeCount").value(1))
            .andExpect(jsonPath("$.post.likedByMe").value(true))
            .andExpect(jsonPath("$.post.commentCount").value(1))
            .andExpect(jsonPath("$.comments[0].text").value("Świetne!"))
            .andExpect(jsonPath("$.authorPostIds[0]").value(postId));
        mvc.perform(get("/api/posts/" + postId)).andExpect(jsonPath("$.post.likedByMe").value(false));

        mvc.perform(delete("/api/posts/%d/like".formatted(postId)).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/posts/" + postId)).andExpect(jsonPath("$.post.likeCount").value(0));
    }

    @Test
    void onlyTheAuthorDeletesAPostAndItsVideoGoesWithIt() throws Exception {
        TestUser author = users.create("Autor");
        TestUser stranger = users.create("Obcy");
        long postId = createPost(author, "Do usunięcia");
        String videoUrl = JsonPath.read(content(get("/api/posts/" + postId)), "$.post.videoUrl");
        Path video = mediaDirectory.resolve(videoUrl.substring("/media/".length()));
        assertThat(video).exists();

        mvc.perform(delete("/api/posts/" + postId).with(as(stranger)).with(csrf()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(MessageKeys.POST_NOT_OWNER));
        mvc.perform(delete("/api/posts/" + postId).with(as(author)).with(csrf())).andExpect(status().isNoContent());

        mvc.perform(get("/api/posts/" + postId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(MessageKeys.POST_NOT_FOUND));
        assertThat(Files.exists(video)).isFalse();
    }

    @Test
    void commentsAreRemovedByTheirAuthorOrThePostAuthorOnly() throws Exception {
        TestUser author = users.create("Autor");
        TestUser commenter = users.create("Komentujący");
        TestUser stranger = users.create("Obcy");
        long postId = createPost(author, "Post");
        long first = comment(postId, commenter);
        long second = comment(postId, commenter);

        mvc.perform(delete("/api/posts/%d/comments/%d".formatted(postId, first)).with(as(stranger)).with(csrf()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value(MessageKeys.COMMENT_NOT_OWNER));
        mvc.perform(delete("/api/posts/%d/comments/%d".formatted(postId, first)).with(as(commenter)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(delete("/api/posts/%d/comments/%d".formatted(postId, second)).with(as(author)).with(csrf()))
            .andExpect(status().isNoContent());
    }

    @Test
    void uploadRejectsAMissingTextOrSomethingThatIsNotAnMp4() throws Exception {
        TestUser author = users.create("Autor");

        mvc.perform(
            multipart("/api/posts").file(TestMedia.notAVideo("video"))
                .param("text", "Opis")
                .with(as(author))
                .with(csrf())
        ).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(MessageKeys.MEDIA_VIDEO_UNSUPPORTED));
        mvc.perform(multipart("/api/posts").param("text", "Opis").with(as(author)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(MessageKeys.MEDIA_VIDEO_REQUIRED));
        mvc.perform(
            multipart("/api/posts").file(TestMedia.video("video")).param("text", " ").with(as(author)).with(csrf())
        ).andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.text.code").value(MessageKeys.REQUIRED));
    }

    private long createPost(TestUser author, String text) {
        return id(
            created(
                multipart("/api/posts").file(TestMedia.video("video")).param("text", text).with(as(author)).with(csrf())
            )
        );
    }

    private long comment(long postId, TestUser author) {
        return id(
            created(
                post("/api/posts/%d/comments".formatted(postId)).with(as(author))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"text\":\"Komentarz\"}")
            )
        );
    }

    private String created(RequestBuilder request) {
        MvcTestResult result = MockMvcTester.create(mvc).perform(request);
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private String content(RequestBuilder request) {
        MvcTestResult result = MockMvcTester.create(mvc).perform(request);
        assertThat(result).hasStatusOk();
        return new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
    }

    private static long id(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }
}
