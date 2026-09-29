package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.ScooterFileDto;
import ru.sberbank.ditsib.transport.tariff.exceptions.ContractLogicException;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

/**
 * Получатель данных из файла о тарифе скутера.
 */
@Component
public class ScooterTariffResolverImpl extends BaseTariffContractResolverImpl<ScooterTariff, ScooterFileDto>  {
    
    private final TariffService tariffService;
    
    private final TariffSender tariffSender;
    
    private final ContractService contractService;
    
    private final ContractorService contractorService;
    
    private final GeoZoneRepository geoZoneRepository;
    
    public ScooterTariffResolverImpl(
            TariffService tariffService, TariffSender tariffSender, OrganizationRepository organizationRepository, ContractService contractService,
            ContractorService contractorService, GeoZoneRepository geoZoneRepository, TariffSearchDTOMapper tariffSearchDTOMapper
                                    ) {
        super(tariffService, organizationRepository, tariffSearchDTOMapper, contractorService, contractService);
        this.tariffService = tariffService;
        this.tariffSender = tariffSender;
        this.contractService = contractService;
        this.contractorService = contractorService;
        this.geoZoneRepository = geoZoneRepository;
    }

    @Override
    protected ScooterTariff newTariff() {
        return new ScooterTariff();
    }

    @Override
    public void importData(ScooterFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var contract = contractService.get(item.getContractor(), item.getContract())
                                      .orElseThrow(() -> new EntityNotFoundException(Contract.class,
                                                                                     item.getContract()));
        final var tariff = getTariff(item);
        tariff.setActive(item.isActive());
        if (item.getId() == null || item.getId().isEmpty()) {
            checkNewTariffContract(item, contract);
            tariff.setContract(contract);
            tariff.setOrganization(organization);
            var geozone = geoZoneRepository.findByName(item.getRegion())
                                           .orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
            tariff.setRegionId(geozone == null ? null : geozone.getId());
        } else {
            checkExistTariffContract(item, tariff);
        }
        tariff.setTransportType(TransportTypeEnum.SCOOTER);
        final var sourceCost = item.getCost();
        if (sourceCost != null) {
            tariff.setRideCostPerMin((int) sourceCost.getTime());
            tariff.setRideCostPerKm((int) sourceCost.getDistance());
            tariff.setBookingCost((int) sourceCost.getBook());
        }

        final var targetParams = tariff.getTimedTariffParams();
        final var sourceCoefficients = item.getCoefficients();
        if (sourceCoefficients != null) {
            targetParams.setCoefWorkDayEvening(sourceCoefficients.getEvening());
            targetParams.setCoefWorkDayNoon(sourceCoefficients.getDay());
            targetParams.setCoefWorkDayMorning(sourceCoefficients.getMorning());
            targetParams.setCoefWorkDayNight(sourceCoefficients.getNight());
            targetParams.setCoefDayOff(sourceCoefficients.getWeekend());
            tariff.setCoefInsurance(sourceCoefficients.getInsurance());
        }
        
        tariffService.save(tariff);
        
        tariffSender.send(tariff, !tariff.isActive());
    }
    
    private void checkExistTariffContract(ScooterFileDto item, ScooterTariff tariff) {
        if (!item.getContract().equals(tariff.getContract().getContractNumber())) {
            throw new ImportChangingValuesException(ImportChangingValuesException.CONTRACT_NUMBER_CHANGING);
        }
        if (!item.getOrganization().equals(tariff.getOrganization().getName())) {
            throw new ImportChangingValuesException(ImportChangingValuesException.ORGANIZATION_NAME_CHANGING);
        }
        if (!item.getRegion().equals(geoZoneRepository.findById(tariff.getRegionId()).orElseThrow().getName())) {
            throw new ImportChangingValuesException(ImportChangingValuesException.REGION_NAME_CHANGING);
        }
    }
    
    private void checkNewTariffContract(ScooterFileDto item, Contract contract) {
        if (!contract.isActive()) {
            throw new ContractLogicException("Договор " + contract.getContractNumber() + " не активен");
        }
        if (!LocalDate.now().isBefore(contract.getEndDate())) {
            throw new ContractLogicException("Срок действия договора " + contract.getContractNumber() + " истек");
        }
        if (item.getContractor() == null || item.getContractor().isEmpty()) {
            throw new ContractLogicException("При импотре тарифа не был указан контрагент");
        }
        var contractor = contractorService.get(contract.getContractorId())
                .filter(it -> it.getName().equals(item.getContractor()))
                .orElseThrow(() -> new EntityNotFoundException(Contractor.class, contract.getContractorId()));
        if (!contractor.isActive()) {
            throw new ContractLogicException("Контрагент " + contractor.getName() + " не активен");
        }
    }
    
    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.SCOOTER;
    }
    
    @Override
    protected Class<ScooterTariff> tariffClass() {
        return ScooterTariff.class;
    }
    
    @Override
    protected ScooterFileDto newItem() {
        return new ScooterFileDto();
    }
    
    @Override
    protected void exportMapping(ScooterFileDto resItem, ScooterTariff item) {
        final var cost = resItem.getCost();
        cost.setBook(item.getBookingCost());
        cost.setDistance(item.getRideCostPerKm());
        cost.setTime(item.getRideCostPerMin());

        final var coefficients = resItem.getCoefficients();
        coefficients.setInsurance(item.getCoefInsurance());
        var timedTariffParams = item.getTimedTariffParams();
        if (timedTariffParams != null) {
            coefficients.setMorning(timedTariffParams.getCoefWorkDayMorning());
            coefficients.setDay(timedTariffParams.getCoefWorkDayNoon());
            coefficients.setEvening(timedTariffParams.getCoefWorkDayEvening());
            coefficients.setNight(timedTariffParams.getCoefWorkDayNight());
            coefficients.setWeekend(timedTariffParams.getCoefDayOff());
        }
    }
}
