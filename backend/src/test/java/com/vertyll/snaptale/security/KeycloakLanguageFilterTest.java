package com.vertyll.snaptale.security;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakLanguageFilterTest {

    @Test
    void aCookieSharedWithKeycloakLivesUnderTheParentDomain() {
        assertThat(KeycloakLanguageFilter.sharedDomain("snaptale.vertyll.dev")).contains("vertyll.dev");
    }

    @Test
    void localhostAndAnAddressShareCookiesWithoutADomain() {
        assertThat(KeycloakLanguageFilter.sharedDomain("localhost")).isEmpty();
        assertThat(KeycloakLanguageFilter.sharedDomain(InetAddress.getLoopbackAddress().getHostAddress())).isEmpty();
    }
}
