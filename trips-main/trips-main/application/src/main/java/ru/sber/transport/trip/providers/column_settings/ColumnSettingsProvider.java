package ru.sber.transport.trip.providers.column_settings;

import java.util.Map;
import java.util.UUID;

/**
 * Провайдер настроек отображаемых столбцов
 */
public interface ColumnSettingsProvider {

    /**
     * Сохранение
     * @param userId Идентификатор пользовател
     * @param setting Настройка
     * @return Информация о настройках
     */
    Map<String, Object> save(UUID userId, Map<String, Object> setting);

    /**
     * Изменение
     * @param userId Идентификатор пользовател
     * @param setting Настройка
     */
    void update(UUID userId, Map<String, Object> setting);

    /**
     * Удаление
     * @param userId Идентификатор пользователа
     */
    void delete(UUID userId);

    /**
     * Получение
     * @param userId Идентификатор пользователа
     * @return Информация о настройках
     */
    Map<String, Object> get(UUID userId);

}
