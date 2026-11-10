package com.vertyll.snaptale.security;

import java.net.URL;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * How the attributes of a session are written to Redis: as JSON, never as Java serialization.
 *
 * <p>
 * A session holds the security context with the signed-in {@link AuthenticatedUser}, the {@code OAuth2AuthorizedClient}
 * with the user's tokens, the sign-in in progress and the page it returns to. Java serialization would tie each stored
 * session to the exact classes of the release that wrote it, and reading it back would instantiate whatever classes its
 * bytes name. Spring Security's Jackson modules map its own types and allow only those, and the collections they hold,
 * as polymorphic types; the two types of SnapTale's own a session holds are allowed by name, so a stored value naming
 * any other class is refused.
 *
 * <p>
 * Spring Session picks the serializer up by its bean name.
 */
@Configuration(proxyBeanMethods = false)
class SessionStoreConfig {

    /**
     * The collections {@code List.of}, {@code Set.of} and {@code Map.of} return, which the claims of a sign-in's ID
     * token can be held in. Spring Security's own list allows only the collections it creates itself.
     */
    private static final String JDK_IMMUTABLE_COLLECTIONS = "java.util.ImmutableCollections$";

    @Bean
    RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return sessionSerializer();
    }

    static RedisSerializer<Object> sessionSerializer() {
        return new GenericJacksonJsonRedisSerializer(
            JsonMapper.builder()
                .addModules(
                    SecurityJacksonModules.getModules(
                        Thread.currentThread().getContextClassLoader(),
                        BasicPolymorphicTypeValidator.builder()
                            .allowIfSubType(JDK_IMMUTABLE_COLLECTIONS)
                            .allowIfSubType(URL.class)
                            .allowIfSubType(AuthenticatedUser.class)
                            .allowIfSubType(SimpleSavedRequest.class)
                    )
                )
                .addMixIn(AuthenticatedUser.class, AuthenticatedUserMixin.class)
                .addMixIn(SimpleSavedRequest.class, SimpleSavedRequestMixin.class)
                .build()
        );
    }

    /** The signed-in user, as Spring Security's modules store the OIDC user it extends, with SnapTale's id beside. */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
    )
    @JsonIgnoreProperties(
        {
            "attributes",
            "nameAttributeKey"
        }
    )
    // A mixin is never instantiated; it only lends its annotations to the class it mixes into.
    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract static class AuthenticatedUserMixin {

        @JsonCreator
        @SuppressWarnings("unused")
        AuthenticatedUserMixin(
            @JsonProperty("id") long id,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("idToken") OidcIdToken idToken,
            @JsonProperty("userInfo") @Nullable OidcUserInfo userInfo
        ) {
        }
    }

    /** The page a sign-in returns to, which Spring Security's modules leave out, read and written field by field. */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    interface SimpleSavedRequestMixin {
    }
}
