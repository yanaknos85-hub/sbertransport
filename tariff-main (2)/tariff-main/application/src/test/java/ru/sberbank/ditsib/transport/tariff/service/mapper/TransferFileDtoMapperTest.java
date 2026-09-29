package ru.sberbank.ditsib.transport.tariff.service.mapper;

import io.qameta.allure.Feature;
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
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.Contract;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.GroupTransferTariff;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.dto.files.TransferFileDto;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres()
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class TransferFileDtoMapperTest extends KafkaTest {
    
    @Autowired
    TransferFileDtoMapperImpl transferFileDtoMapper;
    
    @Test
    void groupTransferTariffToTransferFileDtoTest() {
        var regionName = "regionName";
        var humanReadableId = "humanReadableId";
        
        var contractor = new Contractor();
        contractor.setName("contractorName");
        contractor.setIntegrationType("contractorIntegrationType");
        
        var item = buildGroupTransferTariff();
    
        var target = new TransferFileDto();
        target.setId("id");
        target.setDepartmentHumanReadableId(humanReadableId);
        var transferFileDto = transferFileDtoMapper.mapToTransferFileDto(target, item, contractor, regionName);

        final var expectedTariffValues = transferFileDto.getExtendedTariffValues();
        assertAll(
                () -> assertEquals(target.getId(), transferFileDto.getId()),
                () -> assertEquals(item.isActive(), transferFileDto.isActive()),
                () -> assertEquals(item.getOrganization().getName(), transferFileDto.getOrganization()),
                () -> assertEquals(item.getContract().getContractNumber(), transferFileDto.getContract()),
                () -> assertEquals(contractor.getName(), transferFileDto.getContractor()),
                () -> assertEquals(contractor.getIntegrationType(), transferFileDto.getIntegrationType()),
                () -> assertEquals(regionName, transferFileDto.getRegion()),
                () -> assertEquals(humanReadableId, transferFileDto.getDepartmentHumanReadableId()),
                () -> assertEquals(item.getTariffStartDate().toLocalDate().toString(), transferFileDto.getTariffPeriodFrom()),
                () -> assertEquals(item.getTariffEndDate().toLocalDate().toString(), transferFileDto.getTariffPeriodTo()),
                () -> assertEquals(item.getTransportType().getRusName(), transferFileDto.getServiceType()),
                () -> assertEquals(item.getGroupTransferClass().getRusName(), transferFileDto.getTariffClass()),
                () -> assertEquals(item.getMinKm().longValue(), transferFileDto.getMinimumTripAttributes().getMinKm()),
                () -> assertEquals(item.getMinRideCost().longValue(), transferFileDto.getMinimumTripAttributes().getMinRideCost()),
                () -> assertEquals(item.getFreeWaitingTime().longValue(), transferFileDto.getMinimumTripAttributes().getFreeWaitingTime()),
                () -> assertEquals((double) item.getRideCostPerKm() / 100D, transferFileDto.getGeneralTariffValues().getRideCostPerKm()),
                () -> assertEquals((double) item.getRideCostPerMin() / 100D, transferFileDto.getGeneralTariffValues().getRideCostPerMin()),
                () -> assertEquals((double) item.getWaitCostPerMinIntermediate() / 100D, transferFileDto.getGeneralTariffValues().getWaitIntermediateTime()),
                () -> assertEquals((double) item.getCostPerKmCity() / 100D, expectedTariffValues.getUrb().getCostPerKm()),
                () -> assertEquals((double) item.getCostPerMinCity() / 100D, expectedTariffValues.getUrb().getCostPerMin()),
                () -> assertEquals((double) item.getCostPerKmSuburb() / 100D, expectedTariffValues.getSuburb().getCostPerKm()),
                () -> assertEquals((double) item.getCostPerMinSuburb() / 100D, expectedTariffValues.getSuburb().getCostPerMin()),
                () -> assertEquals(item.getWaitCostPerMin() / 100L, expectedTariffValues.getWaitCostPerMin()),
                () -> assertEquals(item.getMinCancelTime(), transferFileDto.getConditions().getMinCancelTime()),
                () -> assertEquals(item.getMinCreateTime(), transferFileDto.getConditions().getMinCreateTime()),
                () -> assertEquals(item.getTriggerTime(), transferFileDto.getConditions().getTriggerTime())
                 );
    }
    
    private GroupTransferTariff buildGroupTransferTariff() {
        return GroupTransferTariff.builder()
                                  .active(true)
                                  .organization(Organization.builder().name("organizationName").build())
                                  .contract(Contract.builder().contractNumber("123").build())
                                  .regionId(UUID.randomUUID())
                                  .department(Department.builder().id(UUID.randomUUID()).build())
                                  .tariffStartDate(LocalDateTime.now())
                                  .tariffEndDate(LocalDateTime.now())
                                  .transportType(TransportTypeEnum.GROUP_TRANSFER)
                                  .groupTransferClass(GroupTransferClass.TRANSFER)
                                  .workGroup("workGroup")
                                  .minKm(10)
                                  .minRideCost(10)
                                  .freeWaitingTime(5)
                                  .rideCostPerKm(20)
                                  .rideCostPerMin(15)
                                  .waitCostPerMinIntermediate(10)
                                  .costPerKmCity(10)
                                  .costPerMinCity(10)
                                  .costPerKmSuburb(20)
                                  .costPerMinSuburb(30)
                                  .waitCostPerMin(10)
                                  .minCancelTime(20L)
                                  .minCreateTime(5L)
                                  .triggerTime(60L)
                                  .build();
    }
}