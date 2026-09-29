package ru.sber.transport.authentication.providers.access.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.providers.config.Expiration;
import ru.sber.transport.authentication.providers.config.TokenProperties;

/**
 * Properties of JWT.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties implements TokenProperties {
    
    /**
     * Issuer of token.
     */
    private String issuer;
    
    /**
     * Expiration of token.
     */
    private Expiration expire = new Expiration();

    @Override
    public AuthType getAuthType() {
        return AuthType.BASIC;
    }
}
