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
import ru.sberbank.ditsib.transport.tariff.dto.files.CarsharingFileDto;
import ru.sberbank.ditsib.transport.tariff.exceptions.ContractLogicException;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.time.LocalDate;
import java.util.Map;

/**
 * Получатель данных из файла о каршеринговых тарифах.
 */
@Component
public class CarsharingTariffResolverImpl extends BaseTariffContractResolverImpl<CarSharingTariff, CarsharingFileDto> {

    private final TariffService tariffService;

    private final TariffSender tariffSender;

    private final ContractService contractService;

    private final ContractorService contractorService;

    private final GeoZoneRepository geoZoneRepository;

    public CarsharingTariffResolverImpl(
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
    protected CarSharingTariff newTariff() {
        return new CarSharingTariff();
    }

    @Override
    public void importData(CarsharingFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var contract = contractService.get(item.getContractor(), item.getContract()).orElseThrow(() -> new EntityNotFoundException(Contract.class, item.getContract()));
        final var tariff = getTariff(item);
        tariff.setActive(item.isActive());
        if (item.getId() == null || item.getId().isEmpty()) {
            newTariff(item, organization, contract, tariff);
        } else {
            updateTariff(item, tariff);
        }
        tariff.setTransportType(TransportTypeEnum.CARSHARING);
        final var cost = item.getCost();
        tariff.setRideCostPerMin((int) cost.getTime());
        tariff.setRideCostPerKm((int) cost.getDistance());
        tariff.setWaitCostPerMin((int) cost.getWaitTime());

        final var coefficients = item.getCoefficients();
        final var timedTariffParams = tariff.getTimedTariffParams();

        if (timedTariffParams != null) {
            timedTariffParams.setCoefWorkDayEvening(coefficients.getEvening());
            timedTariffParams.setCoefWorkDayNoon(coefficients.getDay());
            timedTariffParams.setCoefWorkDayMorning(coefficients.getMorning());
            timedTariffParams.setCoefWorkDayNight(coefficients.getNight());
            timedTariffParams.setCoefDayOff(coefficients.getWeekend());
        }

        if (coefficients != null) {
            tariff.setCoefCasko(coefficients.getInsurance());
            tariff.setCoefChildSeat(coefficients.getChildren());
            tariff.setCoefPetTransport(coefficients.getPet());
            tariff.setCoefTraffic(coefficients.getTraffic());
        }
        final var saved = (CarSharingTariff) tariffService.save(tariff);

        tariffSender.send(saved, !saved.isActive());
    }

    private void updateTariff(CarsharingFileDto item, CarSharingTariff tariff) {
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

    private void newTariff(CarsharingFileDto item, Organization organization, Contract contract, CarSharingTariff tariff) {
        if (!contract.isActive()) {
            throw new ContractLogicException("Договор " + contract.getContractNumber() + " не активен");
        }
        if (!LocalDate.now().isBefore(contract.getEndDate())) {
            throw new ContractLogicException("Срок действия договора " + contract.getContractNumber() + " истек");
        }
        if (item.getContractor() == null || item.getContractor().isEmpty()) {
            throw new ContractLogicException("При импотре тарифа не был указан контрагент");
        }
        final var contractor = contractorService.get(contract.getContractorId())
                .filter(it -> it.getName().equals(item.getContractor()))
                .orElseThrow(() ->  new EntityNotFoundException(Contractor.class, contract.getContractorId()));
        if (!contractor.isActive()) {
            throw new ContractLogicException("Контрагент " + contractor.getName() + " не активен");
        }
        tariff.setContract(contract);
        tariff.setOrganization(organization);
        final var geozone = geoZoneRepository.findByName(item.getRegion()).orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
        tariff.setRegionId(geozone == null ? null : geozone.getId());
    }

    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.CARSHARING;
    }

    @Override
    protected Class<CarSharingTariff> tariffClass() {
        return CarSharingTariff.class;
    }

    @Override
    protected CarsharingFileDto newItem() {
        return new CarsharingFileDto();
    }

    @Override
    protected void exportMapping(CarsharingFileDto resItem, CarSharingTariff item) {
        final var cost = resItem.getCost();
        cost.setWaitTime(item.getWaitCostPerMin());
        cost.setDistance(item.getRideCostPerKm());
        cost.setTime(item.getRideCostPerMin());

        final var coefficients = resItem.getCoefficients();
        coefficients.setInsurance(item.getCoefCasko());
        coefficients.setPet(item.getCoefPetTransport());
        coefficients.setChildren(item.getCoefChildSeat());
        coefficients.setTraffic(item.getCoefTraffic());
        final var timedTariffParams = item.getTimedTariffParams();
        if (timedTariffParams != null) {
            coefficients.setMorning(timedTariffParams.getCoefWorkDayMorning());
            coefficients.setDay(timedTariffParams.getCoefWorkDayNoon());
            coefficients.setEvening(timedTariffParams.getCoefWorkDayEvening());
            coefficients.setNight(timedTariffParams.getCoefWorkDayNight());
            coefficients.setWeekend(timedTariffParams.getCoefDayOff());
        }
    }
}
