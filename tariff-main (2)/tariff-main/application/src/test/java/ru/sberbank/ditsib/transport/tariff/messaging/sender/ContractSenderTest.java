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
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.dto.ContractForKafkaMessage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka", "kafka-avro"})
@DisplayName("Проверка отправки контрактов в Kafka")
class ContractSenderTest extends KafkaTest {

    @Autowired
    private ContractSender sender;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Test
    @DisplayName("Отправка")
    void test() {
        GeoZone savedGeozone = geoZoneRepository.save(new GeoZone(UUID.randomUUID(), "Московская обл", 1 + "", null));
        var contract = new Contract();
        contract = contract.toBuilder()
                .id(UUID.randomUUID())
                .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                .regionIds(Set.of(savedGeozone.getId()))
                .organizations(
                        Collections.singleton(Organization.builder()
                                .id(UUID.randomUUID())
                                .active(true)
                                .build()))
                .contractorId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI)
                .active(true)
                .sum(500L)
                .creationTime(LocalDateTime.now().minusDays(4))
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .userId(UUID.randomUUID())
                .contractNumber(UUID.randomUUID().toString())
                .driverLatePickupPenalty(BigDecimal.valueOf(0.01))
                .poorServiceQualityPenalty(BigDecimal.valueOf(0.01))
                .driverOrderCancellationPenalty(BigDecimal.valueOf(0.01))
                .build();

        sender.send(contract, false);

        var actual = consumeMessage("service.contract", ContractForKafkaMessage.class);

        assertThat(actual.getId()).isEqualTo(contract.getId());
        assertThat(actual.serviceType()).isEqualTo(contract.getServiceType().toString());
        assertThat(actual.contractorId()).isEqualTo(contract.getContractorId());
        assertThat(actual.transportType()).isEqualTo(contract.getTransportType().getName());
        assertThat(actual.active()).isEqualTo(contract.isActive());
        assertThat(actual.sum()).isEqualTo(contract.getSum());
        assertThat(actual.creationTime()).isEqualTo(contract.getCreationTime());
        assertThat(actual.startDate()).isEqualTo(contract.getStartDate());
        assertThat(actual.endDate()).isEqualTo(contract.getEndDate());
        assertThat(actual.userId()).isEqualTo(contract.getUserId());
        assertThat(actual.deleted()).isFalse();
        assertThat(actual.driverLatePickupPenalty()).isEqualTo(contract.getDriverLatePickupPenalty());
        assertThat(actual.poorServiceQualityPenalty()).isEqualTo(contract.getPoorServiceQualityPenalty());
        assertThat(actual.driverOrderCancellationPenalty()).isEqualTo(contract.getDriverOrderCancellationPenalty());
        assertThat(actual.responsibleEmployeeId()).isEqualTo(contract.getResponsibleEmployeeId());
    }

    @Test
    @DisplayName("Отправка удаления")
    void test_deleted() {
        var contract = new Contract();
        GeoZone savedGeozone = geoZoneRepository.save(new GeoZone(UUID.randomUUID(), "Московская обл", 1 + "", null));
        contract = contract.toBuilder()
                .id(UUID.randomUUID())
                .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                .regionIds(Set.of(savedGeozone.getId()))
                .organizations(
                        Collections.singleton(Organization.builder()
                                .id(UUID.randomUUID())
                                .active(true)
                                .build()))
                .contractorId(UUID.randomUUID())
                .transportType(TransportTypeEnum.TAXI)
                .active(true)
                .sum(500L)
                .creationTime(LocalDateTime.now().minusDays(4))
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .userId(UUID.randomUUID())
                .contractNumber(UUID.randomUUID().toString())
                .build();

        sender.send(contract, true);

        var actual = consumeMessage("service.contract", ContractForKafkaMessage.class);

        assertThat(actual.getId()).isEqualTo(contract.getId());
        assertThat(actual.serviceType()).isEqualTo(contract.getServiceType().toString());
        assertThat(actual.contractorId()).isEqualTo(contract.getContractorId());
        assertThat(actual.transportType()).isEqualTo(contract.getTransportType().getName());
        assertThat(actual.active()).isEqualTo(contract.isActive());
        assertThat(actual.sum()).isEqualTo(contract.getSum());
        assertThat(actual.creationTime()).isEqualTo(contract.getCreationTime());
        assertThat(actual.startDate()).isEqualTo(contract.getStartDate());
        assertThat(actual.endDate()).isEqualTo(contract.getEndDate());
        assertThat(actual.userId()).isEqualTo(contract.getUserId());
        assertThat(actual.deleted()).isTrue();
    }

}