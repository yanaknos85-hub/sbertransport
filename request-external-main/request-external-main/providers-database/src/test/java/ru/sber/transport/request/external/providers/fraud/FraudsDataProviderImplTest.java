package ru.sber.transport.request.external.providers.fraud;

import static org.assertj.core.api.Assertions.assertThat;

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
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.external.providers.model.TestFraud;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера информации о фроде")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class FraudsDataProviderImplTest {

    @Autowired
    private DSLContext context;

    private final FraudsProvider fraudsProvider = new FraudsDataProviderImpl() {

        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка сохранения информации о фроде")
    void test_save() {
        final var source = new TestFraud(
                UUID.randomUUID(),
                Instancio.create(String.class),
                "RECEIPT"
        );

        fraudsProvider.save(source);

        assertThat(context.fetchCount(Tables.FRAUD)).isEqualTo(1);

        final var actual = context.selectFrom(Tables.FRAUD).fetchSingle();
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getComment()).isEqualTo(source.getComment());
    }
}