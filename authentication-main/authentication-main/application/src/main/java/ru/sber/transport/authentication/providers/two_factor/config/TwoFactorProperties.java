package ru.sber.transport.authentication.providers.two_factor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.providers.config.Expiration;
import ru.sber.transport.authentication.providers.config.TokenProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "two-factor")
public class TwoFactorProperties implements TokenProperties {

    /**
     * Issuer of token.
     */
    private String issuer;

    /**
     * Expiration of token.
     */
    private Expiration expire = new Expiration();

    /**
     * Длина кода проверки.
     */
    private CodeProperties code = new CodeProperties();

    @Override
    public AuthType getAuthType() {
        return AuthType.TWO_FA;
    }
}
