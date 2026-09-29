package ru.sber.transport.audit.resolver;

import org.springframework.security.core.Authentication;

/**
 * Получение расширенных данных аутентифицированного пользователя.
 */
public interface AuthenticatedResolver {

    /**
     * Получить данные пользователя.
     *
     * @param user авторизованный пользователь.
     * @return данные пользователя для аудита.
     */
    String resolveUser(Authentication user);

}
