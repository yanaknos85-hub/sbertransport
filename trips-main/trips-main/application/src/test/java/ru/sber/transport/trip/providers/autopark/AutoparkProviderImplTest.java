package ru.sber.transport.trip.providers.autopark;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.database.trips.Tables;
import ru.sber.transport.trip.providers.autopark.mapper.AutoparkMapper;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("Проверка провайдера контрагентов")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
@ActiveProfiles({"test", "kafka", "kafka-ssl"})
class AutoparkProviderImplTest extends KafkaTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private AutoparkMapper autoparkMapper;


    @DisplayName("Проверка сохранения")
    @Test
    void test_save() {
        var provider = new AutoparkProviderImpl(dslContext, autoparkMapper);

        var id = UUID.randomUUID();
        var message = Instancio.of(AutoparkMessage.class)
                .set(Select.field(AutoparkMessage::id), id)
                .create();

        var stage = provider.save(message);
        assertThat(stage).isEqualTo(1);

        var actual = dslContext.selectFrom(Tables.AUTOPARK).where(Tables.AUTOPARK.ID.eq(id)).fetchSingle();
        assertThat(actual.getId()).isEqualTo(message.getId());
    }
}