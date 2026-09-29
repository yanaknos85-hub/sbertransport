package ru.sber.transport.authentication.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.UserRoleMessage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка слушателя связок пользователь-роли")
class UserRoleDtoListenerImplTest extends AbstractContextedTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private Consumer<Message<UserRoleMessage>> userRolesInput;

    @BeforeEach
    void setup() {
        for (var i = 0; i < 100; i++) {
            var role = createRole("Code" + i, "Description" + i, "Name" + i);

            roleRepository.save(role);
        }

        for (var i = 0; i < 100; i++) {
            var account = new AccountRecord();
            account.setId(UUID.randomUUID());
            account.setLogin("Login" + i);
            account.setHash("Hash" + i);
            account.setAuthType(AuthType.BASIC.name());
            account.setNumberOfLoginAttempts(1);
            account.setActive(true);

            accountRepository.save(account);
        }
    }
    
    @Test
    @DisplayName("Проверка получения сообщения")
    void test_handle() {
        var id = accountRepository.findAll().getFirst().getId();
        var message = UserRoleMessage.builder()
                                     .user(id)
                                     .roles(Set.of("Code1", "Code2", "Code3", "Code33", "Code333"))
                                     .build();

        userRolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        var actual = accountRepository.findById(id);

        assertThat(actual).isPresent();
        var roles = roleRepository.findAllByAccountId(actual.get().getId());
        assertThat(roles).hasSize(4);
        assertThat(roles.stream().anyMatch(role -> "Code1".equals(role.getCode()))).isTrue();
        assertThat(roles.stream().anyMatch(role -> "Code2".equals(role.getCode()))).isTrue();
        assertThat(roles.stream().anyMatch(role -> "Code3".equals(role.getCode()))).isTrue();
        assertThat(roles.stream().anyMatch(role -> "Code33".equals(role.getCode()))).isTrue();

    }

    @Test
    @DisplayName("Проверка получения сообщения. Нет пользователя")
    void test_handle_noUser() {
        var id = UUID.randomUUID();
        var message = UserRoleMessage.builder()
                                     .user(id)
                                     .roles(Set.of("Code1", "Code2", "Code3", "Code33", "Code333"))
                                     .build();

        userRolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
    }
}