package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.controller.CalculatingController;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;
import ru.sberbank.ditsib.transport.tariff.service.CalculatingControllerService;
import ru.sberbank.ditsib.transport.tariff.service.EmployeeService;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of calculating controller.
 */
@RestController
@RequiredArgsConstructor
class CalculatingControllerImpl implements CalculatingController {
    
    private final CalculatingControllerService service;
    private final EmployeeService employeeService;
    
    @Override
    public Collection<? extends CalculatedDto> calculateTrip(TripDto tripData, JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return service.calculate(tripData, employee);
    }
    
    @Override
    public CalculatedDto calculateTrip(UUID transportType, UUID tariffId, TripDto tripData, JwtAuthenticationToken authentication) {
        var transportTypeEnum = getTransportType(transportType);
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return service.calculate(transportTypeEnum, tariffId, tripData, employee);
    }
    
    @Override
    public CalculatedDto calculateTrip(TransportTypeEnum transportType, UUID tariffId, @Valid TripDto tripData, JwtAuthenticationToken authentication) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        return service.calculate(transportType, tariffId, tripData, employee);
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculateTrip(UUID tariffType, TripDto tripData, JwtAuthenticationToken authentication) {
        var transportTypeEnum = getTransportType(tariffType);
        return calculateTrip(transportTypeEnum, tripData, authentication);
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculateTrip(
            TransportTypeEnum transportType, @Valid TripDto tripData,
            JwtAuthenticationToken authentication
                                                            ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        var stream = service.calculate(transportType, tripData, employee).stream();
        if (TransportTypeEnum.TAXI.equals(transportType)) {
            stream = stream.sorted(Comparator.<CalculatedDto>comparingInt(dto -> dto.getTaxiClass().getOrder()));
        }
        return stream.collect(Collectors.toList());
    }
    
    @Override
    public TransportPageDTO calculateTransports(
            TripDto tripData, String search, Boolean availableOnly, JwtAuthenticationToken authentication, String token
                                               ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        
        return service.calculateTransports(tripData, search, availableOnly, employee, token);
    }
    
    @Override
    public TransportWithCalculateDTO calculateTransport(
            TripDto tripData, UUID transportId, long startDate, long endDate,
            JwtAuthenticationToken authentication, String token
                                                       ) {
        var employee = employeeService.getAuthenticatedEmployee(authentication);
        
        return service.calculateTransport(tripData, transportId, startDate, endDate, employee, token);
    }
    
    /**
     * Получение типа транспорта по идентификатору.
     *
     * @param tariffType идентификатор типа транспорта.
     *
     * @return тип транспорта.
     */
    private TransportTypeEnum getTransportType(UUID tariffType) {
        return TransportTypeEnum.fromId(tariffType)
                                .orElseThrow(() -> new EntityNotFoundException(TransportTypeEnum.class, tariffType));
    }
}
