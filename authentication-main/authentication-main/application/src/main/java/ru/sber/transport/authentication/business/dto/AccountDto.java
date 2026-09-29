package ru.sber.transport.authentication.business.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

/**
 * УЗ.
 */
@Data
@Builder
@ToString
public class AccountDto {
    
    /**
     * Идентификатор.
     */
    private final UUID id;
    
    /**
     * Логин.
     */
    private String login;
    
    /**
     * Email.
     */
    private final String email;

    /**
     * Phone.
     */
    @Setter
    private String phone;
    
    /**
     * Хэш пароля.
     */
    @Setter
    private String hash;
    
    /**
     * Флаг транспортного пароля.
     */
    private boolean transferPassword;
    
    /**
     * Список ролей.
     */
    private final Map<String, Boolean> roles;

    /**
     * Область видимости пользователя.
     */
    private final Scope scope;

    /**
     * Признак активности.
     */
    private boolean active;

    /**
     * Тип аутентификации.
     */
    @Builder.Default
    private AuthType authType = AuthType.BASIC;

    /**
     * Количество попыток входа.
     */
    private int numberOfLoginAttempts;
}
