package com.vertyll.snaptale.security;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.oauth2.client.DelegatingOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig {

    static final String REGISTRATION_ID = "keycloak";

    private static final String LOGIN_PATH = "/oauth2/authorization/" + REGISTRATION_ID;

    private static final String[] PUBLIC_READ_ENDPOINTS = {
        "/api/me",
        "/api/messages",
        "/api/posts",
        "/api/posts/*",
        "/api/users/*",
        "/media/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        CookieCsrfTokenRepository csrfTokenRepository,
        ClientRegistrationRepository clientRegistrations,
        OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService,
        OAuth2AuthorizedClientRepository authorizedClients,
        SessionAccessTokens sessionAccessTokens,
        SnaptaleSecurityProperties properties,
        ObjectMapper objectMapper
    ) {
        http.authorizeHttpRequests(
            authorize -> authorize.requestMatchers(HttpMethod.GET, PUBLIC_READ_ENDPOINTS)
                .permitAll()
                .requestMatchers("/api/**")
                .authenticated()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/error")
                .permitAll()
                .anyRequest()
                .denyAll()
        )
            .oauth2Login(
                login -> login.loginPage(LOGIN_PATH)
                    .authorizationEndpoint(
                        endpoint -> endpoint.authorizationRequestResolver(
                            new LocalizedAuthorizationRequestResolver(clientRegistrations)
                        )
                    )
                    .userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService))
                    .authorizedClientRepository(authorizedClients)
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/?login=failed")
            )
            .logout(
                logout -> logout.logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(
                        new LogoutUrlResponder(clientRegistrations, properties.frontendUrl(), objectMapper)
                    )
            )
            .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
            .exceptionHandling(
                exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .requestCache(RequestCacheConfigurer::disable)
            .addFilterBefore(new SessionAccessTokenFilter(sessionAccessTokens), AnonymousAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
        ClientRegistrationRepository clientRegistrations,
        OAuth2AuthorizedClientRepository authorizedClients,
        SharedRefreshes sharedRefreshes
    ) {
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrations, authorizedClients);
        manager.setAuthorizedClientProvider(
            new DelegatingOAuth2AuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().build(),
                new SingleFlightRefreshTokenProvider(
                    OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().build(),
                    sharedRefreshes,
                    Clock.systemUTC()
                )
            )
        );
        return manager;
    }

    @Bean
    JwtDecoder accessTokenDecoder(KeycloakProperties keycloak) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder.withJwkSetUri(keycloak.backchannel() + "/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(keycloak.realmUrl()),
                new JwtClaimValidator<List<String>>(
                    JwtClaimNames.AUD,
                    audience -> audience != null && audience.contains(keycloak.audience())
                )
            )
        );
        return decoder;
    }

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
        KeycloakProperties keycloak,
        SnaptaleSecurityProperties properties
    ) {
        String browser = keycloak.realmUrl() + "/protocol/openid-connect";
        String backchannel = keycloak.backchannel() + "/protocol/openid-connect";
        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientName("Keycloak")
            .clientId(keycloak.clientId())
            .clientSecret(keycloak.clientSecret())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(properties.frontendUrl() + "/login/oauth2/code/{registrationId}")
            .scope(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL)
            .authorizationUri(browser + "/auth")
            .tokenUri(backchannel + "/token")
            .jwkSetUri(backchannel + "/certs")
            .issuerUri(keycloak.realmUrl())
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .providerConfigurationMetadata(Map.of("end_session_endpoint", browser + "/logout"))
            .build();
        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    @SuppressWarnings("java:S3330")
    CookieCsrfTokenRepository csrfTokenRepository(SnaptaleSecurityProperties properties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.secure(properties.secureCookies()).sameSite("Lax"));
        return repository;
    }
}
