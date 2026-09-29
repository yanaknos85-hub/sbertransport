package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка класса вспомогательного класса GroupTransferHelper")
@Slf4j
@Feature("app_platform_notifications")
class GroupTransferHelperTest {

    private final NotificationSettingsService notificationSettingsService = mock(NotificationSettingsService.class);

    @DisplayName("Проверка общей валидации")
    @Test
    void commonValidation() {
        assertThat(GroupTransferHelper.commonValidation(null, null)).isTrue();

        assertThat(GroupTransferHelper.commonValidation(
                TripRequest.builder()
                        .transportType(TransportTypeEnum.TAXI)
                        .status("OLD")
                        .build(),
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status("NEW")
                        .build())).isTrue();

        assertThat(GroupTransferHelper.commonValidation(
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status("OLD")
                        .build(),
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status("OLD")
                        .build())).isTrue();


        assertThat(GroupTransferHelper.commonValidation(
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status("OLD")
                        .build(),
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status("NEW")
                        .build())).isFalse();
    }

    @DisplayName("Проверка безопастности получения настроек")
    @Test
    void safelyGetSettings() {
        var department = Department.builder()
                .organizationId(UUID.randomUUID())
                .departmentHeadId(UUID.randomUUID())
                .build();
        when(notificationSettingsService.get(any(), any(), any()))
                .thenThrow(new ru.sber.transport.exceptions.EntityNotFoundException(NotificationSettings.class,
                        NotificationClass.REQUEST_GROUP_TRANSFER + " " + NotificationType.GROUP_TRANSFER_AWAITING_SEARCH));
        assertDoesNotThrow(() -> {
            GroupTransferHelper.safelyGetSettings(notificationSettingsService,
                    department,
                    NotificationType.GROUP_TRANSFER_AWAITING_SEARCH);
        });
    }

    @DisplayName("Проверка отправки уведомления")
    @Test
    void commonNotification() throws JsonProcessingException {
        TripRequest request = TripRequest.builder().build();
        NotificationType notificationType = NotificationType.GROUP_TRANSFER_AWAITING_SEARCH;
        Employee receiver = Employee.builder()
                .departmentId(UUID.randomUUID())
                .build();
        DepartmentService departmentService = mock(DepartmentService.class);
        NotificationSettingsService notificationSettingsService = mock(NotificationSettingsService.class);
        NotificationGlobalProcessor<TripRequest> processor = mock(NotificationGlobalProcessor.class);

        when(notificationSettingsService.get(any(), any(), any()))
                .thenReturn(NotificationSettings.builder().build());

        var department = Department.builder()
                .organizationId(UUID.randomUUID())
                .departmentHeadId(UUID.randomUUID())
                .build();

        when(departmentService.get(receiver.getDepartmentId())).thenReturn(Optional.of(department));

        GroupTransferHelper.commonNotification(
                request, notificationType, receiver, departmentService, notificationSettingsService, processor);

        verify(notificationSettingsService, times(1)).get(any(), any(), any());

        verify(processor, times(1)).process(any(), any(), any());

    }


    @DisplayName("Проверка НЕ отправки уведомления")
    @Test
    void commonNotificationDepartmentOrganizationIsNull() throws JsonProcessingException {
        TripRequest request = TripRequest.builder().build();
        NotificationType notificationType = NotificationType.GROUP_TRANSFER_AWAITING_SEARCH;
        Employee receiver = Employee.builder()
                .departmentId(UUID.randomUUID())
                .build();
        DepartmentService departmentService = mock(DepartmentService.class);
        NotificationSettingsService notificationSettingsService = mock(NotificationSettingsService.class);
        NotificationGlobalProcessor<TripRequest> processor = mock(NotificationGlobalProcessor.class);

        when(notificationSettingsService.get(any(), any(), any()))
                .thenReturn(NotificationSettings.builder().build());

        var department = Department.builder()
                .organizationId(null)
                .departmentHeadId(null)
                .build();

        when(departmentService.get(receiver.getDepartmentId())).thenReturn(Optional.of(department));

        GroupTransferHelper.commonNotification(
                request, notificationType, receiver, departmentService, notificationSettingsService, processor);

        verify(notificationSettingsService, never()).get(any(), any(), any());

        verify(processor, never()).process(any(), any(), any());

    }
}