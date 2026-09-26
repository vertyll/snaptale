package com.vertyll.snaptale.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
class SecurityConfig {

    private static final String[] PUBLIC_READ_ENDPOINTS = {
        "/api/me",
        "/api/messages",
        "/api/posts",
        "/api/posts/*",
        "/api/users/*",
        "/media/**"
    };

    private static final String[] PUBLIC_AUTH_ENDPOINTS = {
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/password/forgot",
        "/api/auth/password/reset",
        "/api/auth/email/verify"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        CookieCsrfTokenRepository csrfTokenRepository,
        SecurityContextRepository securityContextRepository,
        @Value("${server.servlet.session.cookie.name}") String sessionCookieName
    ) {
        http.authorizeHttpRequests(
            authorize -> authorize.requestMatchers(HttpMethod.GET, PUBLIC_READ_ENDPOINTS)
                .permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_AUTH_ENDPOINTS)
                .permitAll()
                .requestMatchers("/api/**")
                .authenticated()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/error")
                .permitAll()
                .anyRequest()
                .denyAll()
        )
            .securityContext(context -> context.securityContextRepository(securityContextRepository))
            .logout(
                logout -> logout.logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                    .deleteCookies(sessionCookieName)
            )
            .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))
            .exceptionHandling(
                exceptions -> exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .requestCache(RequestCacheConfigurer::disable);
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository(SnaptaleSecurityProperties properties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie.secure(properties.secureCookies()).sameSite("Lax"));
        return repository;
    }

    @Bean
    CsrfAuthenticationStrategy csrfAuthenticationStrategy(CookieCsrfTokenRepository csrfTokenRepository) {
        return new CsrfAuthenticationStrategy(csrfTokenRepository);
    }
}
