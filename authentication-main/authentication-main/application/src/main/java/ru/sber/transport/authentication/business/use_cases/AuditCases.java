package ru.sber.transport.authentication.business.use_cases;

import org.springframework.data.domain.Page;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;

import java.util.UUID;

/**
 * Кейсы работы с аудитом входа.
 */
public interface AuditCases {
    
    /**
     * Вход успешен.
     *
     * @param login логин.
     * @param userAgent информация заголовка User Agent.
     * @param clientType тип клиента
     */
    void login(String login, String userAgent, String clientType) throws AccountNotFoundException;
    
    /**
     * Выход успешен.
     *
     * @param userId идентификатор пользователя.
     */
    void logout(UUID userId);
    
    /**
     * Обновление сессии успешно.
     *
     * @param refresh токен обновления.
     * @param userAgent информация заголовка User Agent.
     * @param clientType тип клиента
     */
    void refresh(String refresh, String userAgent, String clientType) throws AccountNotFoundException;
    
    /**
     * Логин неверен.
     *
     * @param login логин.
     */
    void wrongLogin(String login);
    
    /**
     * Пароль неверен.
     *
     * @param login логин.
     */
    void wrongPassword(String login);

    /**
     * Большое количество неудачных попыток авторизации.
     *
     * @param login логин.
     */
    void tooManyLoginTries(String login);
    
    /**
     * Получение записей об аудите.
     *
     * @param page страница.
     * @param size размер страницы.
     * @return список записей.
     */
    Page<AuditDto> get(Integer size, Integer page);
    
    /**
     * Токен обновления истек.
     *
     * @param refresh токен.
     */
    void refreshExpired(String refresh);
}
