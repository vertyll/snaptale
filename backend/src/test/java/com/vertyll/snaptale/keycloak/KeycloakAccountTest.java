package com.vertyll.snaptale.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.vertyll.snaptale.common.Language;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
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
    void theLanguageIsStoredOnTheAccountAndNothingElseChanges() {
        String accountUrl = "http://keycloak/realms/snaptale/account/";
        server.expect(requestTo(accountUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withSuccess(
                    "{\"username\":\"ola\",\"email\":\"ola@example.com\",\"attributes\":{\"locale\":[\"pl\"],\"terms\":[\"1\"]}}",
                    MediaType.APPLICATION_JSON
                )
            );
        server.expect(requestTo(accountUrl))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer access-1"))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.email").value("ola@example.com"))
            .andExpect(jsonPath("$.attributes.terms[0]").value("1"))
            .andExpect(jsonPath("$.attributes.locale[0]").value("en"))
            .andRespond(withNoContent());

        account.keepLanguage(ACCESS_TOKEN, Language.EN);

        server.verify();
    }

    @Test
    void aLanguageTheAccountAlreadyHasIsNotWrittenAgain() {
        server.expect(requestTo("http://keycloak/realms/snaptale/account/"))
            .andRespond(withSuccess("{\"attributes\":{\"locale\":[\"en\"]}}", MediaType.APPLICATION_JSON));

        account.keepLanguage(ACCESS_TOKEN, Language.EN);

        server.verify();
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
