package ru.sber.transport.roles.check.data.dao;

import org.springframework.http.HttpMethod;
import ru.sber.transport.roles.common.model.Url;

import java.util.Collection;
import java.util.Optional;

/**
 * Репозиторий для работы с ролями.
 */
public interface UrlRoleRepository {
    
    /**
     * Получение информации об URL-e.
     *
     * @param method метод доступа.
     * @param requestURI URL.
     * @return информация об URL.
     */
    Collection<Url> findAllByMethodAndPattern(HttpMethod method, String requestURI);
    
    /**
     * Получение информации об URL-e.
     *
     * @param method метод доступа.
     * @param requestURI URL.
     * @return информация об URL.
     */
    Optional<Url> findByMethodAndRestrictedUrl(HttpMethod method, String requestURI);
    
    /**
     * Получение списка инфы об URL.
     *
     * @param role роль для получения данных о разрешенных URL.
     * @return инфа об URL.
     */
    Collection<Url> findAllByRole(String role);

    /**
     * Сохранение информации об URL.
     *
     * @param urlInfo информация об URL.
     * @return сохраненная информаций.
     */
    Url save(Url urlInfo);

    /**
     * Удаление всего.
     */
    void deleteAll();

}
