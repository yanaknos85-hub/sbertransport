package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;
import ru.sberbank.ditsib.transport.tariff.service.CalculateService;
import ru.sberbank.ditsib.transport.tariff.service.CalculatingControllerService;

import java.util.Collection;
import java.util.UUID;

/**
 * Implementation of service for calculating data.
 */
@RequiredArgsConstructor
@Component
class CalculatingControllerServiceImpl implements CalculatingControllerService {
    
    private final CalculateService calculateService;
    
    @Override
    public Collection<? extends CalculatedDto> calculate(TransportTypeEnum transportTypeEnum, TripDto tripData, Employee employee) {
        return calculateService.calculate(transportTypeEnum, tripData, employee);
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculate(TripDto tripData, Employee employee) {
        return calculateService.calculate(tripData, employee);
    }
    
    @Override
    public CalculatedDto calculate(TransportTypeEnum transportTypeEnum, UUID tariffId, TripDto tripData, Employee employee) {
        return calculateService.calculate(transportTypeEnum, tariffId, tripData, employee);
    }
    
    @Override
    public TransportPageDTO calculateTransports(
            TripDto tripData, String search, Boolean availableOnly,
            Employee employee, String token
                                               ) {
        return calculateService.calculateTransports(tripData, search, availableOnly, employee, token);
    }
    
    @Override
    public TransportWithCalculateDTO calculateTransport(
            TripDto tripData, UUID transportId, long startDate, long endDate, Employee employee, String token
                                                       ) {
        return calculateService.calculateTransport(tripData, transportId, startDate, endDate, employee, token);
    }
}
