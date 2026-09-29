package ru.sber.transport.token_generator.messaging;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;
import ru.sber.transport.token_generator.messaging.mappers.RolesMapper;
import ru.sber.transport.token_generator.messaging.mappers.RolesMapperImpl;
import ru.sber.transport.token_generator.messaging.providers.RolesProvider;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка слушателя")
class ListenerConfigTest {

    private final ListenerConfig listenerConfig = new ListenerConfig();

    private final RolesProvider rolesProvider = mock(RolesProvider.class);

    private final AccountsProvider accountsProvider = mock(AccountsProvider.class);

    private final RolesMapper rolesMapper = new RolesMapperImpl();

    @Test
    @DisplayName("Проверка добавления роли")
    void test_add_role() {
        var consumer = listenerConfig.rolesInput(rolesProvider, rolesMapper);

        var message = Instancio.of(RoleMessage.class)
                .set(Select.field(RoleMessage::deleted), false)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.code())));

        consumer.accept(rawMessage);

        var roleCaptor = ArgumentCaptor.forClass(RolesRecord.class);

        verify(rolesProvider).save(roleCaptor.capture());

        var actual = roleCaptor.getValue();

        assertThat(actual.getCode()).isEqualTo(message.code());
        assertThat(actual.getDataMaster()).isEqualTo(message.dataMaster());
    }

    @Test
    @DisplayName("Проверка добавления УЗ")
    void test_add_account() {
        var consumer = listenerConfig.accountsInput(accountsProvider);

        var message = Instancio.of(AccountMessage.class)
                .set(Select.field(AccountMessage::active), true)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        consumer.accept(rawMessage);

        var roleCaptor = ArgumentCaptor.forClass(AccountMessage.class);
        var idCaptor = ArgumentCaptor.forClass(String.class);

        verify(accountsProvider).save(idCaptor.capture(), roleCaptor.capture());

        var actual = roleCaptor.getValue();

        assertThat(actual).isEqualTo(message);
        assertThat(idCaptor.getValue()).isEqualTo(message.getId());
    }

    @Test
    @DisplayName("Проверка удаления УЗ")
    void test_delete_account() {
        var consumer = listenerConfig.accountsInput(accountsProvider);

        var message = Instancio.of(AccountMessage.class)
                .set(Select.field(AccountMessage::active), false)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId())));

        consumer.accept(rawMessage);

        var idCaptor = ArgumentCaptor.forClass(String.class);

        verify(accountsProvider).delete(idCaptor.capture());

        assertThat(idCaptor.getValue()).isEqualTo(message.getId());
    }

    @Test
    @DisplayName("Проверка изменения")
    void test_edit() {
        var consumer = listenerConfig.rolesInput(rolesProvider, rolesMapper);

        var message = Instancio.of(RoleMessage.class)
                .set(Select.field(RoleMessage::deleted), false)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.code())));

        var oldRecord = new RolesRecord();
        oldRecord.setCode(message.code());
        oldRecord.setDataMaster(!message.dataMaster());
        when(rolesProvider.get(message.code())).thenReturn(Optional.of(oldRecord));

        consumer.accept(rawMessage);

        var roleCaptor = ArgumentCaptor.forClass(RolesRecord.class);

        verify(rolesProvider).save(roleCaptor.capture());

        var actual = roleCaptor.getValue();

        assertThat(actual.getCode()).isEqualTo(message.code());
        assertThat(actual.getDataMaster()).isEqualTo(message.dataMaster());
    }

    @Test
    @DisplayName("Проверка удаления")
    void test_delete() {
        var consumer = listenerConfig.rolesInput(rolesProvider, rolesMapper);

        var message = Instancio.of(RoleMessage.class)
                .set(Select.field(RoleMessage::deleted), true)
                .create();

        var rawMessage = MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.code())));

        consumer.accept(rawMessage);

        var roleCaptor = ArgumentCaptor.forClass(String.class);

        verify(rolesProvider).delete(roleCaptor.capture());

        var actual = roleCaptor.getValue();

        assertThat(actual).isEqualTo(message.code());
    }

}