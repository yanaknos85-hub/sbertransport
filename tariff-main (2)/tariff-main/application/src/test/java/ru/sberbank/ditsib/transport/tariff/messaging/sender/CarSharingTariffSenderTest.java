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
import ru.sber.transport.tariff.messaging.CarSharingTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.model.CarSharingTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.TimedTariffParams;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@DisplayName("Проверка отправки тарифов для каршеринга в Kafka")
@ActiveProfiles({"test", "kafka"})
class CarSharingTariffSenderTest extends KafkaTest {
    
    @Autowired
    private CarSharingTariffSender sender;
    
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
    
    private CarSharingTariff create() {
        var department = Department.builder()
                                   .id(UUID.randomUUID())
                                   .build();
        
        var organization = Organization.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        var contract = Contract.builder()
                               .id(UUID.randomUUID())
                               .build();
    
        var timedTariffParams =
                TimedTariffParams.builder()
                                 .coefWorkDayMorning(19)
                                 .coefWorkDayNoon(20)
                                 .coefWorkDayEvening(21)
                                 .coefWorkDayNight(22)
                                 .coefDayOff(23)
                                 .build();
        
        return CarSharingTariff.builder()
                               .id(UUID.randomUUID())
                               .humanReadableId(UUID.randomUUID().toString())
                               .department(department)
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .organization(organization)
                               .region(UUID.randomUUID().toString())
                               .regionId(UUID.randomUUID())
                               .transportType(TransportTypeEnum.CARSHARING)
                               .active(true)
                               .contract(contract)
                               .rideCostPerKm(1)
                               .rideCostPerMin(2)
                               .waitCostPerMin(3)
                               .timedTariffParams(timedTariffParams)
                               .coefTraffic(5)
                               .coefChildSeat(6)
                               .coefPetTransport(7)
                               .coefCasko(8)
                               .build();
    }
    
    private void test(CarSharingTariff tariff, boolean deleted) {
        var actual = consumeMessage("service.tariff.carSharing", CarSharingTariffMessage.class);
        
        assertEquals(actual.getId(), tariff.getId());
        assertEquals(actual.humanReadableId(), tariff.getHumanReadableId());
        assertEquals(actual.departmentId(), tariff.getDepartment().getId());
        assertEquals(actual.serviceType(), tariff.getServiceType().name());
        assertEquals(actual.organizationId(), tariff.getOrganization().getId());
        assertEquals(actual.region(), tariff.getRegion());
        assertEquals(actual.regionId(), tariff.getRegionId());
        assertEquals(actual.transportType(), tariff.getTransportType().getName());
        assertEquals(actual.active(), tariff.isActive());
    
        assertEquals(actual.contractId(), tariff.getContract().getId());
        
        assertEquals(actual.rideCostPerKm(), tariff.getRideCostPerKm());
        assertEquals(actual.rideCostPerMin(), tariff.getRideCostPerMin());
        assertEquals(actual.waitCostPerMin(), tariff.getWaitCostPerMin());
        assertEquals(actual.coefWorkDayMorning(), tariff.getTimedTariffParams().getCoefWorkDayMorning());
        assertEquals(actual.coefWorkDayNoon(), tariff.getTimedTariffParams().getCoefWorkDayNoon());
        assertEquals(actual.coefWorkDayEvening(), tariff.getTimedTariffParams().getCoefWorkDayEvening());
        assertEquals(actual.coefWorkDayNight(), tariff.getTimedTariffParams().getCoefWorkDayNight());
        assertEquals(actual.coefDayOff(), tariff.getTimedTariffParams().getCoefDayOff());
        assertEquals(actual.coefTraffic(), tariff.getCoefTraffic());
        assertEquals(actual.coefChildSeat(), tariff.getCoefChildSeat());
        assertEquals(actual.coefPetTransport(), tariff.getCoefPetTransport());
        assertEquals(actual.coefCasko(), tariff.getCoefCasko());
        
        assertEquals(actual.deleted(), deleted);
    }
    
}