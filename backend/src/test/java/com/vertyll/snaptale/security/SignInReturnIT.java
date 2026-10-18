package com.vertyll.snaptale.security;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jakarta.servlet.http.Cookie;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.vertyll.snaptale.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/** A whole sign-in through Spring Security, with Keycloak's token endpoint and ID token stood in for. */
@IntegrationTest
class SignInReturnIT {

    private static final String REGISTRATION = "keycloak";

    @Autowired
    MockMvc mvc;

    @Autowired
    ClientRegistrationRepository registrations;

    @MockitoBean
    OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> codeTokens;

    @MockitoBean
    JwtDecoderFactory<ClientRegistration> idTokenDecoders;

    @BeforeEach
    void keycloakIssuesTokens() {
        when(codeTokens.getTokenResponse(any())).thenReturn(
            OAuth2AccessTokenResponse.withToken("access-token")
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(300)
                .refreshToken("refresh-token")
                .scopes(Set.of("openid", "profile", "email"))
                .additionalParameters(Map.of(OidcParameterNames.ID_TOKEN, "id-token"))
                .build()
        );
    }

    @Test
    void aSignInReturnsToThePageItWasStartedFrom() {
        assertThat(landingAfterSignIn("/post/7?comments=open")).isEqualTo("/post/7?comments=open");
    }

    @Test
    void aSignInWithoutAPageLandsOnTheStartPage() {
        assertThat(landingAfterSignIn(null)).isEqualTo("/");
    }

    @Test
    void aSignInNeverReturnsToAPageElsewhere() {
        assertThat(landingAfterSignIn("//evil.test/")).isEqualTo("/");
    }

    private String landingAfterSignIn(@Nullable String page) {
        MockHttpServletRequestBuilder authorize = get("/oauth2/authorization/" + REGISTRATION);
        if (page != null) {
            authorize.param(ReturnPage.PARAMETER, page);
        }
        MockHttpServletResponse started = MockMvcTester.create(mvc).perform(authorize).getResponse();
        // The browser keeps the session cookie, which holds the sign-in transaction and the page to return to.
        Cookie[] cookies = started.getCookies();
        Map<String, String> query = queryOf(Objects.requireNonNull(started.getRedirectedUrl(), "goes to Keycloak"));
        String nonce = Objects.requireNonNull(query.get(OidcParameterNames.NONCE), "the sign-in sends a nonce");
        when(idTokenDecoders.createDecoder(any())).thenReturn(token -> idToken(nonce));

        MockHttpServletResponse callback = MockMvcTester.create(mvc)
            .perform(
                get("/login/oauth2/code/" + REGISTRATION).param("code", "code")
                    .param("state", query.get("state"))
                    .cookie(cookies)
            )
            .getResponse();
        return Objects.requireNonNull(callback.getRedirectedUrl(), "the callback redirects");
    }

    private Jwt idToken(String nonce) {
        ClientRegistration registration = Objects.requireNonNull(registrations.findByRegistrationId(REGISTRATION));
        return Jwt.withTokenValue("id-token")
            .header("alg", "RS256")
            .issuer(
                Objects.requireNonNull(
                    registration.getProviderDetails().getIssuerUri(),
                    "the registration names its issuer"
                )
            )
            .subject("2b1c7a52-3c1e-4d2a-9f5b-6e8d7c1a2b3c")
            .audience(List.of(registration.getClientId()))
            .claim("azp", registration.getClientId())
            .claim(OidcParameterNames.NONCE, nonce)
            .claim("email", "return@snaptale.test")
            .claim("preferred_username", "returning")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(300))
            .build();
    }

    private static Map<String, String> queryOf(String url) {
        Map<String, String> query = new HashMap<>();
        UriComponentsBuilder.fromUri(URI.create(url))
            .build()
            .getQueryParams()
            .forEach((name, values) -> query.put(name, URLDecoder.decode(values.getFirst(), StandardCharsets.UTF_8)));
        return query;
    }
}
