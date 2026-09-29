package ru.sberbank.ditsib.transport.request.database.model.magenta;

import java.util.UUID;

/**
 * Методы для совместных поездкок
 */
public interface CoopRequest {
    /**
     * Тип поездки - совместная или индивидуальная
     */
    boolean isCoopTrip();
    
    /**
     * Идентификатор в сервисе magenta
     */
    UUID getRideId();
}
