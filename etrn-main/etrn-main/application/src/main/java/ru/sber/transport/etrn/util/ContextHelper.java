package ru.sber.transport.etrn.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Helper for less boiler plate code
 */
@Slf4j
@UtilityClass
public class ContextHelper {

    public static UUID getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken authenticationToken) {
            UUID userId = UUID.fromString(authenticationToken.getTokenAttributes().get("jti").toString());
            log.debug("Извлечён userId из JWT: jti={}", userId);
            return userId;
        } else {
            UUID userId = Optional.ofNullable(authentication)
                    .map(Authentication::getName)
                    .map(String::valueOf)
                    .map(UUID::fromString)
                    .orElse(null);
            if (userId == null) {
                log.warn("Не удалось извлечь userId из Authentication: type={}",
                        authentication != null ? authentication.getClass().getSimpleName() : "null");
            } else {
                log.debug("Извлечён userId из Authentication.getName(): {}", userId);
            }
            return userId;
        }
    }

    public static Set<String> getRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }
}
