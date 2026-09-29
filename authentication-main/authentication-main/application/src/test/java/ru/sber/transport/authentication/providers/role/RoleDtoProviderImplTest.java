package ru.sber.transport.authentication.providers.role;

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
import ru.sber.transport.authentication.business.providers.RoleProvider;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.authentication.providers.role.mapper.RolesMapper;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка провайдера ролей")
class RoleDtoProviderImplTest extends AbstractDatabaseTest {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    private final RolesMapper mapper = Mappers.getMapper(RolesMapper.class);
    
    private RoleProvider roleProvider;
    
    @BeforeEach
    void setup() {
        roleProvider = new RoleProviderImpl(accountRepository, roleRepository, mapper);
    }
    
    @Test
    @DisplayName("Установка ролей")
    void test_setRoles() {
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setActive(true);
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
        account = accountRepository.save(account);

        var role = createRole("Code");
        
        role = roleRepository.save(role);
        
        roleRepository.save(createRole("CODE_1"));
        roleRepository.save(createRole("CODE_2"));

        roleRepository.add(account.getId(), role);
        
        var accountBusiness = AccountDto
                .builder().login("Login").build();
        
        roleProvider.setRoles(accountBusiness, Set.of("CODE_1", "CODE_2"));
        
        var actualRoles = roleRepository.findAllByAccountId(accountRepository.findAll().getFirst().getId());
        assertThat(actualRoles).hasSize(2);
        assertThat(actualRoles.stream().anyMatch(r -> "CODE_1".equals(r.getCode()))).isTrue();
        assertThat(actualRoles.stream().anyMatch(r -> "CODE_2".equals(r.getCode()))).isTrue();
        assertThat(actualRoles.stream().anyMatch(r -> "Code".equals(r.getCode()))).isFalse();
    }
    
    @Test
    @DisplayName("Получение ролей пользователя")
    void test_getRoles() {
        var role = createRole("Code", "Description", "Name");
    
        role = roleRepository.save(role);
    
        var account = new AccountRecord();
        account.setId(UUID.randomUUID());
        account.setLogin("Login");
        account.setHash("Hash");
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());
    
        accountRepository.save(account);
        roleRepository.add(account.getId(), role);
        
        var actual = roleProvider.getRoles(AccountDto.builder().id(account.getId()).login("Login").build());
        
        assertThat(actual).hasSize(1);
        var actualRole = actual.iterator().next();
        assertThat(actualRole.getCode()).isEqualTo("Code");
        assertThat(actualRole.getName()).isEqualTo("Name");
        assertThat(actualRole.getDescription()).isEqualTo("Description");
    }

    private RoleRecord createRole(String code) {
        return createRole(code, null, code);
    }

}