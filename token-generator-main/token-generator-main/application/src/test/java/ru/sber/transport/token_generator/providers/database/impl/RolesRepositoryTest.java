package ru.sber.transport.token_generator.providers.database.impl;

import io.qameta.allure.Feature;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.database.repository.JooqRepository;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.token_generator.database.token_generator.tables.Roles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres
@DisplayName("Проверка репозитория ролей")
@JooqTest
@ActiveProfiles("test")
@Import(JooqDatabaseConfig.class)
class RolesRepositoryTest {

    @Autowired
    private DSLContext context;

    private final JooqRepository<Roles, RolesRecord, String> repository = new RolesRepositoryImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @Test
    @DisplayName("Проверка репозитория")
    void test_save() {
        var rolesRecord = new RolesRecord();
        rolesRecord.setCode("Code");
        rolesRecord.setDataMaster(false);

        assertThat(context.fetchCount(Roles.ROLES)).isZero();
        repository.save(rolesRecord);
        assertThat(context.fetchCount(Roles.ROLES)).isEqualTo(1);
    }

}