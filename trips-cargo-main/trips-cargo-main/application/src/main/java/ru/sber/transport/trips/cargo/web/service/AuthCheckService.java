package ru.sber.transport.trips.cargo.web.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.trips.cargo.business.model.Dispatcher;

import java.util.UUID;

/**
 * Сервис для проверки авторизации пользователя
 */
public interface AuthCheckService {

    /**
     * Проверка авторизации диспетчера
     * @param contractorId id контрагента
     * @param authentication авторизационный токен
     * @return диспетчер
     */
    Dispatcher dispatcherAuthCheck(UUID contractorId, JwtAuthenticationToken authentication);

    /**
     * Проверка авторизации пользователя
     * @param contractorId id контрагента
     * @param authentication авторизационный токен
     * @return id контрагента
     */
    UUID userAuthCheck(UUID contractorId, JwtAuthenticationToken authentication);

}
