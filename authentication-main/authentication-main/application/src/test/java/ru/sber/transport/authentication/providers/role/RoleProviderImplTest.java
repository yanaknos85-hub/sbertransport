package ru.sber.transport.authentication.providers.role;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.messaging.listeners.providers.RoleProvider;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.roles.messages.RoleMessage;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Тест провайдера ролей")
@ActiveProfiles("test")
public class RoleProviderImplTest {

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Тест удаления роли")
    void test_deleteRole() {
        var roleCode = "ROLE_TEST";
        var accountId = UUID.randomUUID();

        var role = new RoleRecord();
        role.setCode(roleCode);
        role.setName("name");
        role.setDescription("description");
        role.setDataMaster(false);

        var account = new AccountRecord();
        account.setId(accountId);
        account.setLogin("login");
        account.setHash("hash");
        account.setActive(true);
        account.setNumberOfLoginAttempts(0);
        account.setAuthType(AuthType.BASIC.name());

        roleRepository.save(role);
        accountRepository.save(account);
        roleRepository.add(accountId, role);

        var rolesInBetween = roleRepository.findAll();
        var accountRolesInBetween = roleRepository.findAllByAccountId(accountId);

        assertThat(rolesInBetween.size()).isEqualTo(1);
        assertThat(accountRolesInBetween.size()).isEqualTo(1);

        roleProvider.delete(roleCode);

        var roleOpt = roleRepository.findById(roleCode);
        var accountRoleList = roleRepository.findAllByAccountId(accountId);

        assertThat(roleOpt).isEmpty();
        assertThat(accountRoleList.size()).isEqualTo(0);
    }

    @Test
    void testSaveRole() throws JsonProcessingException {
        var objectMapper = new ObjectMapper();

        var message = new RoleMessage("CODE", "Name", "RoleDto description", List.of("EMPLOYEE"), List.of("EXTERNAL"), false, false);

        roleProvider.save(message);
        var actual = roleRepository.findAllByCode(Set.of(message.code())).getFirst();
        assertThat(actual).isNotNull();
        assertThat(actual.getCode()).isEqualTo(message.code());
        assertThat(actual.getDescription()).isEqualTo(message.description());
        assertThat(actual.getDefaultFor().data()).isEqualTo(objectMapper.writeValueAsString(message.scopes()));
        assertThat(actual.getName()).isEqualTo(message.name());
        assertThat(actual.getDataMaster()).isEqualTo(message.dataMaster());
    }
}
