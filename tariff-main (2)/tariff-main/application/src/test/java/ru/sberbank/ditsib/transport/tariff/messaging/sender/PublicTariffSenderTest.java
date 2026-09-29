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
import ru.sber.transport.tariff.messaging.PublicTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.PublicTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
@DisplayName("Проверка отправки тарифов для общественного транспорта в Kafka")
class PublicTariffSenderTest extends KafkaTest {
    
    @Autowired
    private PublicTariffSender sender;
    
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
    
    private PublicTariff create() {
        var department = Department.builder()
                                   .id(UUID.randomUUID())
                                   .build();
        
        var organization = Organization.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        return PublicTariff.builder()
                           .id(UUID.randomUUID())
                           .humanReadableId(UUID.randomUUID().toString())
                           .department(department)
                           .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                           .organization(organization)
                           .region(UUID.randomUUID().toString())
                           .regionId(UUID.randomUUID())
                           .transportType(TransportTypeEnum.CARSHARING)
                           .active(true)
                           .metroTicketCost(1)
                           .tramTicketCost(2)
                           .trolleybusTicketCost(3)
                           .busTicketCost(4)
                           .cityLocalTrainCost(5)
                           .metroAvailability(true)
                           .tramAvailability(false)
                           .trolleybusAvailability(true)
                           .busAvailability(false)
                           .cityLocalTrainAvailability(true)
                           .travelCardMetroCost(6)
                           .travelCardTramCost(7)
                           .travelCardTrolleybusCost(8)
                           .travelCardBusCost(9)
                           .travelCardLocalTrainCost(10)
                           .travelCardAllCityTransportCost(11)
                           .travelCardMetroAvailability(false)
                           .travelCardTramAvailability(true)
                           .travelCardTrolleybusAvailability(false)
                           .travelCardBusAvailability(true)
                           .travelCardLocalTrainAvailability(false)
                           .travelCardAllCityTransportAvailability(true)
                           .build();
    }
    
    private void test(PublicTariff tariff, boolean deleted) {
        var actual = consumeMessage("service.tariff.public", PublicTariffMessage.class);
        
        assertEquals(actual.getId(), tariff.getId());
        assertEquals(actual.humanReadableId(), tariff.getHumanReadableId());
        assertEquals(actual.departmentId(), tariff.getDepartment().getId());
        assertEquals(actual.serviceType(), tariff.getServiceType().name());
        assertEquals(actual.organizationId(), tariff.getOrganization().getId());
        assertEquals(actual.region(), tariff.getRegion());
        assertEquals(actual.regionId(), tariff.getRegionId());
        assertEquals(actual.transportType(), tariff.getTransportType().getName());
        assertEquals(actual.active(), tariff.isActive());
        
        assertEquals(actual.metroTicketCost(), tariff.getMetroTicketCost());
        assertEquals(actual.tramTicketCost(), tariff.getTramTicketCost());
        assertEquals(actual.trolleybusTicketCost(), tariff.getTrolleybusTicketCost());
        assertEquals(actual.cityLocalTrainCost(), tariff.getCityLocalTrainCost());
        assertEquals(actual.metroAvailability(), tariff.isMetroAvailability());
        assertEquals(actual.tramAvailability(), tariff.isTramAvailability());
        assertEquals(actual.trolleybusAvailability(), tariff.isTrolleybusAvailability());
        assertEquals(actual.cityLocalTrainAvailability(), tariff.isCityLocalTrainAvailability());
        assertEquals(actual.travelCardMetroCost(), tariff.getTravelCardMetroCost());
        assertEquals(actual.travelCardTramCost(), tariff.getTravelCardTramCost());
        assertEquals(actual.travelCardTrolleybusCost(), tariff.getTravelCardTrolleybusCost());
        assertEquals(actual.travelCardLocalTrainCost(), tariff.getTravelCardLocalTrainCost());
        assertEquals(actual.travelCardAllCityTransportCost(), tariff.getTravelCardAllCityTransportCost());
        assertEquals(actual.travelCardMetroAvailability(), tariff.isTravelCardMetroAvailability());
        assertEquals(actual.travelCardTramAvailability(), tariff.isTravelCardTramAvailability());
        assertEquals(actual.travelCardTrolleybusAvailability(), tariff.isTravelCardTrolleybusAvailability());
        assertEquals(actual.travelCardBusAvailability(), tariff.isTravelCardBusAvailability());
        assertEquals(actual.travelCardLocalTrainAvailability(), tariff.isTravelCardLocalTrainAvailability());
        assertEquals(actual.travelCardAllCityTransportAvailability(), tariff.isTravelCardAllCityTransportAvailability());
        
        assertEquals(actual.deleted(), deleted);
    }
    
}