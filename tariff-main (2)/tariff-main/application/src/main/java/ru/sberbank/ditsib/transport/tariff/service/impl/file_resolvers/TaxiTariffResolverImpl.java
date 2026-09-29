package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.*;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.exceptions.TariffAlreadyExistsException;
import ru.sberbank.ditsib.transport.tariff.exceptions.TariffLogicException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Получатель данных из файла о тарифе такси.
 */
@Component
public class TaxiTariffResolverImpl extends BaseTariffContractResolverImpl<TaxiTariff, TaxiFileDto> {
    
    private static final String VALUE = "No such value for taxi class '%s' ";
    
    private final TariffService tariffService;
    
    private final TariffSender tariffSender;
    
    private final ContractService contractService;
    
    private final ContractorService contractorService;
    
    private final GeoZoneRepository geoZoneRepository;
    
    private static final String WORK_GROUP_VARIABLE = "транспорт";
    
    public TaxiTariffResolverImpl(
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
    protected TaxiTariff newTariff() {
        return new TaxiTariff();
    }

    @Override
    public void importData(TaxiFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final var contract = contractService.get(item.getContractor(), item.getContract())
                                      .orElseThrow(() -> new TariffLogicException("Контракт с номером %s не найден".formatted(item.getContract())));

        final var tariff = getTariff(item, organization, contract);
        final var contractor = getContractor(item, contract);
        tariff.setActive(item.isActive());
        checkContract(item, contract, tariff, contractor);
        tariff.setTransportType(TransportTypeEnum.TAXI);
        // В тарифе
        final var sourceIncludes = item.getIncludes();
        tariff.setDistanceIncluded(sourceIncludes.getDistance());
        tariff.setTimeIncluded((int) sourceIncludes.getTime());
        tariff.setFreeWaitingTime((int) sourceIncludes.getWaitingTime());
        tariff.setMinRideDistanceCost((int) sourceIncludes.getMinimumDistance());
        tariff.setMinRideTimeCost((int) sourceIncludes.getMinimumTime());
        // стоимость
        final var sourceCost = item.getCost();
        tariff.setRideCostPerKm((int) sourceCost.getDistance());
        tariff.setRideCostPerMin((int) sourceCost.getTime());
        tariff.setWaitCostPerMin((int) sourceCost.getWaitTime());
        tariff.setWaitCostPerMinIntermediate((int) sourceCost.getWaitIntermediateTime());
        // за городом
        final var targetSuburbTariffParams = tariff.getSuburbTariffParams();
        final var sourceSuburbParams = sourceCost.getSuburb();
        targetSuburbTariffParams.setCostPerKmSuburb((int) sourceSuburbParams.getDistance());
        targetSuburbTariffParams.setCostPerMinSuburb((int) sourceSuburbParams.getTime());
        targetSuburbTariffParams.setSuburbServiceCostPerKm((int) sourceSuburbParams.getSubmissionDistance());
        targetSuburbTariffParams.setSuburbServiceCostPerMin((int) sourceSuburbParams.getSubmissionTime());
        // между регионами
        targetSuburbTariffParams.setCostPerKmInterRegion((int) sourceCost.getInterregional().getDistance());
        targetSuburbTariffParams.setCostPerMinInterRegion((int) sourceCost.getInterregional().getTime());
        // коэффициенты
        final var sourceCoefficients = item.getCoefficients();
        tariff.setCoefBicycle(sourceCoefficients.getGoods());
        tariff.setCoefOrg(sourceCoefficients.getOrganization());
        tariff.setCoefPetTransport(sourceCoefficients.getPet());
        tariff.setCoefChildSeat(sourceCoefficients.getChildren());
        tariff.setCoefTraffic(sourceCoefficients.getTraffic());

        final var targetTimedParams = tariff.getTimedTariffParams();
        targetTimedParams.setCoefWorkDayMorning(sourceCoefficients.getMorning());
        targetTimedParams.setCoefWorkDayNoon(sourceCoefficients.getDay());
        targetTimedParams.setCoefWorkDayEvening(sourceCoefficients.getEvening());
        targetTimedParams.setCoefWorkDayNight(sourceCoefficients.getNight());
        targetTimedParams.setCoefDayOff(sourceCoefficients.getWeekend());

        // совместные поездки
        final var targetCoopParams = tariff.getCoopTariffParams();
        final var sourceCoops = item.getCoops();
        targetCoopParams.setSavingsDeviationPct(sourceCoops.getSavingsDeviationPct());
        targetCoopParams.setDistanceDeviationKm(sourceCoops.getDistanceDeviationKm());
        targetCoopParams.setTimeDeviationMin((int) sourceCoops.getTimeDeviationMin());
        targetCoopParams.setMinCancelTimeMin((int) sourceCoops.getMinCancelTimeMin());
        // отклонения контрагента

        final var targetContractorParams = tariff.getContractorDeviationParams();
        final var sourceDeviations = item.getDeviations();
        targetContractorParams.setMaxDiffComputedCostPercent(sourceDeviations.getMaxDiffComputedCostPercent());
        targetContractorParams.setMaxDiffComputedDistancePercent(sourceDeviations.getMaxDiffComputedDistancePercent());
        targetContractorParams.setMaxDiffComputedWaitingPercent(sourceDeviations.getMaxDiffComputedWaitingPercent());
        targetContractorParams.setMaxDiffContractorCostPercent(sourceDeviations.getMaxDiffContractorCostPercent());
        targetContractorParams.setMaxDiffFactDistancePercent(sourceDeviations.getMaxDiffFactDistancePercent());
        
        attachDepartment(item, tariff);
        
        if (!TransportServiceType.EMPLOYEE_TRANSPORTATION.getDescription().equals(item.getServiceType())) {
            throw new ImportChangingValuesException(ImportChangingValuesException.TAXI_SERVICE_TYPE_CHANGING);
        }
        targetCoopParams.setMinCancelTimeMin((int) sourceIncludes.getMinimumCancelTime());
        tariff.setIsNightTariff(item.isNightTariff());

        tariffService.save(tariff);
        tariffSender.send(tariff, !tariff.isActive());
    }
    
    private void attachDepartment(TaxiFileDto item, TaxiTariff tariff) {
        if (StringUtils.hasText(item.getDepartmentHumanReadableId())) {
            tariffService.getDepartmentByHumanReadableId(item.getDepartmentHumanReadableId())
                         .ifPresent(department -> {
                             if (item.getId() == null || item.getId().isEmpty()) {
                                 tariffService.findByDepartmentIdAndTransportTypeAndTaxiClassAndRegionIdAndActive(
                                                      department.getId(), tariff.getTransportType(), tariff.getTaxiClass(), tariff.getRegionId(),
                                                      tariff.isActive())
                                              .filter(foundTariff -> foundTariff.isActive() &&
                                                                     tariff.isActive() == tariffService.getById(foundTariff.getId()).isActive()
                                                                     && tariff.getRegionId()
                                                                              .equals(tariffService.getById(foundTariff.getId()).getRegionId()))
                                              .ifPresentOrElse(foundTariff -> {
                                                  throw new TariffAlreadyExistsException(
                                                          TariffAlreadyExistsException.TARIFF_COMBO_FORMAT,
                                                          "департамент, тип транспорта, класс такси, регион. Идентификатор тарифа с совпадающими данными: " +
                                                          foundTariff.getId());
                                              }, () -> tariff.setDepartment(department));
                             } else {
                                 tariffService.findById(tariff.getId()).ifPresentOrElse(foundTariff -> {
                                     if (!department.getId().equals(foundTariff.getDepartment().getId())) {
                                         throw new ImportChangingValuesException(ImportChangingValuesException.DEPARTMENT_CHANGING);
                                     }
                                 }, () -> {
                                     throw new TariffLogicException("Тариф с ID " + item.getId() + " не найден");
                                 });
                             }
                         });
        }
    }
    
    private Contractor getContractor(TaxiFileDto item, Contract contract) {
        var contractor = contractorService.get(contract.getContractorId());
        if (contractor.isEmpty() || !contractor.get().getName().equals(item.getContractor())) {
            throw new TariffLogicException("Контрагент с наименованием %s не найден".formatted(item.getContractor()));
        }
        return contractor.get();
    }
    
    private TaxiTariff getTariff(TaxiFileDto item, Organization organization, Contract contract) {
        var tariff = TaxiTariff.builder().build();
        if (StringUtils.hasText(item.getId()) && !item.isActive()) {
            tariff = getTariff(item);
        } else {
            tariff.setContract(contract);
            tariff.setOrganization(organization);
            var geozone = geoZoneRepository.findByName(item.getRegion()).orElseThrow(
                    () -> new TariffLogicException("Регион с наименованием %s не найден".formatted(item.getRegion())));
            tariff.setRegionId(geozone == null ? null : geozone.getId());
            tariff.setTaxiClass(TaxiClass.getByRusName(item.getTaxiClass())
                                         .orElseThrow(() -> new NoSuchElementException(
                                                 String.format(VALUE, item.getTaxiClass()))));
        }
        return tariff;
    }
    
    @SuppressWarnings("java:S3776")
    private void checkContract(TaxiFileDto item, Contract contract, TaxiTariff tariff, Contractor contractor) {
        if (item.getId() == null || item.getId().isEmpty()) {
            if (!contract.isActive()) {
                throw new TariffLogicException("Статус по договору №" + contract.getContractNumber() + " не активен");
            }
            if (!LocalDate.now().isBefore(contract.getEndDate())) {
                throw new TariffLogicException("Срок действия договора №" + contract.getContractNumber() + " закончен");
            }
            if (!contractor.isActive()) {
                throw new TariffLogicException("Статус по контрагенту " + contractor.getName() + " не активен");
            }
            if (item.getWorkGroup() != null && !item.getWorkGroup().isEmpty()
                && !contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                if (item.getWorkGroup()
                        .equals(item.getOrganization() + "/" + WORK_GROUP_VARIABLE + "/" + item.getRegion() + "/" + item.getContract())) {
                    tariff.setWorkGroup(item.getOrganization() + "/" + WORK_GROUP_VARIABLE + "/" + item.getRegion() + "/" + item.getContract());
                } else {
                    throw new ImportChangingValuesException(ImportChangingValuesException.INCORRECT_WORK_GROUP_CHANGING);
                }
            }
            if (item.getWorkGroup() != null && !item.getWorkGroup().isEmpty()
                && contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                if (item.getWorkGroup()
                        .equals(item.getOrganization() + "/" + WORK_GROUP_VARIABLE + "/" + item.getRegion() + "/" + item.getContract())) {
                    throw new ImportChangingValuesException(ImportChangingValuesException.INCORRECT_WORK_GROUP_CHANGING);
                } else {
                    tariff.setWorkGroup(item.getWorkGroup()); //Заменить на установку рабочей группы в соответствии с шаблоном,
                    // когда будет готова интеграция по универсальному API
                }
            }
            if (item.getWorkGroup() == null || item.getWorkGroup().isEmpty()) {
                if (!contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                    tariff.setWorkGroup(item.getOrganization() + "/" + WORK_GROUP_VARIABLE + "/" + item.getRegion() + "/" + item.getContract());
                }
                if (contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                    tariff.setWorkGroup(item.getWorkGroup()); //Заменить на установку рабочей группы в соответствии с шаблоном,
                    // когда будет готова интеграция по универсальному API
                }
            }
        } else {
            if (!item.getContract().equals(tariff.getContract().getContractNumber())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.CONTRACT_NUMBER_CHANGING);
            }
            if (!item.getOrganization().equals(tariff.getOrganization().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.ORGANIZATION_NAME_CHANGING);
            }
            if (!item.getTaxiClass().equals(tariff.getTaxiClass().getRusName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.TAXI_CLASS_CHANGING);
            }
            if (!contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())
                &&
                !item.getWorkGroup().equals(item.getOrganization() + "/" + WORK_GROUP_VARIABLE + "/" + item.getRegion() + "/" + item.getContract())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.INCORRECT_WORK_GROUP_CHANGING);
            }
            if (contractor.getIntegrationType().equals(TaxiExternalIntegrationType.JSON_API_1_0.name())) {
                tariff.setWorkGroup(item.getWorkGroup()); //Заменить на проверку рабочей группы в соответствии с шаблоном,
                // когда будет готова интеграция по универсальному API
            }
        }
    }
    
    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.TAXI;
    }
    
    @Override
    protected Class<TaxiTariff> tariffClass() {
        return TaxiTariff.class;
    }
    
    @Override
    protected TaxiFileDto newItem() {
        return new TaxiFileDto();
    }
    
    @Override
    protected void exportMapping(TaxiFileDto resItem, TaxiTariff item) {
        final var costSuburb = new CostFileDto();
        final var targetCost = resItem.getCost();
        targetCost.setSuburb(costSuburb);
        
        var costInterregional = new CostFileDto();
        targetCost.setInterregional(costInterregional);
        
        if (item.getDepartment() != null) {
            var department = tariffService.getDepartmentById(item.getDepartment().getId());
            department.ifPresent(value -> resItem.setDepartmentHumanReadableId(value.getHumanReadableId()));
        }
        resItem.setOrganization(Optional.ofNullable(item.getOrganization()).map(Organization::getName).orElse(null));
        geoZoneRepository.findById(item.getRegionId())
                         .map(GeoZone::getName)
                         .ifPresent(resItem::setRegion);
        var contract = item.getContract();
        if (contract != null) {
            resItem.setContract(contract.getContractNumber());
            var contractor = contractorService.get(contract.getContractorId());
            contractor.ifPresent(contractor1 -> resItem.setContractor(contractor1.getName()));
        }
        resItem.setTaxiClass(item.getTaxiClass().getRusName());
        resItem.setWorkGroup(item.getWorkGroup());
        resItem.setServiceType(item.getTransportType().getRusName());
        // в тарифе
        final var targetIncludes = resItem.getIncludes();
        targetIncludes.setDistance(item.getDistanceIncluded());
        targetIncludes.setTime(item.getTimeIncluded());
        targetIncludes.setWaitingTime(item.getFreeWaitingTime());
        targetIncludes.setMinimumDistance(item.getMinRideDistanceCost());
        targetIncludes.setMinimumTime(item.getMinRideTimeCost());
        var coopTariffParams = item.getCoopTariffParams();
        if (coopTariffParams != null) {
            targetIncludes.setMinimumCancelTime(coopTariffParams.getMinCancelTimeMin());
            // отклонение
            final var targetCoops = resItem.getCoops();
            targetCoops.setSavingsDeviationPct(coopTariffParams.getSavingsDeviationPct());
            targetCoops.setDistanceDeviationKm(coopTariffParams.getDistanceDeviationKm());
            targetCoops.setTimeDeviationMin(coopTariffParams.getTimeDeviationMin());
            targetCoops.setMinCancelTimeMin(coopTariffParams.getMinCancelTimeMin());
        }
        // стоимость
        targetCost.setDistance(item.getRideCostPerKm());
        targetCost.setTime(item.getRideCostPerMin());
        targetCost.setWaitTime(item.getWaitCostPerMin());
        targetCost.setWaitIntermediateTime(item.getWaitCostPerMinIntermediate());
        // за городом
        var suburbTariffParams = item.getSuburbTariffParams();
        if (suburbTariffParams != null) {
            costSuburb.setDistance(suburbTariffParams.getCostPerKmSuburb());
            costSuburb.setTime(suburbTariffParams.getCostPerMinSuburb());
            costSuburb.setSubmissionDistance(suburbTariffParams.getSuburbServiceCostPerKm());
            costSuburb.setSubmissionTime(suburbTariffParams.getSuburbServiceCostPerMin());
            // между регионами
            costInterregional.setDistance(suburbTariffParams.getCostPerKmInterRegion());
            costInterregional.setTime(suburbTariffParams.getCostPerMinInterRegion());
        }
        // коэффициенты
        final var targetCoefficients = resItem.getCoefficients();
        targetCoefficients.setOversized(item.getCoefBicycle());
        targetCoefficients.setOrganization(item.getCoefOrg());
        targetCoefficients.setPet(item.getCoefPetTransport());
        targetCoefficients.setChildren(item.getCoefChildSeat());
        targetCoefficients.setTraffic(item.getCoefTraffic());
        var timedTariffParams = item.getTimedTariffParams();
        if (timedTariffParams != null) {
            targetCoefficients.setMorning(timedTariffParams.getCoefWorkDayMorning());
            targetCoefficients.setDay(timedTariffParams.getCoefWorkDayNoon());
            targetCoefficients.setEvening(timedTariffParams.getCoefWorkDayEvening());
            targetCoefficients.setNight(timedTariffParams.getCoefWorkDayNight());
            targetCoefficients.setWeekend(timedTariffParams.getCoefDayOff());
        }
        final var contractorDeviationParams = item.getContractorDeviationParams();
        final var targetDeviations = resItem.getDeviations();
        if (Objects.nonNull(contractorDeviationParams)) {
            targetDeviations
                   .setMaxDiffComputedCostPercent(contractorDeviationParams.getMaxDiffComputedCostPercent());
            targetDeviations.setMaxDiffComputedDistancePercent(
                    contractorDeviationParams.getMaxDiffComputedDistancePercent());
            targetDeviations
                   .setMaxDiffComputedWaitingPercent(contractorDeviationParams.getMaxDiffComputedWaitingPercent());
            targetDeviations
                   .setMaxDiffContractorCostPercent(contractorDeviationParams.getMaxDiffContractorCostPercent());
            targetDeviations
                   .setMaxDiffFactDistancePercent(contractorDeviationParams.getMaxDiffFactDistancePercent());
        } else {
            targetDeviations.setMaxDiffComputedCostPercent(0);
            targetDeviations.setMaxDiffComputedDistancePercent(0);
            targetDeviations.setMaxDiffComputedWaitingPercent(0);
            targetDeviations.setMaxDiffContractorCostPercent(0);
            targetDeviations.setMaxDiffFactDistancePercent(0);
        }
        resItem.setNightTariff(item.getIsNightTariff());
    }
}
