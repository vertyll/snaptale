package com.vertyll.snaptale.security;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.vertyll.snaptale.MySqlTestContainer;
import com.vertyll.snaptale.RedisTestContainer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The session cookie as the running server sets it. Only a real server applies the
 * {@code server.servlet.session.cookie}
 * settings to Spring Session's cookie, so this is no MockMvc test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(
    {
        MySqlTestContainer.class,
        RedisTestContainer.class
    }
)
class SessionCookieIT {

    @LocalServerPort
    int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void aSignInStartsASessionWhoseCookieOnlyThisHostOverTlsAndNoScriptSees() throws IOException, InterruptedException {
        HttpResponse<Void> started = client.send(
            HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/oauth2/authorization/keycloak")).build(),
            HttpResponse.BodyHandlers.discarding()
        );

        assertThat(started.statusCode()).isEqualTo(302);
        assertThat(started.headers().allValues("Set-Cookie")).anySatisfy(
            cookie -> assertThat(cookie).startsWith("__Host-SNAPTALE_SESSION=")
                .contains("; Path=/;", "; Secure", "; HttpOnly", "; SameSite=Lax")
                .doesNotContain("Domain=")
        );
    }
}
