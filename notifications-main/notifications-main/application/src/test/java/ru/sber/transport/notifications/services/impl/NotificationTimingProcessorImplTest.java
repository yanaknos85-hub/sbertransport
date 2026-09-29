package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.timing.EventType;
import ru.sber.transport.notifications.database.model.settings.timing.TimingSettings;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.utils.collections.MapUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка процессора временных настроек")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationTimingProcessorImplTest {
    
    @Captor
    private ArgumentCaptor<Map<ChannelType, String>> messagesCaptor;
    
    @Captor
    private ArgumentCaptor<Map<String, Object>> additionalData;
    
    @Captor
    private ArgumentCaptor<Employee> receiverCaptor;
    
    @Captor
    private ArgumentCaptor<NotificationType> typeCaptor;
    
    private final NotificationService notificationService = mock(NotificationService.class);
    
    private final NotificationSender notificationSender = mock(NotificationSender.class);
    
    private final TimingService timingService = mock(TimingService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final UserNotificationSettingsService userSettingsService = mock(UserNotificationSettingsService.class);

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private final List<JpaRepository<?, ?>> repositories = List.of(
            employeeRepository
    );

    private final TransactionTemplate transactionTemplate = null;

    private final MapUtils mapUtils = mock(MapUtils.class);

    private final NotificationProcessor processor = new NotificationTimingProcessorImpl(
            notificationService,
            notificationSender,
            timingService,
            repositories,
            objectMapper,
            userSettingsService,
            mapUtils,
            transactionTemplate
    );

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Test
    @DisplayName("Процессинг")
    void test_process() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequestId(request.getId());
        tripApprove.setApproverId(receiver.getId());
        tripApprove.setStatus(false);
        
        var entity = new ObjectMapper().convertValue(tripApprove, new TypeReference<Map<String, Object>>() {});
    
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getChannels().add(new ChannelSettings());
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(entity));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);
        
        when(notificationService.get(notification.getId())).thenReturn(notification);
        
        processor.process(notification);
        
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала")
    void test_process_withChannel() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getTimings().addAll(List.of(createTimings(EventType.AT_EVENT, "request.creationTime", null
                                                          )));
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(timingService.getOfSettings(any(NotificationSettings.class)))
                .then(inv -> inv.getArgument(0, NotificationSettings.class).getTimings());
        
        processor.process(notification);
        verify(notificationSender).send(any(UUID.class), receiverCaptor.capture(), typeCaptor.capture(), messagesCaptor.capture(),
                                        additionalData.capture());
        
        var actualReceiver = receiverCaptor.getValue();
        var messages = messagesCaptor.getValue();
        
        assertThat(actualReceiver.getId()).isEqualTo(receiver.getId());
        assertThat(messages.keySet()).hasSize(1);
        assertThat(messages.keySet().iterator().next()).isEqualTo(ChannelType.SMS);
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Триггер не сработал")
    void test_process_withChannel_deadline_noTrigger() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).minusDays(1));
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getTimings().addAll(List.of(createTimings(EventType.AT_EVENT, "request.creationTime", null
                                                          )));
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(timingService.getOfSettings(any(NotificationSettings.class)))
                .then(inv -> inv.getArgument(0, NotificationSettings.class).getTimings());
        
        processor.process(notification);
    
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Контрольный срок")
    void test_process_withChannel_deadline() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getTimings().addAll(List.of(createTimings(EventType.BEFORE_DEADLINE, null, "request.creationTime")));
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(timingService.getOfSettings(any(NotificationSettings.class)))
                .then(inv -> inv.getArgument(0, NotificationSettings.class).getTimings());
        
        processor.process(notification);
        verify(notificationSender).send(any(UUID.class), receiverCaptor.capture(), typeCaptor.capture(), messagesCaptor.capture(),
                                        additionalData.capture());
        
        var actualReceiver = receiverCaptor.getValue();
        var messages = messagesCaptor.getValue();
        
        assertThat(actualReceiver.getId()).isEqualTo(receiver.getId());
        assertThat(messages.keySet()).hasSize(1);
        assertThat(messages.keySet().iterator().next()).isEqualTo(ChannelType.SMS);
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Контрольный срок. Триггер не сработал")
    void test_process_withChannel_noTrigger() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).minusDays(1));
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getTimings().addAll(List.of(createTimings(EventType.BEFORE_DEADLINE, null, "request.creationTime"
                                                          )));
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(timingService.getOfSettings(any(NotificationSettings.class)))
                .then(inv -> inv.getArgument(0, NotificationSettings.class).getTimings());
        
        processor.process(notification);
    
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    private TimingSettings createTimings(
            EventType type, String propertyName, String deadlineFieldName) {
        var counting = new TimingSettings();
        
        counting.setType(type);
        counting.setTimeFieldName(propertyName);
        counting.setTimeBefore(Duration.ZERO);
        counting.setDeadlineFieldName(deadlineFieldName);
        
        return counting;
    }
    
}