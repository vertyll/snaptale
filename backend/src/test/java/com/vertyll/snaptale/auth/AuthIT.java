package com.vertyll.snaptale.auth;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.vertyll.snaptale.IntegrationTest;
import com.vertyll.snaptale.RecordingMailSender;
import com.vertyll.snaptale.common.MessageKeys;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class AuthIT {

    private static final String PASSWORD = "sekretne-haslo";

    @Autowired
    MockMvc mvc;

    @Autowired
    RecordingMailSender mail;

    @Test
    void registrationSignsInAndSendsAVerificationLinkThatVerifiesTheEmail() throws Exception {
        String email = uniqueEmail();
        MockHttpSession session = new MockHttpSession();

        mvc.perform(json(post("/api/auth/register"), register("Ala", email)).session(session))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session))
            .andExpect(jsonPath("$.user.email").value(email))
            .andExpect(jsonPath("$.user.emailVerified").value(false));

        assertThat(mail.sentTo(email)).singleElement().satisfies(sent -> {
            assertThat(sent.subject()).contains("Potwierdź");
            assertThat(sent.body()).contains("http://localhost:3000/verify-email?token=");
        });
        mvc.perform(json(post("/api/auth/email/verify"), token(mail.lastTokenFor(email))))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    void verificationTokenWorksOnlyOnce() throws Exception {
        String email = uniqueEmail();
        mvc.perform(json(post("/api/auth/register"), register("Ola", email))).andExpect(status().isNoContent());
        String token = mail.lastTokenFor(email);

        mvc.perform(json(post("/api/auth/email/verify"), token(token))).andExpect(status().isNoContent());
        mvc.perform(json(post("/api/auth/email/verify"), token(token)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(MessageKeys.AUTH_TOKEN_INVALID));
    }

    @Test
    void emailMustBeUniqueRegardlessOfCase() throws Exception {
        String email = uniqueEmail();
        mvc.perform(json(post("/api/auth/register"), register("Ela", email))).andExpect(status().isNoContent());

        mvc.perform(json(post("/api/auth/register"), register("Ela", email.toUpperCase(java.util.Locale.ROOT))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(MessageKeys.AUTH_EMAIL_TAKEN));
    }

    @Test
    void registrationValidatesTheForm() throws Exception {
        mvc.perform(
            json(post("/api/auth/register"), "{\"name\":\"\",\"email\":\"nie-email\",\"password\":\"krotkie\"}")
        )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(MessageKeys.INVALID_FORM))
            .andExpect(jsonPath("$.errors.name.code").value(MessageKeys.REQUIRED))
            .andExpect(jsonPath("$.errors.email.code").value(MessageKeys.EMAIL_INVALID))
            .andExpect(jsonPath("$.errors.password.code").value(MessageKeys.TOO_SHORT))
            .andExpect(jsonPath("$.errors.password.args.min").value(8));
    }

    @Test
    void loginAcceptsTheRightPasswordOnlyAndLocksOutAfterRepeatedFailures() throws Exception {
        String email = uniqueEmail();
        mvc.perform(json(post("/api/auth/register"), register("Iza", email))).andExpect(status().isNoContent());

        MockHttpSession session = new MockHttpSession();
        mvc.perform(json(post("/api/auth/login"), login(email, PASSWORD)).session(session))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.user.email").value(email));

        for (int attempt = 0; attempt < 5; attempt++) {
            mvc.perform(json(post("/api/auth/login"), login(email, "zle-haslo-123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(MessageKeys.AUTH_BAD_CREDENTIALS));
        }
        mvc.perform(json(post("/api/auth/login"), login(email, PASSWORD)))
            .andExpect(status().isTooManyRequests())
            .andExpect(jsonPath("$.code").value(MessageKeys.AUTH_TOO_MANY_ATTEMPTS))
            .andExpect(jsonPath("$.args.minutes").isNumber());
    }

    @Test
    void passwordResetLinkSetsANewPassword() throws Exception {
        String email = uniqueEmail();
        mvc.perform(json(post("/api/auth/register"), register("Ewa", email))).andExpect(status().isNoContent());

        mvc.perform(json(post("/api/auth/password/forgot"), "{\"email\":\"%s\"}".formatted(email)))
            .andExpect(status().isAccepted());
        String token = mail.lastTokenFor(email);
        mvc.perform(
            json(
                post("/api/auth/password/reset"),
                "{\"token\":\"%s\",\"password\":\"nowe-haslo-456\"}".formatted(token)
            )
        ).andExpect(status().isNoContent());

        mvc.perform(json(post("/api/auth/login"), login(email, PASSWORD))).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/api/auth/login"), login(email, "nowe-haslo-456"))).andExpect(status().isNoContent());
    }

    @Test
    void forgotPasswordDoesNotRevealWhetherAnAccountExists() throws Exception {
        String email = uniqueEmail();

        mvc.perform(json(post("/api/auth/password/forgot"), "{\"email\":\"%s\"}".formatted(email)))
            .andExpect(status().isAccepted());
        assertThat(mail.sentTo(email)).isEmpty();
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        mvc.perform(json(post("/api/auth/register"), register("Jan", uniqueEmail())).session(session))
            .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/logout").with(csrf()).session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/me").session(session)).andExpect(jsonPath("$.user").isEmpty());
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static String register(String name, String email) {
        return "{\"name\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}".formatted(name, email, PASSWORD);
    }

    private static String login(String email, String password) {
        return "{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password);
    }

    private static String token(String token) {
        return "{\"token\":\"%s\"}".formatted(token);
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
