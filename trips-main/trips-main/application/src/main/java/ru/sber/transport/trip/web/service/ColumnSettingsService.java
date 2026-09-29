package ru.sber.transport.trip.web.service;

import java.util.Map;
import java.util.UUID;

/**
 * Сервис для работы с настройакми отображения столбцов
 */
public interface ColumnSettingsService {

    /**
     * Создание настроек отображения столбцов
     * @param userId Идентификатор пользователя
     * @param setting Настройка
     * @return Информация о настройках
     */
    Map<String, Object> createColumnSetting(UUID userId, Map<String, Object> setting);

    /**
     * Измение настроек отображения столбцов
     * @param userId Идентификатор пользователя
     * @param setting Настройка
     */
    void updateColumnSetting(UUID userId, Map<String, Object> setting);

    /**
     * Удаление настроек отображения столбцов
     * @param userId Идентификатор пользователя
     */
    void deleteColumnSetting(UUID userId);

    /**
     * Получение настроек отображения столбцов
     * @param userId Идентификатор пользователя
     * @return Информация о настройках
     */
    Map<String, Object> getColumnSetting(UUID userId);

}
