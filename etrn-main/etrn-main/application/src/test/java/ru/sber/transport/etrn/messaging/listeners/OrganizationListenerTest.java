package ru.sber.transport.etrn.messaging.listeners;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.etrn.database.dao.OrganizationGroupRepository;
import ru.sber.transport.etrn.database.dao.OrganizationRepository;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sber.transport.etrn.database.model.OrganizationGroup;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@EmbeddedPostgres
@SpringBootTest
@DisplayName("Проверка слушателя организаций")
class OrganizationListenerTest extends KafkaTest {

    @Autowired
    private OrganizationRepository repository;

    @Autowired
    private OrganizationGroupRepository groupRepository;

    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Проверка получения новой организации с группой")
    void handleOrganization_new() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = new OrganizationMessage(id,
                idDigit,
                "Official name",
                "Address", null, null, null, null, false,
                new OrganizationMessage.OrganizationGroup(id, "organizationGroupName", false));


        produceMessage("service.organization", message);

        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
        assertThat(actual.getOrganizationGroup().getId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Проверка получения новой организации без группы")
    void handleOrganization_new2() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = new OrganizationMessage(id,
                idDigit,
                "Official name",
                "Address", null, null, null, null, false,
                null);


        produceMessage("service.organization", message);

        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().get(0);
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id)).isPresent();
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
        assertThat(actual.getOrganizationGroup()).isNull();
    }

    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var organizationGroup = groupRepository.saveAndFlush(OrganizationGroup.builder()
                .id(id)
                .name("organizationGroup1")
                .build());
        repository.save(Organization.builder()
                .id(id)
                .digitId(1L)
                .organizationGroup(organizationGroup)
                .build());

        var message = new OrganizationMessage(id,
                idDigit,
                "Official name",
                "Address", null, null, null, null, true,
                new OrganizationMessage.OrganizationGroup(id, "organizationGroupName",
                        false));

        produceMessage("service.organization", message);

        assertThat(repository.findAllByActive(true)).isEmpty();
    }
}