package ru.sberbank.ditsib.transport.request.service;

import java.util.List;
import java.util.UUID;

/**
 * Сервис отправки заявок готовых для отправки на исполнение контрагенту
 */
public interface ContractorTripService {
    /**
     * Обрабатывает новые заявки готовые к отправке
     */
    void processNewTrips();
    
    /**
     * Отправляет во внешние системы заявки в статусе IN PROGRESS
     */
    void processTripsInProgress();
    
    /**
     * Запросить изменения по списку заявок во внешней системе
     *
     * @param requestIds список заявок
     */
    void processTrips(List<UUID> requestIds);
}
