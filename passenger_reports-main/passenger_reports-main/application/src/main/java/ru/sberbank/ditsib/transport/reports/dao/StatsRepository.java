package ru.sberbank.ditsib.transport.reports.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatsRepository extends JpaRepository<Stats, UUID> {
    
    List<Stats> findByYearAndMonth(Integer year, Integer month);
    
    List<Stats> findByYearAndOrganizationIdAndTransportType(
            int year, UUID organizationId, TransportTypeEnum transportType);
    
    Optional<Stats> findByMonthAndYearAndOrganizationIdAndTransportType(
            int month, int year, UUID organizationId, TransportTypeEnum transportType);
    
    Optional<Stats> findByMonthAndYearAndOrganizationIdAndServiceTypeAndTransportType(
            int month, int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType);
    
    Optional<Stats> findByYearAndOrganizationIdAndServiceTypeAndTransportType(
            int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType);
}
