package ru.sber.transport.journal.helper;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Работа с аутентификацией
 */
@UtilityClass
public class UserAuthorizationHelper {
    
    /**
     * Получение идентификатора записи с таблицы corporate.user из объекта аутентификации
     *
     * @param authentication {@link Authentication}
     *
     * @return Идентификатор записи с таблицы corporate.user
     */
    public static UUID getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken authenticationToken) {
            return UUID.fromString(authenticationToken.getTokenAttributes().get("jti").toString());
        } else {
            return Optional.ofNullable(authentication)
                           .map(Authentication::getName)
                           .map(String::valueOf)
                           .map(UUID::fromString)
                           .orElse(null);
        }
    }
}