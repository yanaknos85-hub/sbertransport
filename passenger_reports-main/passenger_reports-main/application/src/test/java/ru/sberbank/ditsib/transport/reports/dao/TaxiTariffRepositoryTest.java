package ru.sberbank.ditsib.transport.reports.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.Contractor;
import ru.sberbank.ditsib.transport.reports.model.Organization;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;
import ru.sberbank.ditsib.transport.reports.model.tariff.ContractorDeviationsTariffParams;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
public class TaxiTariffRepositoryTest extends KafkaTest {
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private ContractRepository contractRepository;
    
    @Test
    void test() {
        Organization org = organizationRepository.save(
                Organization.builder().id(UUID.randomUUID()).officialName("Org").build());
        
        Contractor contractor =
                contractorRepository.save(Contractor.builder().id(UUID.randomUUID()).name("Contr").build());
        
        Contract contract =
                contractRepository.save(Contract.builder()
                                                .id(UUID.randomUUID())
                                                .contractor(contractor)
                                                .uvhd("test")
                                                .contractNumber("test")
                                                .includeVat(true)
                                                .sum(1000L)
                                                .endDate(LocalDate.now())
                                                .startDate(LocalDate.now().minus(2, ChronoUnit.DAYS))
                                                .transportType(TransportTypeEnum.TAXI)
                                                .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                                .creationTime(LocalDateTime.now().minus(2, ChronoUnit.DAYS))
                                                .active(true)
                                                .userId(UUID.randomUUID())
                                                .vatValue(1000)
                                                .build());
    
        var deltas = ContractorDeviationsTariffParams.builder()
                                                     .maxDiffComputedDistancePercent(10)
                                                     .maxDiffFactDistancePercent(12)
                                                     .maxDiffComputedCostPercent(14)
                                                     .maxDiffContractorCostPercent(16)
                                                     .maxDiffComputedWaitingPercent(18)
                                                     .build();
    
        var tariff = TaxiTariff.builder()
                               .id(UUID.randomUUID())
                               .humanReadableId("humanReadableId")
                               .region("region")
                               .organizationId(org.getId())
                               .transportType(TransportTypeEnum.TAXI)
                               .contract(contract)
                               .taxiClass(TaxiClass.ECONOMY)
                               .rideCostPerKm(1000)
                               .rideCostPerMin(700)
                               .waitCostPerMin(200)
                               .carServiceCost(500)
                               .waitCostPerMinIntermediate(100)
                               .contractorDeviationParams(deltas)
                               .build();
    
        tariff = taxiTariffRepository.save(tariff);
        assertThat(taxiTariffRepository.count()).isEqualTo(1);
    }
}
