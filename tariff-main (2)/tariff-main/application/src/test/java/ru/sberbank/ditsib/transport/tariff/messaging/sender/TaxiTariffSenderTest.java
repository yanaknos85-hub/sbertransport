package ru.sberbank.ditsib.transport.tariff.messaging.sender;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.ContractorDeviationsTariffParams;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.TaxiTariff;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
@DisplayName("Проверка отправки тарифов для такси в Kafka")
class TaxiTariffSenderTest extends KafkaTest {
    
    @Autowired
    private TaxiTariffSender sender;
    
    @Test
    @DisplayName("Отправка")
    void test() {
        var deltas = ContractorDeviationsTariffParams.builder()
                                                     .maxDiffComputedDistancePercent(10)
                                                     .maxDiffFactDistancePercent(12)
                                                     .maxDiffComputedCostPercent(14)
                                                     .maxDiffContractorCostPercent(16)
                                                     .maxDiffComputedWaitingPercent(20)
                                                     .build();
    
        var taxiTariff = new TaxiTariff();
        taxiTariff = taxiTariff.toBuilder()
                               .contractorTariffId("12345")
                               .id(UUID.randomUUID())
                               .humanReadableId("OOT-003-01")
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .region("Московская обл")
                               .regionId(UUID.randomUUID())
                               .organization(Organization.builder().id(UUID.randomUUID()).active(true).build())
                               .transportType(TransportTypeEnum.TAXI)
                               .contract(Contract.builder().id(UUID.randomUUID()).contractorId(UUID.randomUUID()).active(true).build())
                               .active(true)
                               .taxiClass(TaxiClass.ECONOMY)
                               .rideCostPerKm(56)
                               .minRideDistanceCost(60)
                               .rideCostPerMin(57)
                               .minRideTimeCost(61)
                               .waitCostPerMin(10)
                               .carServiceCost(60)
                               .waitCostPerMinIntermediate(25)
                               .contractorDeviationParams(deltas)
                               .workGroup("РГ")
                               .triggerTime(1000)
                               .build();
        
        sender.send(taxiTariff);
        
        var actual = consumeMessage("service.tariff.taxi", TaxiTariffMessage.class);
    
        assertThat(actual.getId()).isEqualTo(taxiTariff.getId());
        assertThat(actual.contractorTariffId()).isEqualTo(taxiTariff.getContractorTariffId());
        assertThat(actual.humanReadableId()).isEqualTo(taxiTariff.getHumanReadableId());
        assertThat(actual.serviceType()).isEqualTo(taxiTariff.getServiceType().toString());
        //assertThat(actual.getRegion()).isEqualTo(taxiTariff.getRegion());
        assertThat(actual.regionId()).isEqualTo(taxiTariff.getRegionId());
        assertThat(actual.organizationId()).isEqualTo(taxiTariff.getOrganization().getId());
        assertThat(actual.transportType()).isEqualTo(taxiTariff.getTransportType().getName());
        assertThat(actual.contractId()).isEqualTo(taxiTariff.getContract().getId());
        assertThat(actual.active()).isEqualTo(taxiTariff.isActive());
        assertThat(actual.taxiClass()).isEqualTo(taxiTariff.getTaxiClass().name());
        assertThat(actual.rideCostPerKm()).isEqualTo(taxiTariff.getRideCostPerKm());
        assertThat(actual.minRideDistanceCost()).isEqualTo(taxiTariff.getMinRideDistanceCost());
        assertThat(actual.rideCostPerMin()).isEqualTo(taxiTariff.getRideCostPerMin());
        assertThat(actual.minRideTimeCost()).isEqualTo(taxiTariff.getMinRideTimeCost());
        assertThat(actual.waitCostPerMin()).isEqualTo(taxiTariff.getWaitCostPerMin());
        assertThat(actual.carServiceCost()).isEqualTo(taxiTariff.getCarServiceCost());
        assertThat(actual.waitCostPerMinIntermediate()).isEqualTo(taxiTariff.getWaitCostPerMinIntermediate());
        assertThat(actual.maxDiffComputedDistancePercent()).isEqualTo(deltas.getMaxDiffComputedDistancePercent());
        assertThat(actual.maxDiffFactDistancePercent()).isEqualTo(deltas.getMaxDiffFactDistancePercent());
        assertThat(actual.maxDiffComputedCostPercent()).isEqualTo(deltas.getMaxDiffComputedCostPercent());
        assertThat(actual.maxDiffContractorCostPercent()).isEqualTo(deltas.getMaxDiffContractorCostPercent());
        assertThat(actual.maxDiffComputedWaitingPercent()).isEqualTo(deltas.getMaxDiffComputedWaitingPercent());
        assertEquals(taxiTariff.getWorkGroup(), actual.workGroup());
        assertNotNull(actual.contractorId());
        assertEquals(taxiTariff.getContract().getContractorId(),actual.contractorId());
        assertEquals(taxiTariff.getTriggerTime(),actual.triggerTime());
        assertThat(actual.deleted()).isFalse();
    }
    
    @Test
    @DisplayName("Отправка удаления")
    @Disabled("Требуется актуализация")
    void test_deleted() {
        var deltas = ContractorDeviationsTariffParams.builder()
                                                     .maxDiffComputedDistancePercent(10)
                                                     .maxDiffFactDistancePercent(12)
                                                     .maxDiffComputedCostPercent(14)
                                                     .maxDiffContractorCostPercent(16)
                                                     .maxDiffComputedWaitingPercent(20)
                                                     .build();
        
        var taxiTariff = new TaxiTariff();
        taxiTariff = taxiTariff.toBuilder()
                               .id(UUID.randomUUID())
                               .humanReadableId("OOT-003-01")
                               .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                               .region("Московская обл")
                               .regionId(UUID.randomUUID())
                               .organization(Organization.builder().id(UUID.randomUUID()).active(true).build())
                               .transportType(TransportTypeEnum.TAXI)
                               .contract(Contract.builder().id(UUID.randomUUID()).active(true).build())
                               .active(true)
                               .taxiClass(TaxiClass.ECONOMY)
                               .rideCostPerKm(56)
                               .minRideDistanceCost(60)
                               .rideCostPerMin(57)
                               .minRideTimeCost(61)
                               .waitCostPerMin(10)
                               .carServiceCost(60)
                               .waitCostPerMinIntermediate(25)
                               .contractorDeviationParams(deltas)
                               .triggerTime(1000)
                               .build();
    
        sender.sendDeleted(taxiTariff);
    
        var actual = consumeMessage("service.tariff.taxi", TaxiTariffMessage.class);
    
        assertThat(actual.getId()).isEqualTo(taxiTariff.getId());
        assertThat(actual.contractorTariffId()).isEqualTo(taxiTariff.getContractorTariffId());
        assertThat(actual.humanReadableId()).isEqualTo(taxiTariff.getHumanReadableId());
        assertThat(actual.serviceType()).isEqualTo(taxiTariff.getServiceType().toString());
        //assertThat(actual.getRegion()).isEqualTo(taxiTariff.getRegion());
        assertThat(actual.regionId()).isEqualTo(taxiTariff.getRegionId());
        assertThat(actual.organizationId()).isEqualTo(taxiTariff.getOrganization().getId());
        assertThat(actual.transportType()).isEqualTo(taxiTariff.getTransportType().getName());
        assertThat(actual.contractId()).isEqualTo(taxiTariff.getContract().getId());
        assertThat(actual.active()).isEqualTo(taxiTariff.isActive());
        assertThat(actual.taxiClass()).isEqualTo(taxiTariff.getTaxiClass().name());
        assertThat(actual.rideCostPerKm()).isEqualTo(taxiTariff.getRideCostPerKm());
        assertThat(actual.minRideDistanceCost()).isEqualTo(taxiTariff.getMinRideDistanceCost());
        assertThat(actual.rideCostPerMin()).isEqualTo(taxiTariff.getRideCostPerMin());
        assertThat(actual.minRideTimeCost()).isEqualTo(taxiTariff.getMinRideTimeCost());
        assertThat(actual.waitCostPerMin()).isEqualTo(taxiTariff.getWaitCostPerMin());
        assertThat(actual.carServiceCost()).isEqualTo(taxiTariff.getCarServiceCost());
        assertThat(actual.waitCostPerMinIntermediate()).isEqualTo(taxiTariff.getWaitCostPerMinIntermediate());
        assertThat(actual.maxDiffComputedDistancePercent()).isEqualTo(deltas.getMaxDiffComputedDistancePercent());
        assertThat(actual.maxDiffFactDistancePercent()).isEqualTo(deltas.getMaxDiffFactDistancePercent());
        assertThat(actual.maxDiffComputedCostPercent()).isEqualTo(deltas.getMaxDiffComputedCostPercent());
        assertThat(actual.maxDiffContractorCostPercent()).isEqualTo(deltas.getMaxDiffContractorCostPercent());
        assertThat(actual.maxDiffComputedWaitingPercent()).isEqualTo(deltas.getMaxDiffComputedWaitingPercent());
        assertEquals(taxiTariff.getWorkGroup(), actual.workGroup());
        assertNotNull(actual.contractorId());
        assertEquals(taxiTariff.getContract().getContractorId(),actual.contractorId());
        assertEquals(taxiTariff.getTriggerTime(),actual.triggerTime());
        assertThat(actual.deleted()).isTrue();
    }

}