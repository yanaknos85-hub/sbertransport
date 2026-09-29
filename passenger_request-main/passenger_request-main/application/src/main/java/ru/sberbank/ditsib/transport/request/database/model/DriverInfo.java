package ru.sberbank.ditsib.transport.request.database.model;

import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;

/**
 * Информация о водителе и машине
 */
public interface DriverInfo {
    
    /**
     * Получение информации о водителе
     *
     * @return информация о водителе
     */
    Driver getDriver();
    
    /**
     * Получение информации о транспортном средстве
     *
     * @return информация о транспортном средстве
     */
    CarInfo getVehicle();
    
}
