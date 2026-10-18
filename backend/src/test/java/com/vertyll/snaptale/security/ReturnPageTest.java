package com.vertyll.snaptale.security;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnPageTest {
    private final RequestCache requests = ReturnPage.requestCache();

    @Test
    void aSignInReturnsToThePageItWasStartedFrom() {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, "/orders?status=open"));

        assertThat(returnsTo(session)).isEqualTo("/orders?status=open");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://evil.test/",
            "//evil.test/",
            "/\\evil.test",
            "orders",
            "/a\nb",
            ""
        }
    )
    void aPageElsewhereIsNotKept(String page) {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, page));

        assertThat(returnsTo(session)).isNull();
    }

    @Test
    void aSignInWithoutAPageForgetsOneAnEarlierSignInNamed() {
        MockHttpSession session = new MockHttpSession();
        ReturnPage.remember(signIn(session, "/orders"));

        ReturnPage.remember(request(session));

        assertThat(returnsTo(session)).isNull();
    }

    @Test
    void aSignInWithoutAPageKeepsTheRequestSpringSecuritySaved() {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest protectedPage = request(session);
        protectedPage.setRequestURI("/admin");
        requests.saveRequest(protectedPage, new MockHttpServletResponse());

        ReturnPage.remember(request(session));

        assertThat(returnsTo(session)).contains("/admin");
    }

    private @Nullable String returnsTo(MockHttpSession session) {
        SavedRequest saved = requests.getRequest(request(session), new MockHttpServletResponse());
        return saved == null ? null : saved.getRedirectUrl();
    }

    private static MockHttpServletRequest signIn(MockHttpSession session, String page) {
        MockHttpServletRequest request = request(session);
        request.setParameter(ReturnPage.PARAMETER, page);
        return request;
    }

    private static MockHttpServletRequest request(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        return request;
    }
}
