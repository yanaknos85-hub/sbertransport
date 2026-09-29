package ru.sber.transport.authentication.providers.account;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractDatabaseTest;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.account.mapper.AccountMapper;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.database.authentication.tables.Account;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка провайдера УЗ")
class AccountProviderImplTest extends AbstractDatabaseTest {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);
    
    private AccountProvider provider;
    
    @BeforeEach
    void setup() {
        provider = new AccountProviderImpl(accountRepository, roleRepository, accountMapper);
    }
    
    @Test
    @DisplayName("Получение по логину")
    void test_get() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Password");
        account.setActive(true);
        account.setTransferPassword(false);
        account.setEmail("email");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);
        
        var actual = provider.get(account.getLogin());
        
        assertThat(actual).isPresent();

        var actualAccount = dslContext.selectFrom(Account.ACCOUNT).where(Account.ACCOUNT.ID.eq(actual.get().getId())).fetchOne();

        assertThat(actualAccount).isNotNull();

        assertThat(actual.get().getLogin()).isEqualTo(actualAccount.getLogin()).isEqualTo(account.getLogin());
        assertThat(actual.get().getId()).isEqualTo(actualAccount.getId()).isEqualTo(account.getId());
        assertThat(actual.get().getEmail()).isEqualTo(actualAccount.getEmail()).isEqualTo(account.getEmail());
        assertThat(actual.get().getHash()).isEqualTo(actualAccount.getHash()).isEqualTo(account.getHash());
        assertThat(actualAccount.getAuthType()).isEqualTo("BASIC");
    }
    
    @Test
    @DisplayName("Получение по идентификатору")
    void test_get_id() {
        var account = new AccountRecord();
        
        account.setLogin("Login");
        account.setHash("Hash");
        account.setId(UUID.randomUUID());
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        account.setActive(true);

        accountRepository.save(account);
        
        var actual = provider.get(account.getId());
        
        assertThat(actual).isPresent();
        
        assertThat(actual.get().getLogin()).isEqualTo(account.getLogin());
        assertThat(actual.get().getHash()).isEqualTo(account.getHash());
    }
    
    @Test
    @DisplayName("Получение по логину. Нет логина")
    void test_get_noLogin() {
        var actual = provider.get("Login");
        
        assertThat(actual).isNotPresent();
    }
    
    @Test
    @DisplayName("Получение по идентификатору. Нет идентификатора")
    void test_get_id_noId() {
        var actual = provider.get(UUID.randomUUID());
        
        assertThat(actual).isNotPresent();
    }
    
    @Test
    @DisplayName("Установка пароля")
    void test_setPassword() {
        var account = AccountDto.builder()
                .login("Login")
                .hash("New hash")
                .build();
        
        var accountDb = new AccountRecord();
    
        accountDb.setLogin("Login");
        accountDb.setHash("Hash");
        accountDb.setId(UUID.randomUUID());
        accountDb.setNumberOfLoginAttempts(0);
        accountDb.setAuthType(AuthType.BASIC.name());
        accountDb.setActive(true);

        accountRepository.save(accountDb);
        
        provider.setPassword(account, "New hash", false);
        
        var actual = accountRepository.findAll().getFirst();
        assertThat(actual.getHash()).isEqualTo("New hash");
    }
    
    @Test
    @DisplayName("Деактивировать")
    void test_deactivate() {
        var accountDb = new AccountRecord();
    
        accountDb.setLogin("Login");
        accountDb.setHash("Hash");
        accountDb.setId(UUID.randomUUID());
        accountDb.setNumberOfLoginAttempts(0);
        accountDb.setAuthType(AuthType.BASIC.name());
        accountDb.setActive(true);

        accountRepository.save(accountDb);
        
        provider.deactivate(AccountDto.builder().login("Login").build());
        
        assertThat(accountRepository.findAll().getFirst().getActive()).isFalse();
    }

    @Test
    @DisplayName("Получение по идентификатору")
    void test_get_id_allActiveness() {
        var account = new AccountRecord();

        account.setLogin("Login");
        account.setHash("Hash");
        account.setId(UUID.randomUUID());
        account.setActive(false);
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account);

        var account2 = new AccountRecord();

        account2.setLogin("Login2");
        account2.setHash("Hash2");
        account2.setId(UUID.randomUUID());
        account2.setActive(true);
        account2.setNumberOfLoginAttempts(0);
        account2.setAuthType(AuthType.BASIC.name());

        accountRepository.save(account2);

        var actual = provider.getAllActiveness(account.getId());

        assertThat(actual).isPresent();

        assertThat(actual.get().getLogin()).isEqualTo(account.getLogin());
        assertThat(actual.get().getHash()).isEqualTo(account.getHash());
        assertThat(actual.get().isActive()).isEqualTo(account.getActive());

        actual = provider.getAllActiveness(account2.getId());

        assertThat(actual).isPresent();

        assertThat(actual.get().getLogin()).isEqualTo(account2.getLogin());
        assertThat(actual.get().getHash()).isEqualTo(account2.getHash());
        assertThat(actual.get().isActive()).isEqualTo(account2.getActive());
    }

    @Test
    @DisplayName("Получение по идентификатору. Нет идентификатора")
    void test_get_id_noId_allActiveness() {
        var actual = provider.getAllActiveness(UUID.randomUUID());

        assertThat(actual).isNotPresent();
    }
}