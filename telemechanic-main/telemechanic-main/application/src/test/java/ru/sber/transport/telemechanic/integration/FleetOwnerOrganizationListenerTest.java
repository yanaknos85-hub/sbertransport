package ru.sber.transport.telemechanic.integration;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.FleetOwnerOrganizationRepository;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.messaging.listener.message.FleetOwnerOrganizationMessage;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest(properties = "extlogging.kafka: false")
@EmbeddedPostgres
class FleetOwnerOrganizationListenerTest extends KafkaTest {
    @Autowired
    private FleetOwnerOrganizationRepository repository;
    
    @Test
    @Sql(scripts = { "/scripts/cleanup_database.sql", "/scripts/basic_corp_structure.sql" })
    @SneakyThrows
    void listen() {
        var organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var organizationId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        var edfOperatorId = "2AL";
        var edfCode = UUID.randomUUID().toString();
        produceMessage("service.tariff-fleet.fleet_owner_organization",
                       new FleetOwnerOrganizationMessage(organizationId1,
                                                         organizationId1,
                                                         edfOperatorId,
                                                         edfCode));
        produceMessage("service.tariff-fleet.fleet_owner_organization",
                       new FleetOwnerOrganizationMessage(organizationId2,
                                                         organizationId2,
                                                         edfOperatorId,
                                                         edfCode));
        var actual = repository.findAll();
        Assertions.assertThat(actual)
                  .hasSize(2)
                  .extracting(FleetOwnerOrganization::getOrganizationId,
                              FleetOwnerOrganization::getEdfOperatorId,
                              FleetOwnerOrganization::getEdfCode)
                  .containsExactly(tuple(organizationId1, edfOperatorId, edfCode),
                                   tuple(organizationId2, edfOperatorId, edfCode));
    }
}