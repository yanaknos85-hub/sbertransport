package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dao.StatsRepository;
import ru.sberbank.ditsib.transport.reports.model.Stats;
import ru.sberbank.ditsib.transport.reports.service.StatsService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class StatsServiceImpl implements StatsService {
    
    private final StatsRepository statsRepository;
    
    @Override
    public Optional<Stats> findById(UUID id) {
        return statsRepository.findById(id);
    }
    
    @Override
    public Stats save(Stats stats) {
        return statsRepository.save(stats);
    }
    
    @Override
    public void delete(Stats stats) {
        statsRepository.delete(stats);
    }
    
    @Override
    public List<Stats> findByYearAndMonth(Integer year, Integer month) {
        return statsRepository.findByYearAndMonth(year, month);
    }
    
    @Override
    public Optional<Stats> getByMonthAndYearAndOrganizationIdAndServiceTypeAndTransportType(
            int month, int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType) {
        return statsRepository.findByMonthAndYearAndOrganizationIdAndServiceTypeAndTransportType(month, year, organizationId, serviceType,
                                                                                                 transportType);
    }
    
    @Override
    public Optional<Stats> getByYearAndOrganizationIdAndServiceTypeAndTransportType(
            int year, UUID organizationId, TransportServiceType serviceType, TransportTypeEnum transportType) {
        return statsRepository.findByYearAndOrganizationIdAndServiceTypeAndTransportType(year, organizationId, serviceType, transportType);
    }
    
}
