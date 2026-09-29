package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.VehicleRepository;
import ru.sber.transport.notifications.database.dao.messages.contractor.DriverRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.request.Vehicle;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.enums.TripStatus;
import ru.sber.transport.notifications.messaging.message.TripMessage;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.Processor;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка процессора поездок")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class TripProcessorImplTest {

    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().modules(new JavaTimeModule()).build();

    private final NotificationService notificationService = mock(NotificationService.class);

    private final NotificationProcessor notificationProcessor = mock(NotificationProcessor.class);

    private final List<NotificationProcessor> processorList = List.of(notificationProcessor);

    private final NotificationSettingsService notificationSettings = mock(NotificationSettingsService.class);

    private final DriverRepository driverRepository = mock(DriverRepository.class);

    private final VehicleRepository vehicleRepository = mock(VehicleRepository.class);

    private final Processor<Trip> tripProcessor = new TripProcessorImpl(notificationSettings, objectMapper, notificationService, processorList, driverRepository, vehicleRepository);

    @Test
    @DisplayName("Процессинг поездки. Нет водителя")
    void test_processing_noDriver() {
        var data = Instancio.of(Trip.class)
                .set(Select.field(Trip::getPlannedShiftId), null)
                .create();
        try {
            tripProcessor.process(data);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(EntityNotFoundException.class)
                    .hasFieldOrPropertyWithValue("entityId", data.getDriver().getId())
                    .hasFieldOrPropertyWithValue("entityName", Driver.class.getSimpleName());
        }
    }

    @Test
    @DisplayName("Процессинг поездки")
    void test_processing() throws JsonProcessingException {
        var data = Instancio.of(Trip.class)
                .ignore(Select.field(Trip::getRequests))
                .ignore(Select.field(Trip::getWaypoints))
                .set(Select.field(Trip::getPlannedShiftId), null)
                .create();
        data.setStatus(TripStatus.DRIVER_ASSIGNED.name());
        var driver = Instancio.of(Driver.class)
                .set(Select.field(Driver::getId), data.getId())
                .create();
        var vehicle = Instancio.of(Vehicle.class)
                .set(Select.field(Vehicle::getId), data.getId())
                .create();
        var settings = Instancio.of(NotificationSettings.class)
                .set(Select.field(NotificationSettings::getOwnerId), driver.getId())
                .set(Select.field(NotificationSettings::getParentId), driver.getContractorId())
                .create();
        var notificationAtomic = new AtomicReference<Notification>();

        when(driverRepository.findById(any(UUID.class))).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(any(UUID.class))).thenReturn(Optional.of(vehicle));
        when(notificationSettings.get(driver.getContractorId(), driver.getId())).thenReturn(settings);
        when(notificationService.save(any(Notification.class))).thenAnswer(inv -> {
            var notification = inv.getArgument(0, Notification.class);
            notification.setId(UUID.randomUUID());
            notification.setSettings(settings);
            notificationAtomic.set(notification);
            return notification;
        });

        tripProcessor.process(data);

        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationProcessor).process(notificationCaptor.capture());

        var capturedNotification = notificationCaptor.getValue();
        var expected = notificationAtomic.get();

        assertThat(capturedNotification.getEntity()).isEqualTo(expected.getEntity());
        assertThat(capturedNotification.getSettings().getId()).isEqualTo(expected.getSettings().getId());
        assertThat(capturedNotification.getId()).isEqualTo(expected.getId());
        assertThat(capturedNotification.getReceiverId()).isEqualTo(expected.getReceiverId());
    }

    @Test
    @DisplayName("Процессинг поездки, плановый водитель")
    void test_processing_planned() throws JsonProcessingException {
        var data = Instancio.of(Trip.class)
                .ignore(Select.field(Trip::getRequests))
                .ignore(Select.field(Trip::getWaypoints))
                .create();
        data.setStatus(TripStatus.WAITING_FOR_ASSIGNMENT.name());
        var driver = Instancio.of(Driver.class)
                .set(Select.field(Driver::getId), data.getId())
                .create();
        var vehicle = Instancio.of(Vehicle.class)
                .set(Select.field(Vehicle::getId), data.getId())
                .create();
        var settings = Instancio.of(NotificationSettings.class)
                .set(Select.field(NotificationSettings::getOwnerId), driver.getId())
                .set(Select.field(NotificationSettings::getParentId), driver.getContractorId())
                .create();
        var notificationAtomic = new AtomicReference<Notification>();

        when(driverRepository.findById(any(UUID.class))).thenReturn(Optional.of(driver));
        when(vehicleRepository.findById(any(UUID.class))).thenReturn(Optional.of(vehicle));
        when(notificationSettings.get(driver.getContractorId(), driver.getId())).thenReturn(settings);
        when(notificationService.save(any(Notification.class))).thenAnswer(inv -> {
            var notification = inv.getArgument(0, Notification.class);
            notification.setId(UUID.randomUUID());
            notification.setSettings(settings);
            notificationAtomic.set(notification);
            return notification;
        });

        tripProcessor.process(data);

        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);

        verify(notificationProcessor).process(notificationCaptor.capture());

        var capturedNotification = notificationCaptor.getValue();
        var expected = notificationAtomic.get();

        assertThat(capturedNotification.getEntity()).isEqualTo(expected.getEntity());
        assertThat(capturedNotification.getSettings().getId()).isEqualTo(expected.getSettings().getId());
        assertThat(capturedNotification.getId()).isEqualTo(expected.getId());
        assertThat(capturedNotification.getReceiverId()).isEqualTo(expected.getReceiverId());
    }

}