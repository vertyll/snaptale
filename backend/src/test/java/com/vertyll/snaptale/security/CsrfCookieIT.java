package com.vertyll.snaptale.security;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(MySqlTestContainer.class)
class CsrfCookieIT {

    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @LocalServerPort
    int port;

    private final CookieManager cookies = new CookieManager();
    private final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).build();

    @Test
    void meIssuesCsrfCookieThatUnlocksWrites() throws IOException, InterruptedException {
        assertThat(send(HttpRequest.newBuilder(uri("/api/me")).GET()).statusCode()).isEqualTo(200);
        String token = csrfCookie();

        assertThat(
            send(HttpRequest.newBuilder(uri("/api/auth/logout")).POST(HttpRequest.BodyPublishers.noBody())).statusCode()
        ).isEqualTo(403);
        assertThat(
            send(
                HttpRequest.newBuilder(uri("/api/auth/logout"))
                    .header(CSRF_HEADER, token)
                    .POST(HttpRequest.BodyPublishers.noBody())
            ).statusCode()
        ).isEqualTo(204);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String csrfCookie() {
        return cookies.getCookieStore()
            .getCookies()
            .stream()
            .filter(cookie -> CSRF_COOKIE.equals(cookie.getName()))
            .map(HttpCookie::getValue)
            .findFirst()
            .orElseThrow();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
