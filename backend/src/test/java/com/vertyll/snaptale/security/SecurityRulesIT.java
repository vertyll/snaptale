package com.vertyll.snaptale.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.vertyll.snaptale.IntegrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class SecurityRulesIT {

    @Autowired
    MockMvc mvc;

    @Test
    void anonymousVisitorsReadButDoNotWrite() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isOk()).andExpect(jsonPath("$.user").isEmpty());
        mvc.perform(get("/api/posts")).andExpect(status().isOk());
        mvc.perform(get("/api/users/suggested")).andExpect(status().isOk());
        mvc.perform(get("/api/messages"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$['errors.status.401']").isString());
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());

        mvc.perform(get("/api/me/following")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/me").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/posts/1/like").with(csrf())).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/email/resend").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void writesNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void unknownPathsAreDenied() throws Exception {
        mvc.perform(get("/index.php")).andExpect(status().isUnauthorized());
        mvc.perform(get("/index.php").with(user(new AuthenticatedUser(1, "ktos@example.com", null))))
            .andExpect(status().isForbidden());
    }
}
