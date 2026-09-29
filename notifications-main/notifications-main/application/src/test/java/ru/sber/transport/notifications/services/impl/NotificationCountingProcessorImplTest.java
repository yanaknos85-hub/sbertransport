package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.support.TransactionTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.ExpectedData;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.counting.CountingSettings;
import ru.sber.transport.notifications.database.model.settings.counting.CountingType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.scripting.ScriptUtils;
import ru.sber.transport.utils.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@DisplayName("Проверка процессора количественных настроек")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationCountingProcessorImplTest {
    
    private final NotificationService notificationService = mock(NotificationService.class);
    
    private final NotificationSender notificationSender = mock(NotificationSender.class);
    
    private final CountingService countingService = mock(CountingService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final MapUtils mapUtils = new MapUtils(new ScriptUtils());

    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

    private final UserNotificationSettingsService userSettingsService = mock(UserNotificationSettingsService.class);

    private final List<JpaRepository<?, ?>> repositories = List.of(
            employeeRepository
    );

    private final TransactionTemplate transactionTemplate = null;

    private final NotificationProcessor processor = new NotificationCountingProcessorImpl(
            notificationService,
            notificationSender,
            countingService,
            repositories,
            objectMapper,
            userSettingsService,
            mapUtils,
            transactionTemplate
    );
    
    @Test
    @DisplayName("Процессинг")
    void test_process() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getChannels().add(new ChannelSettings());
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
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
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
    
        var countings = List.of(createCounting(CountingType.EXACT, null, 4D));
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
        
        var receiverCaptor = ArgumentCaptor.forClass(Employee.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var messagesCaptor = ArgumentCaptor.forClass(Map.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
        
        verify(notificationSender).send(any(UUID.class), receiverCaptor.capture(), typeCaptor.capture(), messagesCaptor.capture(),
                                        dataCaptor.capture());
        
        var actualReceiver = receiverCaptor.getValue();
        var messages = messagesCaptor.getValue();
        
        assertThat(actualReceiver.getId()).isEqualTo(receiver.getId());
        assertThat(messages.keySet()).hasSize(1);
        assertThat(messages.keySet().iterator().next()).isEqualTo(ChannelType.SMS);
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Триггер не сработал")
    void test_process_withChannel_noTrigger() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(5);
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
        
        var countings = List.of(createCounting(CountingType.EXACT, null, 4D));
    
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
    
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Проценты")
    void test_process_withChannel_percent() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(4);
        request.setExpected(ExpectedData.builder().cost(8).build());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
    
        var countings = List.of(createCounting(CountingType.PERCENT,
                "request.expected.cost", .5));
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
        
        var receiverCaptor = ArgumentCaptor.forClass(Employee.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var messagesCaptor = ArgumentCaptor.forClass(Map.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
    
        verify(notificationSender).send(any(UUID.class), receiverCaptor.capture(), typeCaptor.capture(), messagesCaptor.capture(),
                                        dataCaptor.capture());
        
        var actualReceiver = receiverCaptor.getValue();
        var messages = messagesCaptor.getValue();
        
        assertThat(actualReceiver.getId()).isEqualTo(receiver.getId());
        assertThat(messages.keySet()).hasSize(1);
        assertThat(messages.keySet().iterator().next()).isEqualTo(ChannelType.SMS);
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Триггер не сработал. Проценты")
    void test_process_withChannel_noTrigger_percents() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(5);
        request.setExpected(ExpectedData.builder().cost(8).build());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
    
        var countings = List.of(createCounting(CountingType.PERCENT,
                "request.expected.cost", .5));
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
    
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Остатки")
    void test_process_withChannel_remains() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(3);
        request.setExpected(ExpectedData.builder().cost(8).build());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
    
        var countings = List.of(createCounting(CountingType.REMAINS,
                "request.expected.cost", 5D));
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
        
        var receiverCaptor = ArgumentCaptor.forClass(Employee.class);
        var typeCaptor = ArgumentCaptor.forClass(NotificationType.class);
        var messagesCaptor = ArgumentCaptor.forClass(Map.class);
        var dataCaptor = ArgumentCaptor.forClass(Map.class);
    
        verify(notificationSender).send(any(UUID.class), receiverCaptor.capture(), typeCaptor.capture(), messagesCaptor.capture(),
                                        dataCaptor.capture());
        
        var actualReceiver = receiverCaptor.getValue();
        var messages = messagesCaptor.getValue();
        
        assertThat(actualReceiver.getId()).isEqualTo(receiver.getId());
        assertThat(messages.keySet()).hasSize(1);
        assertThat(messages.keySet().iterator().next()).isEqualTo(ChannelType.SMS);
    }
    
    @Test
    @DisplayName("Процессинг с настройками канала. Триггер не сработал. Остаток")
    void test_process_withChannel_noTrigger_remains() throws JsonProcessingException {
        var receiver = new Employee();
        receiver.setId(UUID.randomUUID());
        
        var request = new TripRequest();
        request.setId(UUID.randomUUID());
        request.setPassengerCount(2);
        request.setExpected(ExpectedData.builder().cost(8).build());
        
        var tripApprove = new TripApprove();
        tripApprove.setRequest(request);
        tripApprove.setApprover(receiver);
        tripApprove.setStatus(false);
        
        var channelSettings = new ChannelSettings();
        channelSettings.setChannel(ChannelType.SMS);
        channelSettings.setText("Text");
    
        var countings = List.of(createCounting(CountingType.REMAINS,
                "request.expected.cost", 5D));
        
        var settings = new NotificationSettings();
        settings.setRestrictions(new RestrictionSettings());
        settings.getCountings().addAll(countings);
        settings.getChannels().add(channelSettings);
        
        var notification = new Notification();
        notification.setReceiverId(receiver.getId());
        notification.setSent(false);
        notification.setEntity(objectMapper.writeValueAsString(tripApprove));
        notification.setId(UUID.randomUUID());
        notification.setSettings(settings);

        when(employeeRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(notificationService.get(notification.getId())).thenReturn(notification);
        when(countingService.getOfSettings(any(NotificationSettings.class))).thenReturn(countings);
        
        processor.process(notification);
    
        verify(notificationSender, never()).send(any(UUID.class), any(Employee.class), any(NotificationType.class), anyMap(), anyMap());
    }
    
    private CountingSettings createCounting(CountingType type, String initialPropertyName, Double count) {
        var counting = new CountingSettings();
        
        counting.setType(type);
        counting.setPropertyName("request.passengerCount");
        counting.setInitialPropertyName(initialPropertyName);
        counting.setCount(count);
        
        return counting;
    }
    
}