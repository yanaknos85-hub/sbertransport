package ru.sber.transport.trips.cargo.web.service;

/**
 * Сервис отправки заявок готовых для отправки на исполнение контрагенту
 */
public interface DriverAssigningService {
    /**
     * Обрабатывает новые заявки готовые к отправке
     */
    void assignDrivers();

}
