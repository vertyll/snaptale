package com.vertyll.snaptale.security;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> type = parameter.getParameterType();
        return CurrentUser.class.equals(type) || Viewer.class.equals(type);
    }

    @Override
    public Object resolveArgument(
        MethodParameter parameter,
        @Nullable ModelAndViewContainer mavContainer,
        NativeWebRequest webRequest,
        @Nullable WebDataBinderFactory binderFactory
    ) {
        Optional<Long> userId = signedInUserId();
        if (Viewer.class.equals(parameter.getParameterType())) {
            return new Viewer(userId);
        }
        return new CurrentUser(
            userId.orElseThrow(
                () -> new IllegalStateException(
                    "Endpoint " + parameter.getExecutable()
                            + " requires authentication, but SecurityConfig has no rule for it"
                )
            )
        );
    }

    private static Optional<Long> signedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user
                ? Optional.of(user.id()) : Optional.empty();
    }
}
