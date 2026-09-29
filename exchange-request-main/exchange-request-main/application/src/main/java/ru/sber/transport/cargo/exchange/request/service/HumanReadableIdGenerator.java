package ru.sber.transport.cargo.exchange.request.service;

/**
 * Сервис генерации читаемого идентификатора в формате: {префикс}-{ГГГГММ}-{счётчик}
 */
public interface HumanReadableIdGenerator {

    /**
     * Генерирует уникальный human-readable ID.
     * Пример: ОР-202504-00000001
     *
     * @return сгенерированный идентификатор
     */
    String generateHumanReadableId();

    /**
     * Генерирует human-readable ID с пользовательским префиксом.
     * Полезно для разных типов заявок (например, АУК, ФРХ).
     *
     * @param prefix кастомный префикс (например, "АУК")
     * @return сгенерированный идентификатор
     */
    String generateHumanReadableId(String prefix);
}