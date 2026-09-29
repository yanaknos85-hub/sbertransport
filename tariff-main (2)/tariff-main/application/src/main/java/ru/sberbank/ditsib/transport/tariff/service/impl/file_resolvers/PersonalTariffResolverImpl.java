package ru.sberbank.ditsib.transport.tariff.service.impl.file_resolvers;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.files.*;
import ru.sberbank.ditsib.transport.tariff.exceptions.ImportChangingValuesException;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.messaging.sender.TariffSender;
import ru.sberbank.ditsib.transport.tariff.service.OrganizationService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Получатель данных из файла о тарифе личного транспорта.
 */
@Slf4j
@Component
public class PersonalTariffResolverImpl extends BaseTariffResolverImpl<PersonalTariff, PersonalFileDto> {
    
    private final TariffService tariffService;
    
    private final TariffSender tariffSender;
    
    private final GeoZoneRepository geoZoneRepository;
    
    public PersonalTariffResolverImpl(
            TariffService tariffService, TariffSender tariffSender, OrganizationRepository organizationRepository, GeoZoneRepository geoZoneRepository,
            TariffSearchDTOMapper tariffSearchDTOMapper
                                     ) {
        super(tariffService, organizationRepository, tariffSearchDTOMapper);
        this.tariffService = tariffService;
        this.tariffSender = tariffSender;
        this.geoZoneRepository = geoZoneRepository;
    }

    @Override
    protected PersonalTariff newTariff() {
        return new PersonalTariff();
    }

    @Override
    public void importData(PersonalFileDto item, @NotNull Map<String, ?> params, @NotNull JwtAuthenticationToken authenticationToken) {
        final var organization = getOrganization(item.getOrganization());
        final PersonalTariff tariff;
        if (StringUtils.hasText(item.getId()) && !item.isActive()) {
            tariff = getTariff(item);
            if (!item.getOrganization().equals(tariff.getOrganization().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.ORGANIZATION_NAME_CHANGING);
            }
            if (!item.getRegion().equals(geoZoneRepository.findById(tariff.getRegionId()).orElseThrow().getName())) {
                throw new ImportChangingValuesException(ImportChangingValuesException.REGION_NAME_CHANGING);
            }
        } else {
            tariff = new PersonalTariff();
            tariff.setOrganization(organization);
            final var geozone = geoZoneRepository.findByName(item.getRegion()).orElseThrow(() -> new EntityNotFoundException(GeoZone.class, item.getRegion()));
            tariff.setRegionId(geozone == null ? null : geozone.getId());
        }
        tariff.setActive(item.isActive());
        tariff.setServiceType(TransportServiceType.EMPLOYEE_TRANSPORTATION);
        tariff.setTransportType(TransportTypeEnum.PERSONAL);
        // В тарифе
        final var cost = item.getCost();
        tariff.setDistanceIncluded(cost.getDistance() > 0 ? cost.getMinRideDistance() / cost.getDistance() : 0);
        tariff.setTimeIncluded(cost.getTime() > 0 ? (int) cost.getMinRideTime() / (int) cost.getTime() : 0);
        // стоимость
        tariff.setMinRideDistanceCost((int) cost.getMinRideDistance());
        tariff.setMinRideTimeCost((int) cost.getMinRideTime());
        tariff.setRideCostPerKm((int) cost.getDistance());
        tariff.setRideCostPerMin((int) cost.getTime());
        tariff.setWaitCostPerMinIntermediate((int) cost.getWaitIntermediateTime());
        // за городом
        final var targetSuburb = tariff.getSuburbTariffParams();
        final var sourceSuburb = cost.getSuburb();
        targetSuburb.setCostPerKmSuburb((int) sourceSuburb.getDistance());
        targetSuburb.setCostPerMinSuburb((int) sourceSuburb.getTime());
        targetSuburb.setSuburbServiceCostPerKm((int) sourceSuburb.getSubmissionDistance());
        targetSuburb.setSuburbServiceCostPerMin((int) sourceSuburb.getSubmissionTime());
        // между регионами
        final var interregional = cost.getInterregional();
        targetSuburb.setCostPerKmInterRegion((int) interregional.getDistance());
        targetSuburb.setCostPerMinInterRegion((int) interregional.getTime());
        // коэффициенты
        final var sourceCoefficients = item.getCoefficients();
        tariff.setCoefMaterialAssets(sourceCoefficients.getGoods());
        tariff.setCoefTraffic(sourceCoefficients.getTraffic());

        final var targetTimedTariffParams = tariff.getTimedTariffParams();
        targetTimedTariffParams.setCoefWorkDayMorning(sourceCoefficients.getMorning());
        targetTimedTariffParams.setCoefWorkDayNoon(sourceCoefficients.getDay());
        targetTimedTariffParams.setCoefWorkDayEvening(sourceCoefficients.getEvening());
        targetTimedTariffParams.setCoefWorkDayNight(sourceCoefficients.getNight());
        targetTimedTariffParams.setCoefDayOff(sourceCoefficients.getWeekend());
        tariff.setTrustIdx(sourceCoefficients.getTrustIdx());
        // сезон
        final var sourceSeason = sourceCoefficients.getSeason();
        tariff.setSeasonalCoefficient(sourceSeason.getValue());
        tariff.setSeasonStart(sourceSeason.getStart());
        tariff.setSeasonEnd(sourceSeason.getEnd());
        // двигатель
        final var targetEngineTariffParams = tariff.getEngineTariffParams();
        final var sourceEngine = sourceCoefficients.getEngine();
        targetEngineTariffParams.setCoefEngine1_6(sourceEngine.getSmall());
        targetEngineTariffParams.setCoefEngine1_6_to_2_0(sourceEngine.getMedium());
        targetEngineTariffParams.setCoefEngine2_0_to_2_5(sourceEngine.getLarge());
        // отклонение
        final var sourceCoop = tariff.getCoopTariffParams();
        final var targetCoop = item.getCoops();
        sourceCoop.setSavingsDeviationPct(targetCoop.getSavingsDeviationPct());
        sourceCoop.setDistanceDeviationKm(targetCoop.getDistanceDeviationKm());
        sourceCoop.setTimeDeviationMin((int) targetCoop.getTimeDeviationMin());
        sourceCoop.setMinCancelTimeMin((int) targetCoop.getMinCancelTimeMin());

        final var saved = tariffService.save(tariff);
        tariffSender.send(saved, !saved.isActive());
    }
    
    @Override
    protected TransportTypeEnum transportType() {
        return TransportTypeEnum.PERSONAL;
    }
    
    @Override
    protected Class<PersonalTariff> tariffClass() {
        return PersonalTariff.class;
    }
    
    @Override
    protected PersonalFileDto newItem() {
        return new PersonalFileDto();
    }
    
    @Override
    protected void exportMapping(PersonalFileDto resItem, PersonalTariff item) {
        final var costSuburb = new CostFileDto();
        final var targetCost = resItem.getCost();
        targetCost.setSuburb(costSuburb);
        
        final var costInterregional = new CostFileDto();
        resItem.setOrganization(Optional.ofNullable(item.getOrganization()).map(Organization::getName).orElse(null));
        geoZoneRepository.findById(item.getRegionId())
                         .map(GeoZone::getName)
                         .ifPresent(resItem::setRegion);
        targetCost.setInterregional(costInterregional);
        resItem.setServiceType(item.getServiceType().getDescription());
        
        // стоимость
        fillCosts(resItem, item);
        // за городом
        fillSuburb(costSuburb, item);
        fillInterregional(costInterregional, item);
        fillCoefficients(resItem, item);
        fillIncludes(resItem, item);
        fillEngineCoefficients(resItem, item);
        fillDeviations(resItem, item);
    }
    
    private void fillCosts(PersonalFileDto target, PersonalTariff source) {
        final var targetCost = target.getCost();
        targetCost.setMinRideDistance(source.getMinRideDistanceCost());
        targetCost.setMinRideTime(source.getMinRideTimeCost());
        targetCost.setDistance(source.getRideCostPerKm());
        targetCost.setTime(source.getRideCostPerMin());
        targetCost.setWaitIntermediateTime(source.getWaitCostPerMinIntermediate());
    }
    
    private void fillSuburb(CostFileDto target, PersonalTariff source) {
        final var suburbTariffParams = source.getSuburbTariffParams();
        target.setDistance(suburbTariffParams.getCostPerKmSuburb());
        target.setTime(suburbTariffParams.getCostPerMinSuburb());
        target.setSubmissionDistance(suburbTariffParams.getSuburbServiceCostPerKm());
        target.setSubmissionTime(suburbTariffParams.getSuburbServiceCostPerMin());
    }
    
    private void fillInterregional(CostFileDto target, PersonalTariff source) {
        final var suburbTariffParams = source.getSuburbTariffParams();
        target.setDistance(suburbTariffParams.getCostPerKmInterRegion());
        target.setTime(suburbTariffParams.getCostPerMinInterRegion());
    }
    
    private void fillCoefficients(PersonalFileDto target, PersonalTariff source) {
        final var coefficients = target.getCoefficients();
        coefficients.setGoods(source.getCoefMaterialAssets());
        coefficients.setTraffic(source.getCoefTraffic());

        final var timedTariffParams = source.getTimedTariffParams();
        coefficients.setMorning(timedTariffParams.getCoefWorkDayMorning());
        coefficients.setDay(timedTariffParams.getCoefWorkDayNoon());
        coefficients.setEvening(timedTariffParams.getCoefWorkDayEvening());
        coefficients.setNight(timedTariffParams.getCoefWorkDayNight());
        coefficients.setWeekend(timedTariffParams.getCoefDayOff());

        final var season = coefficients.getSeason();
        season.setValue(source.getSeasonalCoefficient());
        season.setStart(source.getSeasonStart());
        season.setEnd(source.getSeasonEnd());
        coefficients.setTrustIdx(source.getTrustIdx());
    }
    
    private void fillEngineCoefficients(PersonalFileDto target, PersonalTariff source) {
        final var targetEngine = target.getCoefficients().getEngine();
        final var sourceEngine = source.getEngineTariffParams();
        targetEngine.setSmall(sourceEngine.getCoefEngine1_6());
        targetEngine.setMedium(sourceEngine.getCoefEngine1_6_to_2_0());
        targetEngine.setLarge(sourceEngine.getCoefEngine2_0_to_2_5());
    }
    
    private void fillIncludes(PersonalFileDto target, PersonalTariff source) {
        final var includes = target.getIncludes();
        includes.setDistance(source.getDistanceIncluded());
        includes.setTime(source.getTimeIncluded());
    }
    
    private void fillDeviations(PersonalFileDto target, PersonalTariff source) {
        // отклонение
        final var targetCoops = target.getCoops();
        final var sourceParams = source.getCoopTariffParams();
        if (sourceParams != null) {
            targetCoops.setSavingsDeviationPct(sourceParams.getSavingsDeviationPct());
            targetCoops.setDistanceDeviationKm(sourceParams.getDistanceDeviationKm());
            targetCoops.setTimeDeviationMin(sourceParams.getTimeDeviationMin());
            targetCoops.setMinCancelTimeMin(sourceParams.getMinCancelTimeMin());
        }
    }
}
