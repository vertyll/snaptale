package com.vertyll.snaptale.keycloak;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;

/**
 * How SnapTale reaches Keycloak over HTTP: its account API, its token endpoint and its signing keys alike. A Keycloak
 * that stops answering fails the call after the timeout instead of holding the request's thread.
 */
@Configuration(proxyBeanMethods = false)
class KeycloakHttpConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Bean
    ClientHttpRequestFactory keycloakRequests() {
        JdkClientHttpRequestFactory requests =
                new JdkClientHttpRequestFactory(HttpClient.newBuilder().connectTimeout(TIMEOUT).build());
        requests.setReadTimeout(TIMEOUT);
        return requests;
    }
}
