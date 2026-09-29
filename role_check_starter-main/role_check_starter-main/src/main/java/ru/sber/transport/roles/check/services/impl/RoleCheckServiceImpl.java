package ru.sber.transport.roles.check.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import ru.sber.transport.roles.check.services.RoleProvider;

import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Сервис проверки роли.
 */
@Component
@RequiredArgsConstructor
class RoleCheckServiceImpl implements AuthorizationManager<RequestAuthorizationContext> {

    private final RoleProvider provider;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        var request = context.getRequest();
        var method = request.getMethod();
        if (HttpMethod.OPTIONS.name().equals(method)) {
            return new AuthorizationDecision(true);
        }
        var roles = authentication.get().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toUnmodifiableSet());
        var requestUri = request.getRequestURI();
        var allowed = provider.getAllowedRoles(HttpMethod.valueOf(method), requestUri);
        return new AuthorizationDecision(allowed.stream().anyMatch(roles::contains));
    }
}
