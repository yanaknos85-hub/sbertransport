package ru.sber.transport.dispatcher.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

/**
 * Сервис проверки авторизации
 */
public interface AuthCheckService {

    /**
     * Получить идентификатор контрагента по токену
     * @param token токен
     * @return идентификатор контрагента
     */
    UUID getContractorIdByToken(JwtAuthenticationToken token);

}
