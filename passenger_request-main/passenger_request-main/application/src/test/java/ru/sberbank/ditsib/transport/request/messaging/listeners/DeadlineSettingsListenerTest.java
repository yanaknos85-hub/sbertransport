package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.messaging.message.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.deadline.DeadlineSettingsRepository;
import ru.sberbank.ditsib.transport.request.shared.DeadlineSettingsSharedData;

import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Тест слушателя Настроек КС")
@Transactional
class DeadlineSettingsListenerTest extends KafkaTest {
    
    @Autowired
    private DeadlineSettingsRepository settingsRepository;
    @Autowired
    @Qualifier("deadlineSettingsInput")
    private Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInput;
    
    private final DeadlineSettingsSharedData sharedData = new DeadlineSettingsSharedData();
    
    
    @BeforeEach
    void clearBefore() {
        settingsRepository.deleteAll();
    }
    
    @AfterEach
    void clear() {
        settingsRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Создание, редактирование, удаление Настроек КС - успех")
    void handle() {
        //создание
        var message = sharedData.getTestDeadlineSettingsMessage(UUID.randomUUID());
        deadlineSettingsInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(settingsRepository.count()).isEqualTo(1);
        var settings = settingsRepository.findAll().get(0);
        sharedData.checkSettingsByMessage(message, settings);
        
        //редактирование - проверка checkSettingsByMessage включает в себя проверку сarsharingJoinDeadline
        message.setOrganizationId(UUID.randomUUID());
        message.getCarsharingJoinDeadline().setUnit(ChronoUnit.MINUTES);
        message.getCarsharingJoinDeadline().setValue(100);
        //убедимся, что новые данные сообщения не совпадают с БД
        assertThat(settings.getOrganizationId()).isNotEqualTo(message.getOrganizationId());
        assertThat(settings.getCarsharingJoinDeadline().getUnit())
                .isNotEqualTo(message.getCarsharingJoinDeadline().getUnit());
        assertThat(settings.getCarsharingJoinDeadline().getValue())
                .isNotEqualTo(message.getCarsharingJoinDeadline().getValue());
        //проверим изменение после отправки сообщения
        deadlineSettingsInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(settingsRepository.count()).isEqualTo(1);
        sharedData.checkSettingsByMessage(message, settings);
        
        //удаление
        var deletedMessage = sharedData.getDeletedMessage(message.getId());
        deadlineSettingsInput.accept(MessageBuilder.withPayload(deletedMessage).build());
        assertThat(settingsRepository.count()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("Попытка удаления Настроек КС, которых нет в БД")
    void handle_deactivateIncorrectSettings() {
        //сгенерим сообщение на удаление с рандомным ID и попытаемся удалить его из БД
        var deletedMessage = sharedData.getDeletedMessage(UUID.randomUUID());
        Throwable ex = catchThrowable(() -> deadlineSettingsInput.accept(MessageBuilder.withPayload(deletedMessage).build()));
        assertThat(ex instanceof EntityNotFoundException).isTrue();
        assertThat(settingsRepository.count()).isEqualTo(0);
    }
}