package ru.sber.transport.token_generator.providers.database.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.token_generator.database.token_generator.tables.records.AccountRolesRecord;
import ru.sber.transport.token_generator.providers.database.AccountRepository;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@Transactional
@JooqTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EmbeddedPostgres
@Import(JooqDatabaseConfig.class)
@DisplayName("Проверка репозитория УЗ")
@ActiveProfiles("test")
class AccountRepositoryTest {

    @Autowired
    private DSLContext context;

    private final AccountRepository repository = new AccountRepositoryImpl() {
        @Override
        public DSLContext context() {
            return context;
        }
    };

    @DisplayName("Проверка сохранения")
    @Test
    void test_save() {
        var source = new AccountRolesRecord();
        source.setId(UUID.randomUUID());
        source.setRole("ROLE");

        assertThat(context.fetchCount(repository.table())).isZero();

        repository.save(source);

        assertThat(context.fetchCount(repository.table())).isEqualTo(1);
        var actual = context.selectFrom(repository.table()).fetchOne();
        assertThat(actual).isEqualTo(source);
    }

    @DisplayName("Проверка существования")
    @Test
    void test_exists() {
        var source = new AccountRolesRecord();
        source.setId(UUID.randomUUID());
        source.setRole("ROLE");

        context.insertInto(repository.table()).set(source).execute();

        source.setRole("ROLE_2");

        context.insertInto(repository.table()).set(source).execute();

        assertThat(repository.findRoles(source.getId().toString())).hasSameElementsAs(List.of("ROLE", "ROLE_2"));
    }

    @DisplayName("Удаление по идентификатору и роли")
    @Test
    void test_deleteByIdAndRole() {
        var id1 = UUID.randomUUID();

        var source = new AccountRolesRecord();
        source.setId(id1);
        source.setRole("ROLE");

        context.insertInto(repository.table()).set(source).execute();

        source.setRole("ROLE_2");

        context.insertInto(repository.table()).set(source).execute();

        var id2 = UUID.randomUUID();
        source.setId(id2);

        context.insertInto(repository.table()).set(source).execute();

        assertThat(context.fetchCount(repository.table())).isEqualTo(3);

        repository.deleteAll(id1.toString(), List.of("ROLE", "ROLE_2"));

        assertThat(context.fetchCount(repository.table())).isEqualTo(1);
        assertThat(context.fetchExists(context.selectFrom(repository.table()).where(repository.table().ID.eq(id1)).and(repository.table().ROLE.eq("ROLE")))).isFalse();
        assertThat(context.fetchExists(context.selectFrom(repository.table()).where(repository.table().ID.eq(id1)).and(repository.table().ROLE.eq("ROLE_2")))).isFalse();
        assertThat(context.fetchExists(context.selectFrom(repository.table()).where(repository.table().ID.eq(id2)).and(repository.table().ROLE.eq("ROLE_2")))).isTrue();
    }

    @DisplayName("Сохранение всех")
    @Test
    void test_saveAll() {
        var id = UUID.randomUUID();
        var roles = Instancio.ofList(String.class).create();

        repository.saveAll(id.toString(), roles);

        assertThat(context.fetchCount(repository.table())).isEqualTo(roles.size());
    }

}