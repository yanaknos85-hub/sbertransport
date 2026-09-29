package ru.sber.transport.authentication.business.providers;

import org.springframework.data.domain.Page;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;

/**
 * Аудитор.
 */
public interface AuditProvider {
    
    /**
     * Записать информацию о неверном логине.
     *
     * @param login введенный логин.
     */
    void wrongLogin(String login);
    
    /**
     * Записать информацию о неверном пароле.
     *
     * @param login введенный логин.
     */
    void wrongPassword(String login);

    /**
     * Большое количество неудачных попыток авторизации.
     *
     * @param login логин.
     */
    void tooManyLoginTries(String login);
    
    /**
     * Записать информацию об успешном логине.
     *
     * @param login введенный логин.
     * @param userAgent информация заголовка User Agent.
     * @param clientType тип клиента.
     */
    void login(String login, String userAgent, String clientType) throws AccountNotFoundException;
    
    /**
     * Записать информацию об успешном логауте.
     *
     * @param login введенный логин.
     */
    void logout(String login);
    
    /**
     * Записать информацию о проваленном релогине.
     *
     * @param login введенный логин.
     */
    void refreshExpired(String login, String refresh);
    
    /**
     * Записать информацию о релогине.
     *
     * @param login введенный логин.
     * @param userAgent информация заголовка User Agent.
     * @param clientType тип клиента.
     */
    void refresh(String login, String userAgent, String clientType, String refresh) throws AccountNotFoundException;
    
    /**
     * Получит записи о попытках входа.
     *
     * @param page номер страницы.
     * @param size размер страницы.
     * @return записи.
     */
    Page<AuditDto> getRecords(int page, int size);
    
}
