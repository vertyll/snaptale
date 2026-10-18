package com.vertyll.snaptale.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class ReturnPageTest {

    @Test
    void aSignInReturnsOnceToThePageItWasStartedFrom() {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, "/post/7?comments=open"));

        assertThat(ReturnPage.take(request(session))).hasValue("/post/7?comments=open");
        assertThat(ReturnPage.take(request(session))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://evil.test/",
            "//evil.test/",
            "/\\evil.test",
            "post/7",
            "/a\nb",
            ""
        }
    )
    void aPageElsewhereIsNotKept(String page) {
        MockHttpSession session = new MockHttpSession();

        ReturnPage.remember(signIn(session, page));

        assertThat(ReturnPage.take(request(session))).isEmpty();
    }

    @Test
    void aSignInWithoutAPageForgetsAnEarlierOne() {
        MockHttpSession session = new MockHttpSession();
        ReturnPage.remember(signIn(session, "/post/7"));

        ReturnPage.remember(request(session));

        assertThat(ReturnPage.take(request(session))).isEmpty();
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
