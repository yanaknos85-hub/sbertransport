package ru.sber.transport.authentication.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.messaging.listeners.providers.RoleProvider;
import ru.sber.transport.roles.messages.RoleMessage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.authentication.business.dto.Scope.AUTOSERVICE;
import static ru.sberbank.ditsib.transport.messaging.messages.UserMessage.Scope.AUTOSERVICE_TA;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка получения ролей")
@MockitoBean(types = ru.sber.transport.authentication.business.providers.RoleProvider.class)
class RolesListenerTest extends AbstractContextedTest {

    private final RoleProvider roleProvider = mock(RoleProvider.class);

    private final ListenersConfig config = new ListenersConfig();

    private final Consumer<Message<RoleMessage>> rolesInput = config.rolesInput(roleProvider);
    
    @Test
    @DisplayName("Получена новая роль")
    void handleRole() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", null, List.of("EXTERNAL"), false, false);
        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        
        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);
        
        verify(roleProvider).save(messageCaptor.capture());
    
        var actualMessage = messageCaptor.getValue();
        
        assertThat(actualMessage.code()).isEqualTo(message.code());
        assertThat(actualMessage.description()).isEqualTo(message.description());
        assertThat(actualMessage.name()).isEqualTo(message.name());
    }

    @Test
    @DisplayName("Получена новая внутренняя роль")
    void handleRole_internal() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", null, List.of("INTERNAL"), false, false);

        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        verify(roleProvider, never()).save(any());
    }

    @Test
    @DisplayName("Получена новая роль без указания зоны использования")
    void handleRole_default() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", null, List.of(), false, false);

        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        verify(roleProvider, never()).save(any());
    }

    @Test
    @DisplayName("Получена новая роль c зоной использования")
    void handleRole_scope() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", List.of(AUTOSERVICE.name(), AUTOSERVICE_TA.name()), List.of("EXTERNAL"), true, false);

        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));

        var messageCaptor = ArgumentCaptor.forClass(RoleMessage.class);

        verify(roleProvider).save(messageCaptor.capture());

        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.code()).isEqualTo(message.code());
        assertThat(actualMessage.description()).isEqualTo(message.description());
        assertThat(actualMessage.name()).isEqualTo(message.name());
        assertThat(actualMessage.scopes()).isEqualTo(message.scopes());
        assertThat(actualMessage.exclusives()).isEqualTo(message.exclusives());

    }

    @Test
    @DisplayName("Удалена роль")
    void handleRole_deleted() {
        var message = new RoleMessage("CODE", "Name", "RoleDto description", null, null, false, true);

        rolesInput.accept(MessageBuilder.createMessage(message, new MessageHeaders(Map.of(KafkaHeaders.RECEIVED_KEY, message.getId()))));
        
        var codeCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(roleProvider).delete(codeCaptor.capture());
        
        assertThat(codeCaptor.getValue()).isEqualTo(message.code());
    }
}