package com.vertyll.snaptale.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KeycloakAccountTest {

    private static final String CREDENTIALS = "http://keycloak/realms/snaptale/account/credentials?type=otp";

    private static final Jwt ACCESS_TOKEN = Jwt.withTokenValue("access-1").header("alg", "RS256").subject("s").build();

    private final RestClient.Builder keycloak = RestClient.builder().baseUrl("http://keycloak/realms/snaptale");
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(keycloak).build();
    private final KeycloakAccount account = new KeycloakAccount(keycloak);

    @Test
    void theSecondFactorIsReadWithTheCallersOwnToken() {
        server.expect(requestTo(CREDENTIALS))
            .andExpect(header("Authorization", "Bearer access-1"))
            .andRespond(withSuccess("""
                    [{"type":"otp","category":"two-factor",
                      "userCredentialMetadatas":[{"credential":{"id":"otp-1","type":"otp","userLabel":"phone"}}]}]
                    """, MediaType.APPLICATION_JSON));

        assertThat(account.secondFactor(ACCESS_TOKEN)).contains("otp-1");
    }

    @Test
    void noOneTimePasswordIsNoSecondFactor() {
        server.expect(requestTo(CREDENTIALS))
            .andRespond(
                withSuccess(
                    "[{\"type\":\"otp\",\"category\":\"two-factor\",\"userCredentialMetadatas\":[]}]",
                    MediaType.APPLICATION_JSON
                )
            );

        assertThat(account.secondFactor(ACCESS_TOKEN)).isEmpty();
    }
}
