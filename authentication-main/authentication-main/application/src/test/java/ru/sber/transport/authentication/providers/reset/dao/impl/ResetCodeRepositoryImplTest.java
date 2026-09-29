package ru.sber.transport.authentication.providers.reset.dao.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.dto.SendingChannel;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.ResetCode;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.ResetCodeRecord;
import ru.sber.transport.authentication.providers.reset.dao.ResetCodeRepository;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.scripting.ScriptUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@JooqTest
@ContextConfiguration(classes = {ResetCodeRepositoryImpl.class, JooqDatabaseConfig.class, ScriptUtils.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Проверка репозитория кода сброса")
@ActiveProfiles("test")
class ResetCodeRepositoryImplTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private ResetCodeRepository resetCodeRepository;

    @Test
    @DisplayName("Поиск по почте")
    void test_findByEmail() {
        var email = Instancio.create(String.class);
        var accountRecord = new AccountRecord();
        accountRecord.setEmail(email);
        accountRecord.setAuthType(AuthType.TWO_FA.name());
        accountRecord.setActive(true);
        accountRecord.setId(UUID.randomUUID());
        accountRecord.setLogin("login");
        accountRecord.setHash("hash");

        var resetRecord = new ResetCodeRecord();
        resetRecord.setSendingTarget(email);
        resetRecord.setSendingChannel(SendingChannel.EMAIL.name());
        resetRecord.setHash("hash");
        resetRecord.setAccountId(accountRecord.getId());

        dslContext.insertInto(Account.ACCOUNT)
                .set(accountRecord)
            .execute();
        dslContext.insertInto(ResetCode.RESET_CODE)
                .set(resetRecord)
                    .execute();

        var found = resetCodeRepository.findByEmail(email);

        assertThat(found)
            .isPresent()
            .hasValueSatisfying(actual -> assertThat(actual).isEqualTo(resetRecord));
    }

}