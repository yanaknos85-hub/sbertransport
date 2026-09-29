package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.tariff.model.BaseTariffDataDto;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.controller.TariffController;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.ShortTariffDto;
import ru.sberbank.ditsib.transport.tariff.dto.TariffSearchDTO;
import ru.sberbank.ditsib.transport.tariff.dto.files.WorkGroupFileDto;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.CarSharingTariffSender;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.PersonalTariffSender;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.PublicTariffSender;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TaxiTariffSender;
import ru.sberbank.ditsib.transport.tariff.service.EmployeeService;
import ru.sberbank.ditsib.transport.tariff.service.TariffControllerService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers.WorkGroupResolverImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of personal tariff controller.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
class TariffControllerImpl implements TariffController {

    private final TariffControllerService service;

    private final TariffService tariffService;

    private final TaxiTariffSender taxiTariffSender;

    private final PersonalTariffSender personalTariffSender;

    private final CarSharingTariffSender carSharingTariffSender;

    private final PublicTariffSender publicTariffSender;

    private final EmployeeService employeeService;

    private final WorkGroupResolverImpl wgResolver;

    @Override
    public void delete(UUID transportType, UUID tariffId) {
        service.delete(transportType, tariffId);
    }

    @Override
    public BaseTariffDataDto get(UUID transportType, UUID tariffId) {
        return service.get(transportType, tariffId);
    }

    @Override
    public List<? extends ShortTariffDto> getAll(UUID regionId, ContractType contractType) {
        return service.getAll(regionId, contractType);
    }

    @Override
    public List<? extends ShortTariffDto> getAll(UUID tariffType, UUID regionId, ContractType contractType) {
        return service.getAll(tariffType, regionId, contractType);
    }

    @Override
    public Page<? extends ShortTariffDto> search(
            TariffSearchDTO searchDTO, Pageable page,
            JwtAuthenticationToken authentication
    ) {
        if (searchDTO != null && searchDTO.getContractType() == null && searchDTO.getTransportType() != null &&
                !(searchDTO.getTransportType() == TransportTypeEnum.PERSONAL || searchDTO.getTransportType() == TransportTypeEnum.PUBLIC)) {
            searchDTO = searchDTO.toBuilder().contractType(ContractType.TRANSITIONAL).build();
        }
        final var token = authentication.getToken();
        Employee initiator = employeeService.getByUserIdWithOrganizationId(UUID.fromString(token.getId()))
                .orElseThrow(() -> new EntityNotFoundException(Employee.class,
                        token.getId()));
        boolean dataMaster = Optional.ofNullable(token.getClaimAsBoolean("data_master")).orElse(false);
        return service.search(searchDTO, initiator.getOrganizationId(), dataMaster, page);
    }

    @Override
    public void resendTariffs() {
        List<TaxiTariff> taxiTariffs = tariffService.getAll(TransportTypeEnum.TAXI, null, null).stream()
                .map(TaxiTariff.class::cast).toList();
        taxiTariffs.forEach(taxiTariffSender::send);
        log.info("resendTariffs: taxi tariffs sent " + taxiTariffs.size());

        List<PersonalTariff> personalTariffs = tariffService.getAll(TransportTypeEnum.PERSONAL, null, null).stream()
                .map(PersonalTariff.class::cast).toList();
        personalTariffs.forEach(personalTariffSender::send);
        log.info("resendTariffs: personal tariffs sent " + personalTariffs.size());

        List<CarSharingTariff> carSharingTariffs = tariffService.getAll(TransportTypeEnum.CARSHARING, null, null).stream()
                .map(CarSharingTariff.class::cast).toList();
        carSharingTariffs.forEach(carSharingTariffSender::send);
        log.info("resendTariffs: car sharing tariffs sent " + carSharingTariffs.size());

        List<PublicTariff> publicTariffs = tariffService.getAll(TransportTypeEnum.PUBLIC, null, null).stream()
                .map(PublicTariff.class::cast).toList();
        publicTariffs.forEach(publicTariffSender::send);
        log.info("resendTariffs: public tariffs sent " + publicTariffs.size());
    }

    @Override
    public List<WorkGroupFileDto> workgroups() {
        return wgResolver.exportData(Map.of(), (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication());
    }
}
