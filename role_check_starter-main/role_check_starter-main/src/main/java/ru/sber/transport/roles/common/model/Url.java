package ru.sber.transport.roles.common.model;

import org.springframework.http.HttpMethod;

import java.util.Set;
import java.util.UUID;

/**
 * Данные энд-поинта для проверки прав.
 */
public interface Url {

    /**
     * Название таблицы разрешаемых энд-поинтов.
     */
    String URL_TABLE = "urls";

    /**
     * Название таблицы ролей.
     */
    String ROLE_TABLE = "roles";

    /**
     * Название поля с идентификатором энд-поинта.
     */
    String URL_ID = "id";

    /**
     * Название поля с идентификатором энд-поинта в таблице разрешений.
     */
    String ROLE_URL_ID = "url_id";

    /**
     * Название поля с разрешенным методом доступа.
     */
    String URL_METHOD = "method";

    /**
     * Название поля с разрешаемым энд-поинтом в формате контроллера.
     */
    String URL_URL = "url";

    /**
     * Название поля с шаблоном поиска разрешений в формате базы данных.
     */
    String URL_PATTERN = "pattern";

    /**
     * Название поля с кодом разрешаемой роли.
     */
    String ROLE_ROLE = "role";

    /**
     * Создание объекта разрешаемого энд-поинта.
     *
     * @param method метод доступа.
     * @param url энд-поинт в формате контроллера.
     * @return объект для проверки разрешений.
     */
    static Url create(HttpMethod method, String url) {
        return create(null, method, url, url.replaceAll("\\{[-_\\da-zA-Z]+}", "_%"));
    }

    /**
     * Создание объекта разрешаемого энд-поинта.
     *
     * @param id идентификатор.
     * @param method метод доступа.
     * @param url энд-поинт в формате контроллера.
     * @param pattern шаблон для проверки доступа.
     * @return объект для проверки разрешений.
     */
    static Url create(UUID id, HttpMethod method, String url, String pattern) {
        var urlInfo = new UrlImpl();
        urlInfo.setId(id);
        urlInfo.setRestrictedUrl(url);
        urlInfo.setPattern(pattern);
        urlInfo.setMethod(method);
        return urlInfo;
    }

    /**
     * @return список ролей.
     */
    Set<String> getRoles();

    /**
     * @return метод доступа.
     */
    HttpMethod getMethod();

    /**
     * @return разрешаемый урл.
     */
    String getRestrictedUrl();

    /**
     * @return шаблон урла.
     */
    String getPattern();

    /**
     * @return идентификатор.
     */
    UUID getId();

    /**
     * @param id идентификатор.
     */
    void setId(UUID id);
}
