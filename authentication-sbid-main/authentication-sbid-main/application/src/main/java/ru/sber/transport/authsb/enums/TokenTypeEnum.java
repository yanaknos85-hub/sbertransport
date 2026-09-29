package ru.sber.transport.authsb.enums;

import jdk.jfr.Description;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Description("Тип предоставления авторизации (grant type), который используется для запроса токена доступа.")
public enum TokenTypeEnum {
    AUTHORIZATION_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token");

    private final String name;
}
