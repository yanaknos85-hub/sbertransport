package ru.sber.transport.request.external.providers.available_classes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.qameta.allure.Feature;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
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
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.postgres.EmbeddedPostgres;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера разрешений при условии allow.enabled = false")
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {JooqDatabaseConfig.class})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@ActiveProfiles("test")
class AvailableClassesAllowDisabledImplTest {

    @Autowired
    private DSLContext dslContext;

    private final EmployeesProvider employeesProvider = mock(EmployeesProvider.class);
    private final Clock clock = Clock.fixed(OffsetDateTime.parse("2022-01-01T00:00:00+03:00").toInstant(), ZoneOffset.UTC);

    private final AvailableClasses provider = new AvailableClassesImpl(employeesProvider, false, false, clock) {

        @Override
        public DSLContext context() {
            return dslContext;
        }
    };


    @Test
    @DisplayName("Проверка разрешения для пользователя")
    void test_allow() {
        assertThat(provider.allow(UUID.randomUUID())).isTrue();
        verify(employeesProvider, never()).get(any());
    }

}