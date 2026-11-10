package com.vertyll.snaptale.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class BearerTokenRequestsTest {

    private final BearerTokenRequests bearerTokenRequests = new BearerTokenRequests();

    @Test
    void aRequestWithABearerTokenMatches() {
        assertThat(bearerTokenRequests.matches(requestWithAuthorization("Bearer eyJhbGciOiJSUzI1NiJ9"))).isTrue();
    }

    @Test
    void theSchemeMatchesInAnyCase() {
        assertThat(bearerTokenRequests.matches(requestWithAuthorization("bearer eyJhbGciOiJSUzI1NiJ9"))).isTrue();
    }

    @Test
    void aRequestWithAMalformedBearerTokenMatches() {
        assertThat(bearerTokenRequests.matches(requestWithAuthorization("Bearer not a token"))).isTrue();
    }

    @Test
    void aRequestWithAnotherSchemeDoesNotMatch() {
        assertThat(bearerTokenRequests.matches(requestWithAuthorization("Basic YWRhOnNlY3JldA=="))).isFalse();
    }

    @Test
    void aRequestWithoutAnAuthorizationHeaderDoesNotMatch() {
        assertThat(bearerTokenRequests.matches(new MockHttpServletRequest())).isFalse();
    }

    private static MockHttpServletRequest requestWithAuthorization(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, value);
        return request;
    }
}
