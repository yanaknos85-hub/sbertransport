package ru.sber.transport.cargo.exchange.request.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.cargo.exchange.request.database.model.User;

import java.util.UUID;

/**
 * Сервис для работы с пользователями.
 * Основной метод — поиск пользователя по tokenId (jti из JWT).
 */
public interface UserService {

    /**
     * Находит идентификатор пользователя по уникальному идентификатору токена (token_id).
     *
     * @param authentication jwt токен
     * @return UUID пользователя, если найден
     */
    UUID findUserIdByToken(JwtAuthenticationToken authentication);

    /**
     * Находит организацию-пользователя по уникальному идентификатору токена (token_id).
     *
     * @param authentication jwt токен
     * @return Optional с пользователем, если найден
     */
    UUID findOrganizationIdByToken(JwtAuthenticationToken authentication);

    /**
     * Находит пользователя по токену.
     *
     * @param authentication jwt токен
     * @return Пользователь, если найден
     */
    User findUserByToken(JwtAuthenticationToken authentication);
}