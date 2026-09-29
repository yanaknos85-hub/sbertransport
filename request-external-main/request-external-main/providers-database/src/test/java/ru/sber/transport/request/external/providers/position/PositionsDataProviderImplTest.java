package ru.sber.transport.request.external.providers.position;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.database.external_request.Tables.POSITION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.TRANSPORT_TYPES;

import io.qameta.allure.Feature;
import java.util.UUID;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.providers.model.TestPosition;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера должностей")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class PositionsDataProviderImplTest {

    @Autowired
    private DSLContext context;

    private final PositionsProvider positionsProvider = new PositionsDataProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения должности")
    void test_save() {
        final var source = new TestPosition(
                UUID.randomUUID(),
                Instancio.createList(String.class)
        );

        positionsProvider.save(source);

        assertThat(context.fetchCount(Tables.POSITION)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.POSITION).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());

        assertThat(context.fetchCount(POSITION_TRANSPORT_TYPE)).isEqualTo(source.getAvailableClasses().size());
        assertThat(context.fetchCount(TRANSPORT_TYPES)).isEqualTo(source.getAvailableClasses().size());

        assertThat(context.select(TRANSPORT_TYPES.TRANSPORT_TYPE).from(TRANSPORT_TYPES).fetch(TRANSPORT_TYPES.TRANSPORT_TYPE)).hasSameElementsAs(source.getAvailableClasses());
        assertThat(context.select(POSITION_TRANSPORT_TYPE.TRANSPORT_TYPE).from(POSITION_TRANSPORT_TYPE).fetch(POSITION_TRANSPORT_TYPE.TRANSPORT_TYPE)).hasSameElementsAs(source.getAvailableClasses());
        assertThat(context.select(POSITION_TRANSPORT_TYPE.POSITION_ID).from(POSITION_TRANSPORT_TYPE).fetch(POSITION_TRANSPORT_TYPE.POSITION_ID)).allMatch(id -> source.getId().equals(id));
    }

}