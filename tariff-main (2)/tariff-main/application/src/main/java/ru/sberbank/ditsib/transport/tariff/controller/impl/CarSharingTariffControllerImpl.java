package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.controller.CarSharingTariffController;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.CarSharingTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.NewCarSharingTariffDTO;
import ru.sberbank.ditsib.transport.tariff.service.EmployeeService;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Имплементация контроллера тарифов каршеринга
 */
@RestController
@RequiredArgsConstructor
public class CarSharingTariffControllerImpl implements CarSharingTariffController {
    
    private final TariffControllerService service;
    
    private final EmployeeService employeeService;
    
    @Override
    public CarSharingTariffDTO add(@Valid NewCarSharingTariffDTO newData, JwtAuthenticationToken authentication) {
        final var token = authentication.getToken();
        Employee initiator = employeeService.getByUserIdWithOrganizationId(UUID.fromString(token.getId()))
                                            .orElseThrow(() -> new EntityNotFoundException(Employee.class,
                                                                                           token.getId()));
        final var dataMaster = Optional.ofNullable(token.getClaimAsBoolean("data_master")).orElse(false);
        return service.addCarSharing(newData, initiator.getOrganizationId(), dataMaster);
    }
    
    @Override
    public void edit(UUID tariffId, @Valid NewCarSharingTariffDTO newData) {
        service.edit(TransportTypeEnum.CARSHARING.getId(), tariffId, newData);
    }
    
    @Override
    public void delete(UUID tariffId) {
        service.delete(TransportTypeEnum.CARSHARING.getId(), tariffId);
    }
    
    @Override
    public CarSharingTariffDTO get(UUID tariffId) {
        return service.getCarSharing(tariffId);
    }
    
    @Override
    public List<? extends CarSharingTariffDTO> getAll(UUID regionId) {
        return service.getAllCarSharing(regionId);
    }
}
