package ru.sber.transport.authentication.providers.two_factor;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.DSLContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.config.JooqDatabaseConfig;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.TwoFactor;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.TwoFactorRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.authentication.business.dto.AccessTokenData;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.providers.TwoFactorProvider;
import ru.sber.transport.authentication.providers.account.mapper.AccountMapper;
import ru.sber.transport.authentication.providers.account.mapper.AccountMapperImpl;
import ru.sber.transport.authentication.providers.two_factor.config.TwoFactorProperties;
import ru.sber.transport.authentication.providers.two_factor.mappers.TwoFactorBusinessMapper;
import ru.sber.transport.authentication.providers.two_factor.mappers.TwoFactorBusinessMapperImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@JooqTest
@DisplayName("Проверка провайдера второго фактора")
@ContextConfiguration(classes = {AccountMapperImpl.class, JooqDatabaseConfig.class})
class TwoFactorProviderImplTest {

    @Autowired
    private DSLContext dslContext;

    @Autowired
    private AccountMapper mapper;

    private final TwoFactorBusinessMapper twoFactorBusinessMapper = new TwoFactorBusinessMapperImpl();

    private final TwoFactorProperties properties = new TwoFactorProperties();

    private final TwoFactorProvider provider = new TwoFactorProviderImpl(twoFactorBusinessMapper, properties, Clock.systemUTC()) {

        /**
         * Контекст выполнения.
         *
         * @return контекст.
         */
        public DSLContext context() {
            return dslContext;
        }

    };

    @Test
    @DisplayName("Генерация второго фактора")
    void test_generate() {
        var account = Instancio.create(AccountDto.class);
        var data = Instancio.create(AccessTokenData.class);

        var accRecord = new AccountRecord();
        mapper.toModel(accRecord, account);

        dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

        var actual = provider.generate(account, data);

        assertThat(actual.code()).isNotNull();
        assertThat(actual.expiration()).isEqualTo(data.expiration().toLocalDateTime());
        assertThat(actual.token()).isEqualTo(data.value());
    }

    @Test
    @DisplayName("Проверка наличия кода у УЗ. Нет кода")
    void test_checkUserCode_noCode() {
        assertThat(provider.checkCode(UUID.randomUUID(), "1234")).isFalse();
    }

    @Test
    @DisplayName("Проверка наличия кода у УЗ")
    void test_checkUserCode() {
        var account = Instancio.create(AccountDto.class);

        var accRecord = new AccountRecord();
        mapper.toModel(accRecord, account);

        dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

        var code = "1234";

        var twoFactor = new TwoFactorRecord();
        twoFactor.setId(account.getId());
        twoFactor.setToken("Token");
        twoFactor.setCode(code);
        twoFactor.setExpire(LocalDateTime.now().plusDays(2));

        dslContext.insertInto(TwoFactor.TWO_FACTOR).set(twoFactor).execute();

        assertThat(provider.checkCode(account.getId(), code)).isTrue();
    }

    @Test
    @DisplayName("Проверка наличия кода у УЗ. Код истек")
    void test_checkUserCode_expired() {
        var account = Instancio.create(AccountDto.class);

        var accRecord = new AccountRecord();
        mapper.toModel(accRecord, account);

        dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

        var code = "1234";

        var twoFactor = new TwoFactorRecord();
        twoFactor.setId(account.getId());
        twoFactor.setToken("Token");
        twoFactor.setCode(code);
        twoFactor.setExpire(LocalDateTime.now().minusDays(2));

        dslContext.insertInto(TwoFactor.TWO_FACTOR).set(twoFactor).execute();

        assertThat(provider.checkCode(account.getId(), code)).isFalse();
    }

    @Test
    @DisplayName("Проверка наличия кода у УЗ. Не тот код")
    void test_checkUserCode_wrongCode() {
        var account = Instancio.create(AccountDto.class);

        var accRecord = new AccountRecord();
        mapper.toModel(accRecord, account);

        dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

        var twoFactor = new TwoFactorRecord();
        twoFactor.setId(account.getId());
        twoFactor.setToken("Token");
        twoFactor.setCode("1234");
        twoFactor.setExpire(LocalDateTime.now().plusDays(2));

        dslContext.insertInto(TwoFactor.TWO_FACTOR).set(twoFactor).execute();

        assertThat(provider.checkCode(account.getId(), "5678")).isFalse();
    }

    @DisplayName("Проверка удаления")
    @Test
    void test_delete() {
        var account = Instancio.create(AccountDto.class);

        var accRecord = new AccountRecord();
        mapper.toModel(accRecord, account);

        dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

        var twoFactor = new TwoFactorRecord();
        twoFactor.setId(account.getId());
        twoFactor.setToken("Token");
        twoFactor.setCode("1234");
        twoFactor.setExpire(LocalDateTime.now().plusDays(2));

        dslContext.insertInto(TwoFactor.TWO_FACTOR).set(twoFactor).execute();

        provider.removeCode(account.getId());

        assertThat(dslContext.fetchCount(TwoFactor.TWO_FACTOR)).isZero();
    }

    @Test
    @DisplayName("Проверка удаления старых кодов")
    void test_releaseOld() {
        var now = LocalDateTime.now(Clock.systemUTC());
        for (var i = 0; i < 100; i++) {
            var expiration = now.plusDays(50 - i);
            var account = Instancio.create(AccountDto.class);

            var accRecord = new AccountRecord();
            mapper.toModel(accRecord, account);

            dslContext.insertInto(Account.ACCOUNT).set(accRecord).execute();

            var twoFactor = new TwoFactorRecord();
            twoFactor.setId(account.getId());
            twoFactor.setToken(UUID.randomUUID().toString());
            twoFactor.setCode(UUID.randomUUID().toString());
            twoFactor.setExpire(expiration);

            dslContext.insertInto(TwoFactor.TWO_FACTOR).set(twoFactor).execute();
        }
        ((TwoFactorProviderImpl) provider).releaseOld();

        assertThat(dslContext.fetchCount(TwoFactor.TWO_FACTOR)).isEqualTo(50);

        var minimum = dslContext.select(TwoFactor.TWO_FACTOR.EXPIRE).from(TwoFactor.TWO_FACTOR).orderBy(TwoFactor.TWO_FACTOR.EXPIRE.asc())
                .limit(1).fetchOneInto(LocalDateTime.class);

        var maximum = dslContext.select(TwoFactor.TWO_FACTOR.EXPIRE).from(TwoFactor.TWO_FACTOR).orderBy(TwoFactor.TWO_FACTOR.EXPIRE.desc())
                .limit(1).fetchOneInto(LocalDateTime.class);

        assertThat(minimum).isNotNull();
        assertThat(maximum).isNotNull();
        assertThat(minimum.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(now.plusDays(1).truncatedTo(ChronoUnit.SECONDS));
        assertThat(maximum.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(now.plusDays(50).truncatedTo(ChronoUnit.SECONDS));
    }

}