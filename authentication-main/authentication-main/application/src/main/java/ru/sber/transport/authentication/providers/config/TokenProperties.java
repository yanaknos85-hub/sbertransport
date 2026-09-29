package ru.sber.transport.authentication.providers.config;

import ru.sber.transport.authentication.business.dto.AuthType;

public interface TokenProperties {

    String getIssuer();

    Expiration getExpire();

    AuthType getAuthType();

}
