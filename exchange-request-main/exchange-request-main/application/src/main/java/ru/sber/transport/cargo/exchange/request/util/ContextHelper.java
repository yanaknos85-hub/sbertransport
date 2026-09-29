package ru.sber.transport.cargo.exchange.request.util;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.exception.NotAuthorizedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Helper for less boiler plate code
 */
@UtilityClass
public class ContextHelper {
    public static UUID getUserId(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getId)
                .map(UUID::fromString)
                .orElseThrow(() -> new NotAuthorizedException("Пользователь не может быть авторизован в системе"));
    }
    
    public static List<String> getRoles(Authentication authentication) {
        return Optional.ofNullable(authentication)
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(JwtAuthenticationToken::getToken)
                .map(jwt -> jwt.getClaimAsStringList("roles"))
                .orElseThrow(() -> new NotAuthorizedException("Пользователь не может быть авторизован в системе"));
    }

    public static boolean hasRole(Authentication authentication, String role) {
        try {
            return getRoles(authentication).contains(role);
        } catch (NotAuthorizedException e) {
            return false;
        }
    }
}
