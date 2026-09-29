package ru.sberbank.ditsib.transport.tariff.messaging.listener;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@DisplayName("Проверка получения организаций")
@ActiveProfiles({"test", "kafka"})
class OrganizationListenerTest extends KafkaTest {

    @Autowired
    private OrganizationRepository repository;

    @AfterEach
    void dropRepository() {
        repository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Проверка получения новой организации")
    void handleOrganization_new() {
        var idDigit = 1L;
        var id = UUID.randomUUID();
        var message = new OrganizationMessage();
        message.setId(id);
        message.setAddress("Address");
        message.setOfficialName("Official name");
        message.setDigitId(idDigit);
        message.setMsrn(Instancio.create(String.class));
        message.setTid(Instancio.create(String.class));
        message.setContacts(List.of());
        message.setDeleted(false);


        produceMessage("service.organization", message);

        assertThat(repository.count()).isEqualTo(1);
        Organization actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(repository.findById(id).orElse(null)).isNotNull();
        assertThat(actual.getDigitId()).isEqualTo(idDigit);
    }

    @Test
    @DisplayName("Удаление")
    void handleOrganization_delete() {
        var id = UUID.randomUUID();
        repository.save(Instancio.of(Organization.class)
                .set(field(Organization::getId), id)
                .set(field(Organization::getDigitId), 1L)
                .set(field(Organization::isActive), true)
                .create());
        var message = new OrganizationMessage();
        message.setId(id);
        message.setAddress("Address");
        message.setOfficialName("Official name");
        message.setDigitId(Instancio.create(Long.class));
        message.setMsrn(Instancio.create(String.class));
        message.setTid(Instancio.create(String.class));
        message.setContacts(List.of());
        message.setDeleted(true);

        produceMessage("service.organization", message);

        assertEquals(1, repository.findAllByActive(true).size());
    }
}