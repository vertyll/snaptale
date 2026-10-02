package com.vertyll.snaptale.security;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;

import com.vertyll.snaptale.IntegrationTest;
import com.vertyll.snaptale.TestUsers;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class SecurityRulesIT {

    private static final String KEYCLOAK = "http://localhost:8180/realms/snaptale/protocol/openid-connect";

    @Autowired
    MockMvc mvc;

    @Autowired
    ClientRegistrationRepository registrations;

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
    }

    @Test
    void writesNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/auth/logout")).andExpect(status().isForbidden());
    }

    @Test
    void signInRedirectsToKeycloakWithPkceInTheChosenLanguage() throws Exception {
        mvc.perform(get("/oauth2/authorization/keycloak").cookie(new Cookie("lang", "en")))
            .andExpect(status().isFound())
            .andExpect(
                header().string(
                    "Location",
                    allOf(
                        startsWith(KEYCLOAK + "/auth?"),
                        containsString("client_id=snaptale-backend"),
                        containsString("code_challenge_method=S256"),
                        containsString("ui_locales=en"),
                        containsString("redirect_uri=http://localhost:3000/login/oauth2/code/keycloak")
                    )
                )
            );
    }

    @Test
    void unknownLanguageFallsBackToPolish() throws Exception {
        mvc.perform(get("/oauth2/authorization/keycloak").cookie(new Cookie("lang", "xx")))
            .andExpect(header().string("Location", containsString("ui_locales=pl")));
    }

    @Test
    void signOutHandsBackTheKeycloakLogoutUrl() throws Exception {
        mvc.perform(
            post("/api/auth/logout").with(csrf())
                .with(
                    oidcLogin().clientRegistration(registrations.findByRegistrationId("keycloak"))
                        .oidcUser(TestUsers.principal(1, "subject", "ktos@example.com"))
                )
        )
            .andExpect(status().isOk())
            .andExpect(
                jsonPath(
                    "$.logoutUrl",
                    allOf(
                        startsWith(KEYCLOAK + "/logout?"),
                        containsString("id_token_hint=test-id-token"),
                        containsString("post_logout_redirect_uri=http://localhost:3000/")
                    )
                )
            );
    }

    @Test
    void unknownPathsAreDenied() throws Exception {
        mvc.perform(get("/index.php")).andExpect(status().isUnauthorized());
        mvc.perform(get("/index.php").with(oidcLogin().oidcUser(TestUsers.principal(1, "subject", "ktos@example.com"))))
            .andExpect(status().isForbidden());
    }
}
