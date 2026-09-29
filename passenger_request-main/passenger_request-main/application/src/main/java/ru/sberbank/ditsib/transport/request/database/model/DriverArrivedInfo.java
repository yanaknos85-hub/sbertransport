package ru.sberbank.ditsib.transport.request.database.model;

import java.time.LocalDateTime;

/**
 * Интерфейс для получения информации о времени прибытии водителя
 */
public interface DriverArrivedInfo {

    /**
     * Возвращает время прибытия водителя
     * @return время прибытия водителя
     */
    LocalDateTime getDriverArrivedDatetime();

}
