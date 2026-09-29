package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.EwbTariffRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.messaging.listener.message.EwbTariffMessage;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class EwbTariffListenerTest extends KafkaTest {
    @Autowired
    private EwbTariffRepository repository;
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql",
                     "/scripts/basic_corp_structure.sql",
                     "/scripts/organization_medical_license.sql",
                     "/scripts/ewb_contract.sql",
                     "/scripts/fleet_owner_organization.sql"})
    @SneakyThrows
    void listen() {
        var expected1 = new EwbTariff(UUID.randomUUID(),
                                      new EwbContract().setContractId(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435")),
                                      UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                                      UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                                      true);
        var expected2 = new EwbTariff(UUID.randomUUID(),
                                      new EwbContract().setContractId(UUID.fromString("e921aa12-0668-4bac-856c-c3d851882911")),
                                      UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                                      UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                      false);
        var expected = List.of(expected1, expected2);
        produceMessage("service.tariff-fleet.ewb_tariff",
                       new EwbTariffMessage(expected1.getTariffId(),
                                            expected1.getTariffId(),
                                            expected1.getContract().getContractId(),
                                            expected1.getOrganizationId(),
                                            expected1.getDepartmentId(),
                                            expected1.isActive()));
        produceMessage("service.tariff-fleet.ewb_tariff",
                       new EwbTariffMessage(expected2.getTariffId(),
                                            expected2.getTariffId(),
                                            expected2.getContract().getContractId(),
                                            expected2.getOrganizationId(),
                                            expected2.getDepartmentId(),
                                            expected2.isActive()));
        var actual = repository.findAll();
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("contract")
                .isEqualTo(expected);
    }
}