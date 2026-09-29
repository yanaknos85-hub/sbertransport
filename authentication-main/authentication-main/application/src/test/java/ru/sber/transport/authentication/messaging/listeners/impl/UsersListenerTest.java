package ru.sber.transport.authentication.messaging.listeners.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.dto.Scope;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.business.use_cases.AccountCases;
import ru.sber.transport.authentication.messaging.senders.EmailSender;
import ru.sber.transport.authentication.providers.account.dao.AccountRepository;
import ru.sber.transport.authentication.providers.role.dao.RoleRepository;
import ru.sber.transport.database.authentication.tables.records.AccountRecord;
import ru.sber.transport.database.authentication.tables.records.RoleRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked"})
@UnitTest
@IsolatedTest
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Feature("app_platform_authentication")
@Transactional
@DisplayName("Проверка получения аккаунтов")
class UsersListenerTest extends AbstractContextedTest {
    @MockitoSpyBean
    private AccountCases accountCases;

    @MockitoSpyBean
    private AccountProvider accountProvider;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private EmailSender emailSender;

    @Autowired
    private Consumer<Message<List<UserMessage>>> usersInput;

    @DisplayName("Новый пользователь")
    @Test
    void handleUsers_newUser() {
        var testEmail = "email@email.ru";
        var message = new UserMessage(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                false,
                true,
                null,
                false,
                testEmail,
                "EXTERNAL",
                null,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).save(accountArgumentCaptor.capture());
        var actual = (AccountDto) accountArgumentCaptor.getValue().getFirst();
        assertThat(actual.getLogin()).isEqualTo(message.login());
        assertThat(actual.getScope().name()).isEqualTo(Scope.EMPLOYEE.name());
        assertThat(actual.getId()).isEqualTo(message.getId());

        var emailsCaptor = ArgumentCaptor.forClass(List.class);

        //noinspection unchecked
        verify(emailSender).send(emailsCaptor.capture(), nullable(String.class), anyString(), anyMap());

        assertThat(emailsCaptor.getValue()).hasSize(1);
        assertThat(emailsCaptor.getValue().getFirst())
                .isInstanceOf(String.class)
                .isEqualTo(testEmail);
    }

    @DisplayName("Новый пользователь ТУЗ")
    @ParameterizedTest
    @EnumSource(value = UserMessage.Scope.class, names = {"AUTOSERVICE_TA", "CONTRACTOR"})
    void handleUsers_newUser_TA(UserMessage.Scope scope) {
        var testEmail = "email@email.ru";
        var message = new UserMessage(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                false,
                true,
                null,
                false,
                testEmail,
                "EXTERNAL",
                scope,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).save(accountArgumentCaptor.capture());
        var actual = (AccountDto) accountArgumentCaptor.getValue().getFirst();
        assertThat(actual.getLogin()).isEqualTo(message.login());
        assertThat(actual.getScope().name()).isEqualTo(scope.name());
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getHash()).isEqualTo(message.hash());

        var emailsCaptor = ArgumentCaptor.forClass(List.class);

        //noinspection unchecked
        verify(emailSender, times(0)).send(emailsCaptor.capture(), nullable(String.class), anyString(), anyMap());

    }

    @DisplayName("Новый пользователь без логина с именем")
    @Test
    void handle_newUser_noLogin_withName() {
        var testEmail = "email@email.ru";
        var message = new UserMessage(
                UUID.randomUUID(),
                "Last",
                "First",
                "Patronymic",
                null,
                null,
                null,
                true,
                false,
                true,
                null,
                false,
                testEmail,
                "EXTERNAL",
                null,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));

        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).save(accountArgumentCaptor.capture());
        assertThat(((AccountDto) accountArgumentCaptor.getValue().getFirst()).getLogin()).isEqualTo("LastF-P");
        assertThat(((AccountDto) accountArgumentCaptor.getValue().getFirst()).getId()).isEqualTo(message.getId());

        var emailsCaptor = ArgumentCaptor.forClass(List.class);

        //noinspection unchecked
        verify(emailSender).send(emailsCaptor.capture(), nullable(String.class), anyString(), anyMap());

        assertThat(emailsCaptor.getValue()).hasSize(1);
        assertThat(emailsCaptor.getValue().getFirst())
                .isInstanceOf(String.class)
                .isEqualTo(testEmail);
    }

    @DisplayName("Новый пользователь без логина с именем. Коллизия")
    @Test
    void handle_newUser_noLogin_withName_collision() {
        var account = new AccountRecord();
        account.setLogin("LastF-P");
        account.setHash("hash");
        account.setId(UUID.randomUUID());
        account.setActive(true);
        account.setAuthType(AuthType.BASIC.name());
        account.setNumberOfLoginAttempts(0);

        accountRepository.save(account);

        var testEmail = "email@email.ru";
        var message = new UserMessage(
                UUID.randomUUID(),
                "Last",
                "First",
                "Patronymic",
                null,
                null,
                null,
                true,
                false,
                true,
                null,
                false,
                testEmail,
                "EXTERNAL",
                null,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));

        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).save(accountArgumentCaptor.capture());
        assertThat(((AccountDto) accountArgumentCaptor.getValue().getFirst()).getLogin()).isEqualTo("LastF-P1");
        assertThat(((AccountDto) accountArgumentCaptor.getValue().getFirst()).getId()).isEqualTo(message.getId());
    }

    @Transactional
    @DisplayName("Редактирование пользователя с новыми ролями")
    @Test
    void handleUsers_editUser() {
        Map<String, Boolean> collect = IntStream.of(3).boxed().collect(Collectors.toMap(value -> "Role" + value, value -> Boolean.FALSE));
        collect.entrySet().stream().map(this::createRole).forEach(roleRepository::save);
        UUID id = UUID.randomUUID();
        var initialStore = AccountDto.builder()
                .id(id)
                .email("enail")
                .login("login")
                .hash("hash")
                .active(true)
                .scope(Scope.DISPATCHER)
                .roles(collect).build();
        accountProvider.save(initialStore);

        Set<String> collect1 = IntStream.of(4, 10).boxed().map(value -> "Role" + value).collect(Collectors.toSet());
        collect1.stream().map(this::createRole).forEach(roleRepository::save);

        var message = new UserMessage(
                id,
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                false,
                true,
                collect1,
                false,
                null,
                "EXTERNAL",
                UserMessage.Scope.DISPATCHER,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        verify(emailSender, never()).send(anyList(), anyString(), anyString(), anyMap());

        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).save(accountArgumentCaptor.capture());

        var actual = (AccountDto) accountArgumentCaptor.getValue().getFirst();
        assertThat(actual.getLogin()).isEqualTo(initialStore.getLogin());
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getScope().name()).isEqualTo(message.scope().name());
        assertThat(actual.getRoles().keySet()).containsAll(collect1);
    }

    @Test
    @DisplayName("Пользователь удален")
    void handleUsers_deleteUser() {
        var id = UUID.randomUUID();

        var message = new UserMessage(
                id,
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                true,
                false,
                null,
                false,
                null,
                "EXTERNAL",
                UserMessage.Scope.DISPATCHER,
                null
        );

        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        verify(accountCases, times(1)).deactivate(List.of(id));

        var idCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases).deactivate(idCaptor.capture());
        assertThat(idCaptor.getValue().getFirst()).isEqualTo(message.getId());
    }

    @Test
    @DisplayName("Пользователь удален. Не найден")
    void handleUsers_deleteUser_notFound() {
        var id = UUID.randomUUID();

        var message = new UserMessage(
                id,
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                true,
                false,
                null,
                false,
                null,
                "EXTERNAL",
                UserMessage.Scope.DISPATCHER,
                null
        );

        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        verify(accountCases).deactivate(List.of(id));
    }

    @DisplayName("Новый пользователь. Дублирование сообщения")
    @Test
    void handleUsers_newUser_duplicateMessage() {
        var testEmail = "email@email.ru";
        var message = new UserMessage(
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                "Login",
                "Hash",
                true,
                false,
                true,
                null,
                false,
                testEmail,
                "EXTERNAL",
                null,
                null
        );
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        usersInput.accept(MessageBuilder.createMessage(List.of(message), new MessageHeaders(Map.of(KafkaHeaders.BATCH_CONVERTED_HEADERS,  List.of(Map.of("type", "EXTERNAL"))))));
        var accountArgumentCaptor = ArgumentCaptor.forClass(List.class);

        verify(accountCases, times(2)).save(accountArgumentCaptor.capture());
        var actual = (AccountDto) accountArgumentCaptor.getValue().getFirst();
        assertThat(actual.getLogin()).isEqualTo(message.login());
        assertThat(actual.getScope().name()).isEqualTo(Scope.EMPLOYEE.name());
        assertThat(actual.getId()).isEqualTo(message.getId());

        var emailsCaptor = ArgumentCaptor.forClass(List.class);

        //noinspection unchecked
        verify(emailSender).send(emailsCaptor.capture(), nullable(String.class), anyString(), anyMap());

        assertThat(emailsCaptor.getValue()).hasSize(1);
        assertThat(emailsCaptor.getValue().getFirst())
                .isInstanceOf(String.class)
                .isEqualTo(testEmail);
    }

    private RoleRecord createRole(Map.Entry<String, Boolean> entry) {
        var role = new RoleRecord();
        role.setCode(entry.getKey());
        role.setDescription(entry.getKey());
        role.setName(entry.getKey());
        role.setDataMaster(entry.getValue());
        return role;
    }

    private RoleRecord createRole(String code) {
        return createRole(new AbstractMap.SimpleEntry<>(code, true));
    }
}