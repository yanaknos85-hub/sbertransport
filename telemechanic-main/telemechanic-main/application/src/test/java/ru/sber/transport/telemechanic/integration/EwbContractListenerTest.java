package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.dao.EwbContractRepository;
import ru.sber.transport.telemechanic.database.dao.OrganizationMedicalLicenseRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.AwaitingSynchronizationException;
import ru.sber.transport.telemechanic.messaging.listener.message.EwbContractMessage;
import ru.sber.transport.telemechanic.messaging.listener.message.OrganizationMedicalLicenseMessage;
import ru.sber.transport.telemechanic.provider.impl.EwbContractProviderImpl;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class EwbContractListenerTest extends KafkaTest {
    @Autowired
    private OrganizationMedicalLicenseRepository organizationMedicalLicenseRepository;
    @Autowired
    private EwbContractRepository ewbContractRepository;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(EwbContractProviderImpl.class);
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql",
                     "/scripts/basic_corp_structure.sql" })
    @SneakyThrows
    void listen() {
        var now = LocalDate.now();
        var license1Id1 = UUID.randomUUID();
        var organizationMedicalLicense1 = new OrganizationMedicalLicense(license1Id1,
                                                                         UUID.randomUUID().toString(),
                                                                         UUID.randomUUID().toString(),
                                                                         now,
                                                                         now.plusYears(1),
                                                                         true);
        var organizationMedicalLicense2 = new OrganizationMedicalLicense(UUID.randomUUID(),
                                                                         UUID.randomUUID().toString(),
                                                                         UUID.randomUUID().toString(),
                                                                         now,
                                                                         now.plusYears(1),
                                                                         true);
        var expectedLicenses = List.of(organizationMedicalLicense1, organizationMedicalLicense2);
        var ewbContract1 = new EwbContract(UUID.randomUUID(),
                                           UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                           InspectionType.MEDIC,
                                           "2BM",
                                           UUID.randomUUID().toString(),
                                           license1Id1,
                                           true,
                                           LocalDate.now(),
                                           LocalDate.now().plusYears(1));
        var ewbContract2 = new EwbContract(UUID.randomUUID(),
                                           UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                                           InspectionType.TECHNIC,
                                           "2BM",
                                           UUID.randomUUID().toString(),
                                           null,
                                           false,
                                           LocalDate.now().minusYears(1),
                                           LocalDate.now().minusDays(1));
        var expectedEwbContracts = List.of(ewbContract1, ewbContract2);
        var message = new EwbContractMessage(ewbContract1.getContractId(),
                                             ewbContract1.getContractId(),
                                             ewbContract1.getOrganizationId(),
                                             ewbContract1.getInspectionType(),
                                             ewbContract1.getEdfOperatorId(),
                                             ewbContract1.getEdfCode(),
                                             ewbContract1.getOrganizationMedicalLicenseId(),
                                             ewbContract1.getStart(),
                                             ewbContract1.getEnd(),
                                             ewbContract1.isActive());
        try {
            produceMessage("service.tariff-fleet.ewb_contract", message);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(AwaitingSynchronizationException.class);
            assertThat(e.getMessage())
                    .isEqualTo("Can't save ewb contract, id:%s, organizationMedicalLicenseId:%s, organization medical license isn't present",
                               ewbContract1.getContractId(),
                               license1Id1);
        }
        produceMessage("service.tariff-fleet.ewb_contract",
                       new EwbContractMessage(ewbContract2.getContractId(),
                                              ewbContract2.getContractId(),
                                              ewbContract2.getOrganizationId(),
                                              ewbContract2.getInspectionType(),
                                              ewbContract2.getEdfOperatorId(),
                                              ewbContract2.getEdfCode(),
                                              ewbContract2.getOrganizationMedicalLicenseId(),
                                              ewbContract2.getStart(),
                                              ewbContract2.getEnd(),
                                              ewbContract2.isActive()));
        produceMessage("service.tariff-fleet.organization_medical_license",
                       new OrganizationMedicalLicenseMessage(organizationMedicalLicense1.getId(),
                                                             organizationMedicalLicense1.getSeries(),
                                                             organizationMedicalLicense1.getNumber(),
                                                             organizationMedicalLicense1.getIssueDate(),
                                                             organizationMedicalLicense1.getExpiryDate()));
        produceMessage("service.tariff-fleet.organization_medical_license",
                       new OrganizationMedicalLicenseMessage(organizationMedicalLicense2.getId(),
                                                             organizationMedicalLicense2.getSeries(),
                                                             organizationMedicalLicense2.getNumber(),
                                                             organizationMedicalLicense2.getIssueDate(),
                                                             organizationMedicalLicense2.getExpiryDate()));
        await().atMost(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(1))
               .until(() -> organizationMedicalLicenseRepository.findAll().size() == 2);
        //Так как почему-то dlq сообщение не отправляется в тестовой конфигурации, даже с продолжительным ожиданием, пока тестируем то, что можем
        produceMessage("service.tariff-fleet.ewb_contract", message);
        var actualEwbContracts = ewbContractRepository.findAll();
        assertThat(actualEwbContracts)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedEwbContracts);
        var actualLicenses = organizationMedicalLicenseRepository.findAll();
        assertThat(actualLicenses)
                .usingRecursiveComparison()
                .isEqualTo(expectedLicenses);
        assertThat(LOGGING_EXTENSION.getEvents()).hasSize(1);
        var firstEvent = LOGGING_EXTENSION.getEvents().getFirst();
        assertThat(firstEvent.getLoggerName()).isEqualTo(EwbContractProviderImpl.class.getName());
        assertThat(firstEvent.getFormattedMessage())
                .isEqualTo("Can't save ewb contract, id:%s, organizationMedicalLicenseId:%s, organization medical license isn't present"
                                   .formatted(ewbContract1.getContractId(), license1Id1));
    }
}