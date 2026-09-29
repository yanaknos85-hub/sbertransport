package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка отправки тарифов для личного транспорта в Kafka")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class PersonalTariffSenderTest extends KafkaTest {
    
    @Autowired
    private PersonalTariffSender sender;
    
    @Test
    @DisplayName("Отправка")
    void test() {
        var tariff = create();
        sender.send(tariff);
        test(tariff, false);
    }
    
    @Test
    @DisplayName("Отправка удаления")
    void test_deleted() {
        var tariff = create();
        sender.sendDeleted(tariff);
        test(tariff, true);
    }
    
    private PersonalTariff create() {
        var department = Department.builder()
                                   .id(UUID.randomUUID())
                                   .build();
        
        var organization = Organization.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        var suburbTariffParams =
                SuburbTariffParams.builder()
                                  .costPerKmSuburb(9)
                                  .costPerMinSuburb(10)
                                  .suburbServiceCostPerKm(11)
                                  .suburbServiceCostPerMin(12)
                                  .costPerKmInterRegion(13)
                                  .costPerMinInterRegion(14)
                                  .build();
        
        var engineTariffParams =
                EngineTariffParams.builder()
                                  .coefEngine1_6(16)
                                  .coefEngine1_6_to_2_0(17)
                                  .coefEngine2_0_to_2_5(18)
                                  .build();
        
        var timedTariffParams =
                TimedTariffParams.builder()
                                 .coefWorkDayMorning(19)
                                 .coefWorkDayNoon(20)
                                 .coefWorkDayEvening(21)
                                 .coefWorkDayNight(22)
                                 .coefDayOff(23)
                                 .build();
        
        var coopTariffParams =
                CoopTariffParams.builder()
                                .savingsDeviationPct(26)
                                .distanceDeviationKm(27)
                                .timeDeviationMin(28)
                                .minCancelTimeMin(29)
                                .build();
        
        return PersonalTariff.builder()
                             .id(UUID.randomUUID())
                             .humanReadableId(UUID.randomUUID().toString())
                             .department(department)
                             .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                             .organization(organization)
                             .region(UUID.randomUUID().toString())
                             .regionId(UUID.randomUUID())
                             .transportType(TransportTypeEnum.CARSHARING)
                             .active(true)
                             .rideCostPerKm(1)
                             .rideCostPerMin(2)
                             .timeIncluded(3)
                             .distanceIncluded(4)
                             .minRideDistanceCost(5)
                             .minRideTimeCost(6)
                             .waitCostPerMin(7)
                             .waitCostPerMinIntermediate(8)
                             .suburbTariffParams(suburbTariffParams)
                             .seasonalCoefficient(15)
                             .seasonStart(LocalDate.now())
                             .seasonEnd(LocalDate.now().plusDays(1))
                             .engineTariffParams(engineTariffParams)
                             .timedTariffParams(timedTariffParams)
                             .coefTraffic(24)
                             .coefMaterialAssets(25)
                             .coopTariffParams(coopTariffParams)
                             .trustIdx(26)
                             .build();
    }
    
    private void test(PersonalTariff tariff, boolean deleted) {
        var actual = consumeMessage("service.tariff.personal", PersonalTariffMessage.class);
        
        assertEquals(actual.getId(), tariff.getId());
        assertEquals(actual.humanReadableId(), tariff.getHumanReadableId());
        assertEquals(actual.departmentId(), tariff.getDepartment().getId());
        assertEquals(actual.serviceType(), tariff.getServiceType().name());
        assertEquals(actual.organizationId(), tariff.getOrganization().getId());
        assertEquals(actual.region(), tariff.getRegion());
        assertEquals(actual.regionId(), tariff.getRegionId());
        assertEquals(actual.transportType(), tariff.getTransportType().getName());
        assertEquals(actual.active(), tariff.isActive());
        
        assertEquals(actual.rideCostPerKm(), tariff.getRideCostPerKm());
        assertEquals(actual.rideCostPerMin(), tariff.getRideCostPerMin());
        assertEquals(actual.timeIncluded(), tariff.getTimeIncluded());
        assertEquals(actual.distanceIncluded(), tariff.getDistanceIncluded());
        assertEquals(actual.minRideDistanceCost(), tariff.getMinRideDistanceCost());
        assertEquals(actual.minRideTimeCost(), tariff.getMinRideTimeCost());
        assertEquals(actual.waitCostPerMin(), tariff.getWaitCostPerMin());
        assertEquals(actual.waitCostPerMinIntermediate(), tariff.getWaitCostPerMinIntermediate());
        assertEquals(actual.costPerKmSuburb(), tariff.getSuburbTariffParams().getCostPerKmSuburb());
        assertEquals(actual.costPerMinSuburb(), tariff.getSuburbTariffParams().getCostPerMinSuburb());
        assertEquals(actual.suburbServiceCostPerKm(), tariff.getSuburbTariffParams().getSuburbServiceCostPerKm());
        assertEquals(actual.suburbServiceCostPerMin(), tariff.getSuburbTariffParams().getSuburbServiceCostPerMin());
        assertEquals(actual.costPerKmInterRegion(), tariff.getSuburbTariffParams().getCostPerKmInterRegion());
        assertEquals(actual.costPerMinInterRegion(), tariff.getSuburbTariffParams().getCostPerMinInterRegion());
        assertEquals(actual.seasonalCoefficient(), tariff.getSeasonalCoefficient());
        assertEquals(actual.seasonStart(), tariff.getSeasonStart());
        assertEquals(actual.seasonEnd(), tariff.getSeasonEnd());
        assertEquals(actual.coefEngine1_6(), tariff.getEngineTariffParams().getCoefEngine1_6());
        assertEquals(actual.coefEngine1_6_to_2_0(), tariff.getEngineTariffParams().getCoefEngine1_6_to_2_0());
        assertEquals(actual.coefEngine2_0_to_2_5(), tariff.getEngineTariffParams().getCoefEngine2_0_to_2_5());
        assertEquals(actual.coefWorkDayMorning(), tariff.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(actual.coefWorkDayNoon(), tariff.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(actual.coefWorkDayEvening(), tariff.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(actual.coefWorkDayNight(), tariff.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(actual.coefDayOff(), tariff.getTimedTariffParams().getCoefDayOff());
        assertEquals(actual.coefTraffic(), tariff.getCoefTraffic());
        assertEquals(actual.coefMaterialAssets(), tariff.getCoefMaterialAssets());
        assertEquals(actual.savingsDeviationPct(), tariff.getCoopTariffParams().getSavingsDeviationPct());
        assertEquals(actual.distanceDeviationKm(), tariff.getCoopTariffParams().getDistanceDeviationKm());
        assertEquals(actual.timeDeviationMin(), tariff.getCoopTariffParams().getTimeDeviationMin());
        assertEquals(actual.minCancelTimeMin(), tariff.getCoopTariffParams().getMinCancelTimeMin());
        assertEquals(actual.trustIdx(), tariff.getTrustIdx());
        
        assertEquals(actual.deleted(), deleted);
    }
}