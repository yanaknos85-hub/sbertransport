package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.BicycleTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.BicycleFileDto;
import ru.sberbank.ditsib.transport.tariff.exceptions.ContractLogicException;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

import java.time.LocalDate;
import java.util.Map;

/**
 * Получатель данных из файла о велосипедном тарифе.
 */
@Component
@ValidateOnExecution(type = ExecutableType.NONE)
@Transactional
public class BicycleTariffResolverImpl extends BaseTariffContractResolverImpl<BicycleTariff, BicycleFileDto> {

    private final TariffService tariffService;

    private final TariffSender tariffSender;

    private final ContractService contractService;

    private final ContractorService contractorService;

    private final GeoZoneRepository geoZoneRepository;

    /**
     * Создать получатель данных из файла о велосипедном тарифе.
     *
     * @param tariffService         - сервис тарифов
     * @param tariffSender          отправитель тарифов
     * @param organizationRepository   - репозиторий организаций
     * @param contractService       - сервис договоров
     * @param contractorService     сервис контрагентов
     * @param geoZoneRepository     - репозиторий геозон
     * @param tariffSearchDTOMapper маппер для поиска тарифов
     */
    public BicycleTariffResolverImpl(
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
    protected BicycleTariff newTariff() {
        return new BicycleTariff();
    }

    @Override
    public void importData(BicycleFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var tariff = getTariff(item);
        final var contract = contractService.get(item.getContractor(), item.getContract())
                .orElseThrow(() -> new EntityNotFoundException(Contract.class, item.getContract()));
        tariff.setActive(item.isActive());
        if (item.getId() == null || item.getId().isEmpty()) {
            validateData(item, contract);
            tariff.setContract(contract);
            tariff.setOrganization(organization);
            var geozone = geoZoneRepository.findByName(item.getRegion())
                    .orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
            tariff.setRegionId(geozone == null ? null : geozone.getId());
        } else {
            validateEditing(item, tariff);
        }
        tariff.setTransportType(TransportTypeEnum.BICYCLE);

        final var cost = item.getCost();
        tariff.setRideCostPerMin((int) cost.getTime());
        tariff.setRideCostPerKm((int) cost.getDistance());
        tariff.setBookingCost((int) cost.getBook());

        final var sourceCoefficients = item.getCoefficients();
        final var timedTariffParams = tariff.getTimedTariffParams();
        timedTariffParams.setCoefWorkDayEvening(sourceCoefficients.getEvening());
        timedTariffParams.setCoefWorkDayNoon(sourceCoefficients.getDay());
        timedTariffParams.setCoefWorkDayMorning(sourceCoefficients.getMorning());
        timedTariffParams.setCoefWorkDayNight(sourceCoefficients.getNight());
        timedTariffParams.setCoefDayOff(sourceCoefficients.getWeekend());
        tariff.setCoefInsurance(sourceCoefficients.getInsurance());

        tariffService.save(tariff);

        tariffSender.send(tariff, !tariff.isActive());
    }

    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.BICYCLE;
    }

    @Override
    protected Class<BicycleTariff> tariffClass() {
        return BicycleTariff.class;
    }

    @Override
    protected BicycleFileDto newItem() {
        return new BicycleFileDto();
    }

    @Override
    protected void exportMapping(BicycleFileDto resItem, BicycleTariff item) {
        final var cost = resItem.getCost();
        cost.setBook(item.getBookingCost());
        cost.setDistance(item.getRideCostPerKm());
        cost.setTime(item.getRideCostPerMin());

        final var timedTariffParams = item.getTimedTariffParams();
        final var coefficients = resItem.getCoefficients();
        if (timedTariffParams != null) {
            coefficients.setMorning(timedTariffParams.getCoefWorkDayMorning());
            coefficients.setDay(timedTariffParams.getCoefWorkDayNoon());
            coefficients.setEvening(timedTariffParams.getCoefWorkDayEvening());
            coefficients.setNight(timedTariffParams.getCoefWorkDayNight());
            coefficients.setWeekend(timedTariffParams.getCoefDayOff());
            coefficients.setInsurance(item.getCoefInsurance());
        }
    }

    private void validateEditing(BicycleFileDto item, BicycleTariff tariff) {
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

    private void validateData(BicycleFileDto item, Contract contract) {
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
}