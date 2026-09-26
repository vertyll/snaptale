package com.vertyll.snaptale.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.vertyll.snaptale.IntegrationTest;
import com.vertyll.snaptale.TestMedia;
import com.vertyll.snaptale.TestUsers;
import com.vertyll.snaptale.common.MessageKeys;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.vertyll.snaptale.TestUsers.as;

@IntegrationTest
class MyAccountIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestUsers users;

    @Test
    void profileUpdateTrimsAndClearsAnEmptyBio() throws Exception {
        Account user = users.create("Stare imię");

        mvc.perform(
            patch("/api/me").with(as(user))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"  Nowe imię  \",\"bio\":\"   \"}")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Nowe imię"))
            .andExpect(jsonPath("$.bio").isEmpty());
    }

    @Test
    void avatarIsCroppedAndServed() throws Exception {
        Account user = users.create("Awatar");

        mvc.perform(
            multipart(HttpMethod.PUT, "/api/me/avatar").file(TestMedia.png("image", 400, 300))
                .param("x", "50")
                .param("y", "0")
                .param("width", "300")
                .param("height", "300")
                .with(as(user))
                .with(csrf())
        ).andExpect(status().isOk()).andExpect(jsonPath("$.avatarUrl", startsWith("/media/avatars/")));
        mvc.perform(get("/api/users/" + user.id()))
            .andExpect(jsonPath("$.user.avatarUrl", startsWith("/media/avatars/")));
    }

    @Test
    void cropOutsideTheImageIsRejected() throws Exception {
        Account user = users.create("Awatar");

        mvc.perform(
            multipart(HttpMethod.PUT, "/api/me/avatar").file(TestMedia.png("image", 100, 100))
                .param("x", "50")
                .param("y", "50")
                .param("width", "100")
                .param("height", "100")
                .with(as(user))
                .with(csrf())
        ).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(MessageKeys.MEDIA_CROP_INVALID));
    }
}
