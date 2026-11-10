package com.vertyll.snaptale.security;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.vertyll.snaptale.MySqlTestContainer;
import com.vertyll.snaptale.RedisTestContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(
    {
        MySqlTestContainer.class,
        RedisTestContainer.class
    }
)
class CsrfCookieIT {

    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";

    @LocalServerPort
    int port;

    // The cookies are Secure, which a cookie handler keeps from a plain-HTTP test server, so they are sent by hand.
    private final CookieManager cookies = new CookieManager();
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void meIssuesCsrfCookieThatUnlocksWrites() throws IOException, InterruptedException {
        assertThat(send(HttpRequest.newBuilder(uri("/api/me")).GET()).statusCode()).isEqualTo(200);
        HttpCookie csrfCookie = csrfCookie();
        assertThat(csrfCookie.getSecure()).isTrue();
        String token = csrfCookie.getValue();

        assertThat(
            send(HttpRequest.newBuilder(uri("/api/auth/logout")).POST(HttpRequest.BodyPublishers.noBody())).statusCode()
        ).isEqualTo(403);
        assertThat(
            send(
                HttpRequest.newBuilder(uri("/api/auth/logout"))
                    .header(CSRF_HEADER, token)
                    .POST(HttpRequest.BodyPublishers.noBody())
            ).statusCode()
        ).isEqualTo(200);
    }

    private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
        List<HttpCookie> stored = cookies.getCookieStore().getCookies();
        if (!stored.isEmpty()) {
            request.header(
                "Cookie",
                stored.stream()
                    .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                    .collect(Collectors.joining("; "))
            );
        }
        HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
        cookies.put(response.uri(), response.headers().map());
        return response;
    }

    private HttpCookie csrfCookie() {
        return cookies.getCookieStore()
            .getCookies()
            .stream()
            .filter(cookie -> CSRF_COOKIE.equals(cookie.getName()))
            .findFirst()
            .orElseThrow();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
