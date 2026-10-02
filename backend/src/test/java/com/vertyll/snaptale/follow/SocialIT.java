package com.vertyll.snaptale.follow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.vertyll.snaptale.IntegrationTest;
import com.vertyll.snaptale.TestUsers;
import com.vertyll.snaptale.TestUsers.TestUser;
import com.vertyll.snaptale.common.MessageKeys;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.vertyll.snaptale.TestUsers.as;

@IntegrationTest
class SocialIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    TestUsers users;

    @Test
    void followingShowsOnTheProfileAndInTheFollowingList() throws Exception {
        TestUser star = users.create("Gwiazda");
        TestUser fan = users.create("Fan");

        mvc.perform(put("/api/users/%d/follow".formatted(star.id())).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(put("/api/users/%d/follow".formatted(star.id())).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/users/" + star.id()).with(as(fan)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user.name").value("Gwiazda"))
            .andExpect(jsonPath("$.follows.followers").value(1))
            .andExpect(jsonPath("$.followedByMe").value(true))
            .andExpect(jsonPath("$.posts").isArray());
        mvc.perform(get("/api/users/" + star.id())).andExpect(jsonPath("$.followedByMe").value(false));
        mvc.perform(get("/api/me/following").with(as(fan))).andExpect(jsonPath("$[0].id").value(star.id()));

        mvc.perform(delete("/api/users/%d/follow".formatted(star.id())).with(as(fan)).with(csrf()))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/users/" + star.id())).andExpect(jsonPath("$.follows.followers").value(0));
    }

    @Test
    void nobodyFollowsThemselvesOrAMissingUser() throws Exception {
        TestUser user = users.create("Samotnik");

        mvc.perform(put("/api/users/%d/follow".formatted(user.id())).with(as(user)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(MessageKeys.FOLLOW_SELF));
        mvc.perform(put("/api/users/999999999/follow").with(as(user)).with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(MessageKeys.USER_NOT_FOUND));
    }

    @Test
    void suggestionsNeverIncludeTheViewer() throws Exception {
        TestUser viewer = users.create("Widz");
        users.create("Inny");

        mvc.perform(get("/api/users/suggested").with(as(viewer)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", not(hasItem((int) viewer.id()))));
    }
}
