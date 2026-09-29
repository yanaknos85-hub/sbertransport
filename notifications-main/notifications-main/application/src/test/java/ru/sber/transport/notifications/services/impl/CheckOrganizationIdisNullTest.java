package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.Address;
import ru.sber.transport.notifications.database.model.request.SharedRide;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.request.Waypoint;
import ru.sber.transport.notifications.dto.contractor.DriverDTO;
import ru.sber.transport.notifications.dto.contractor.VehicleDTO;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferAwaitingApprovalCommand;
import ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferDriverArrivedCommand;
import ru.sber.transport.notifications.services.impl.commands.groupTransfer.GroupTransferRequestDriverFoundNotificationCommand;
import ru.sber.transport.notifications.services.impl.commands.personal.*;
import ru.sber.transport.notifications.services.impl.commands.public_request.PublicTripRequestApprovingStatusNotificationCommand;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@DisplayName("Проверка command на departmant.organizationId != null")
@Slf4j
@ExtendWith(MockitoExtension.class)
class CheckOrganizationIdisNullTest {
    private static final String CUSTOMER_PHONE_FIELD = "addContactPhone";
    @Mock
    private EmployeeService employeeService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private NotificationSettingsService notificationSettingsService;
    @Mock
    private NotificationGlobalProcessor<TripRequest> processor;

    @InjectMocks
    private GroupTransferRequestDriverFoundNotificationCommand requestDriverFoundNotificationCommand;
    @InjectMocks
    private GroupTransferAwaitingApprovalCommand awaitingApprovalCommand;
    @InjectMocks
    private GroupTransferDriverArrivedCommand driverArrivedCommand;
    @InjectMocks
    private PersonalCoopTripAttachmentNotificationCommand personalCoopTripAttachmentNotificationCommand;
    @InjectMocks
    private PersonalCoopTripChangesNotificationCommand personalCoopTripChangesNotificationCommand;

    @InjectMocks
    private PersonalPaymentAwaitingNotificationCommand personalPaymentAwaitingNotificationCommand;

    @InjectMocks
    private PersonalPaymentStatusNotificationCommand personalPaymentStatusNotificationCommand;

    @InjectMocks
    private PersonalTripRequestApprovingNotificationCommand personalTripRequestApprovingNotificationCommand;

    @InjectMocks
    private PersonalTripRequestApprovingStatusNotificationCommand personalTripRequestApprovingStatusNotificationCommand;

    @InjectMocks
    private PersonalTripRequestFinishedNotificationCommand personalTripRequestFinishedNotificationCommand;

    @InjectMocks
    private PersonalTripWaypointArrivedNotificationCommand personalTripWaypointArrivedNotificationCommand;

    @InjectMocks
    private PublicTripRequestApprovingStatusNotificationCommand publicTripRequestApprovingStatusNotificationCommand;

    private Employee receiver;
    private TripRequest current;
    private Department department;
    private TripApprove approve;

    @BeforeEach
    void setUp(){
        department = Department.builder()
                .organizationId(null)
                .departmentHeadId(null)
                .build();
        receiver = Employee.builder()
                .id(UUID.randomUUID())
                .departmentId(UUID.randomUUID())
                .build();
        current = new TripRequest();
        current.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(50));
        current.setTimeZone(TimeZone.getDefault().toZoneId().toString());
        current.setApprovalState("APPROVED");
        current.setWaypoints(
                List.of(
                        Waypoint.builder().address(
                                        Address.builder().region("Moscow").build())
                                .build(),
                        Waypoint.builder().address(
                                        Address.builder().region("PetersBurg").build())
                                .build()));
        current.setDriver(DriverDTO.builder()
                .driverFio("Иванов Иван Иванович")
                .build());
        current.setVehicle(VehicleDTO.builder().carInfo("Полосатое такси").build());
        current.setAuthorId(receiver.getId());
        current.setPassengerId(receiver.getId());
        current.getInformation().put(CUSTOMER_PHONE_FIELD, "+789998998989");
        current.setSharedRide(SharedRide.builder().id(UUID.randomUUID()).build());

        TripRequest request = new TripRequest();
        request.setPassenger(receiver);

        approve = new TripApprove();
        approve.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(50));
        approve.setApproverId(receiver.getId());
        approve.setPassengerId(receiver.getId());
        approve.setRequest(request);
        when(employeeService.get(any())).thenReturn(Optional.of(receiver));

        when(departmentService.get(receiver.getDepartmentId())).thenReturn(Optional.of(department));
    }

    @DisplayName("Проверка НЕ отправки уведомления GroupTransferRequestDriverFoundNotificationCommand")
    @Test
    void testGroupTransferAwaitingApprovalCommand() throws JsonProcessingException {

        when(employeeService.getByIds(any())).thenReturn(List.of(receiver));

        awaitingApprovalCommand.sendNotification(approve);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());

    }

    @DisplayName("Проверка НЕ отправки уведомления GroupTransferRequestDriverFoundNotificationCommand")
    @Test
    void testGroupTransferRequestDriverFoundNotificationCommand() throws JsonProcessingException {
        requestDriverFoundNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления DriverArrivedCommand")
    @Test
    void testDriverArrivedCommand() throws JsonProcessingException {
        driverArrivedCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalCoopTripAttachmentNotificationCommand")
    @Test
    void testPersonalCoopTripAttachmentNotificationCommand() throws JsonProcessingException {
        when(employeeService.getPassengersByRequestIds(any())).thenReturn(List.of(receiver));
        personalCoopTripAttachmentNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalCoopTripChangesNotificationCommand")
    @Test
    void testPersonalCoopTripChangesNotificationCommand() throws JsonProcessingException {

        current.setPassengerId(UUID.randomUUID());

        when(employeeService.getPassengersByRequestIds(any())).thenReturn(List.of(receiver));
        personalCoopTripChangesNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalPaymentAwaitingNotificationCommand")
    @Test
    void testPersonalPaymentAwaitingNotificationCommand() throws JsonProcessingException {
        personalPaymentAwaitingNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalPaymentStatusNotificationCommand")
    @Test
    void testPersonalPaymentStatusNotificationCommand() throws JsonProcessingException {
        personalPaymentStatusNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalTripRequestApprovingNotificationCommand")
    @Test
    void testPersonalTripRequestApprovingNotificationCommand() throws JsonProcessingException {
        when(employeeService.getByIds(anyList())).thenReturn(List.of(receiver));
        personalTripRequestApprovingNotificationCommand.sendNotification(approve);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalTripRequestApprovingStatusNotificationCommand")
    @Test
    void testPersonalTripRequestApprovingStatusNotificationCommand() throws JsonProcessingException {
        personalTripRequestApprovingStatusNotificationCommand.sendNotification(approve);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalTripRequestFinishedNotificationCommand")
    @Test
    void testPersonalTripRequestFinishedNotificationCommand() throws JsonProcessingException {
        personalTripRequestFinishedNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PersonalTripWaypointArrivedNotificationCommand")
    @Test
    void testPersonalTripWaypointArrivedNotificationCommand() throws JsonProcessingException {
        personalTripWaypointArrivedNotificationCommand.sendNotification(current);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }

    @DisplayName("Проверка НЕ отправки уведомления PublicTripRequestApprovingStatusNotificationCommand")
    @Test
    void testPublicTripRequestApprovingStatusNotificationCommand() throws JsonProcessingException {

        publicTripRequestApprovingStatusNotificationCommand.sendNotification(approve);

        verify(notificationSettingsService, never()).get(any(), any(), any());
        verify(processor, never()).process(any(), any(), any());
    }
}
