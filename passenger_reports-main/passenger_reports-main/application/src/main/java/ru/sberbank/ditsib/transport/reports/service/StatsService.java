package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.Stats;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис по операциям с контрагентами/каршеринговыми компаниями
 */
public interface StatsService {
    /**
     * Поиск контрагента по ID
     * @param id
     * @return контрагент
     */
    Optional<Stats> findById(UUID id);
    
    /**
     * Удаление контрагента
     * @param stats
     */
    void delete(Stats stats);
    
    /**
     * Сохранение статистики
     * @return
     */
    Stats save(Stats stats);
    
    /**
     * Поиск по параметрам
     * @return список статистик
     */
    List<Stats> findByYearAndMonth(Integer year, Integer month);
    
    /**
     * Поиск по параметрам
     * @return список статистик
     */
    Optional<Stats> getByMonthAndYearAndOrganizationIdAndServiceTypeAndTransportType(
            int month, int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType);
    
    /**
     * Поиск по параметрам
     * @return список статистик
     */
    Optional<Stats> getByYearAndOrganizationIdAndServiceTypeAndTransportType(
            int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType);
}
