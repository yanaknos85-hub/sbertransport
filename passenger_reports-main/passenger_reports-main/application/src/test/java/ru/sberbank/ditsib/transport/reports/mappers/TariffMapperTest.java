package ru.sberbank.ditsib.transport.reports.mappers;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TariffMapperTest {
    
    private TariffMapper tariffMapper = Mappers.getMapper(TariffMapper.class);
    
    @Test
    void taxiTariffMessageToModel() {
        TaxiTariffMessage message = TaxiTariffMessage.builder()
                                                     .id(UUID.randomUUID())
                                                     .humanReadableId("humanReadableId")
                                                     //.region("region")
                                                     .organizationId(UUID.randomUUID())
                                                     .transportType("TAXI")
                                                     .serviceType("EMPLOYEE_TRANSPORTATION")
                                                     //.contractorId(UUID.randomUUID())
                                                     .taxiClass("ECONOMY")
                                                     .rideCostPerKm(1000)
                                                     .waitCostPerMin(200)
                                                     .carServiceCost(500)
                                                     .waitCostPerMinIntermediate(100)
                                                     .deleted(false)
                                                     .maxDiffComputedDistancePercent(10)
                                                     .maxDiffFactDistancePercent(12)
                                                     .maxDiffComputedCostPercent(14)
                                                     .maxDiffContractorCostPercent(16)
                                                     .maxDiffComputedWaitingPercent(18)
                                                     .build();
        
        TaxiTariff actual = tariffMapper.taxiTariffMessageToModel(message);
        
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHumanReadableId()).isEqualTo(message.getHumanReadableId());
        assertThat(actual.getServiceType().name()).isEqualTo(message.getServiceType());
        //assertThat(actual.getRegion()).isEqualTo(message.getRegion());
        assertThat(actual.getOrganizationId()).isEqualTo(message.getOrganizationId());
        assertThat(actual.getTaxiClass().name()).isEqualTo(message.getTaxiClass());
        assertThat(actual.getRideCostPerKm()).isEqualTo(message.getRideCostPerKm());
        assertThat(actual.getWaitCostPerMin()).isEqualTo(message.getWaitCostPerMin());
        assertThat(actual.getCarServiceCost()).isEqualTo(message.getCarServiceCost());
        assertThat(actual.getWaitCostPerMinIntermediate()).isEqualTo(message.getWaitCostPerMinIntermediate());
        assertThat(actual.getContractorDeviationParams().getMaxDiffComputedDistancePercent())
                .isEqualTo(message.getMaxDiffComputedDistancePercent());
        assertThat(actual.getContractorDeviationParams().getMaxDiffFactDistancePercent())
                .isEqualTo(message.getMaxDiffFactDistancePercent());
        assertThat(actual.getContractorDeviationParams().getMaxDiffComputedCostPercent())
                .isEqualTo(message.getMaxDiffComputedCostPercent());
        assertThat(actual.getContractorDeviationParams().getMaxDiffContractorCostPercent())
                .isEqualTo(message.getMaxDiffContractorCostPercent());
        assertThat(actual.getContractorDeviationParams().getMaxDiffComputedWaitingPercent())
                .isEqualTo(message.getMaxDiffComputedWaitingPercent());
    }
}