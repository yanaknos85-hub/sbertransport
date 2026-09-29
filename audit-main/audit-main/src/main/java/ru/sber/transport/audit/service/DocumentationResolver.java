package ru.sber.transport.audit.service;

/**
 * Интерфейс для распознавателей документации.
 */
public interface DocumentationResolver {

    /**
     * Получение описания end-point'а.
     *
     * @param method метод доступа.
     * @param url end-point.
     * @return описание.
     */
    String getDescription(String method, String url);

    /**
     * Проверка аудитаблености end-point'а.
     *
     * @param method метод доступа.
     * @param url end-point.
     * @return результат проверки.
     */
    boolean isAuditable(String method, String url);
}
