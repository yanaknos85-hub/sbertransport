package ru.sber.transport.authsb.services;

import ru.sber.transport.authsb.dto.TokenResponseDto;

public interface ServiceController {

    /**
     * Сервис для получения ссылки для авторизации
     * @param sessionId id сессии
     * @return ссылка для авторизации
     */
    String createUrl(String sessionId);

    /**
     * Получение долгосрочных токенов доступа (access_token, refresh_token, id_token) в обмен на одноразовый authorization_code
     * @param code одноразовый authorization_code
     * @return долгосрочные токены доступа
     */
    TokenResponseDto getAuthToken(String code, String state);

    /**
     * Обновление токенов доступа
     * @param refreshToken токен обновления
     * @return  новые токены доступа
     */
    TokenResponseDto refreshToken(String refreshToken);
}
