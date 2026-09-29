package ru.sber.transport.roles.check.services;

import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Set;

/**
 * Провайдер ролей.
 */
public interface RoleProvider {
    
    /**
     * Получение доступных ролей.
     *
     * @param method метод.
     * @param requestURI URL.
     * @return роли.
     */
    Set<String> getAllowedRoles(HttpMethod method, String requestURI);
    
    /**
     * Сохранение информации о ролях.
     *
     * @param role роль.
     * @param method метод.
     * @param url URL.
     */
    void save(String role, HttpMethod method, String url);
    
    /**
     * Получение списка URL.
     *
     * @param role роль.
     * @return URL.
     */
    List<String> getUrls(String role);
    
    /**
     * Очистка инфо о ролях.
     *
     * @param role роль.
     */
    void clearRole(String role);
}
