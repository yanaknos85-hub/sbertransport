package ru.sberbank.ditsib.transport.request.service;

public interface DriverArrivedDeadlineChecker {
    
    /**
     * Проверка исполнения контрольных сроков подачи транспортного средства
     * (с пометкой заявок с нарушением контрольного срока для расчета SLA)
     */
    int execute();
}
