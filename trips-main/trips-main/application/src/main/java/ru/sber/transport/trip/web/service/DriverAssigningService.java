package ru.sber.transport.trip.web.service;

/**
 * Сервис отправки заявок готовых для отправки на исполнение контрагенту
 */
public interface DriverAssigningService {
    /**
     * Обрабатывает новые заявки готовые к отправке
     */
    void assignDrivers();

}
