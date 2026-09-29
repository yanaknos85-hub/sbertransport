package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class UserAuthorizationHelper {
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
    
    public static Set<String> getRoles(Authentication authentication) {
        return authentication.getAuthorities().stream()
                             .map(GrantedAuthority::getAuthority)
                             .collect(Collectors.toSet());
    }
}
