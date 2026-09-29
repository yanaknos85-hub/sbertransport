package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.approve.ApproveStatus;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Проверка обработки сообщений о согласованиях")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class TripApproveProcessorImplTest {
    
    private final NotificationSettingsService notificationSettingsService = mock(NotificationSettingsService.class);
    
    private final DepartmentService departmentService = mock(DepartmentService.class);
    
    private final EmployeeService employeeService = mock(EmployeeService.class);
    
    private final TripRequestService tripRequestService = mock(TripRequestService.class);
    
    private final NotificationService notificationService = mock(NotificationService.class);
    
    private final List<NotificationProcessor> processors = List.of(mock(NotificationProcessor.class));
    
    private final Processor<TripApprove> processor = new TripApproveProcessorImpl(notificationSettingsService,
                                                                        departmentService, employeeService,
                                                                        tripRequestService);
    
    private static Stream<Arguments> typesSource() {
        return Stream.of(
                Arguments.of(TransportTypeEnum.TAXI, NotificationClass.REQUEST_TAXI,
                             NotificationClass.REQUEST_TAXI.getTypes().get(0)),
                Arguments.of(TransportTypeEnum.BICYCLE, NotificationClass.REQUEST_BICYCLE,
                             NotificationClass.REQUEST_BICYCLE.getTypes().get(0)),
                Arguments.of(TransportTypeEnum.CARSHARING, NotificationClass.REQUEST_CAR_SHARING,
                             NotificationClass.REQUEST_CAR_SHARING.getTypes().get(0)),
                Arguments.of(TransportTypeEnum.PERSONAL, NotificationClass.REQUEST_PERSONAL,
                             NotificationClass.REQUEST_PERSONAL.getTypes().get(0)),
                Arguments.of(TransportTypeEnum.PUBLIC, NotificationClass.REQUEST_PUBLIC,
                             NotificationClass.REQUEST_PUBLIC.getTypes().get(0)),
                Arguments.of(TransportTypeEnum.SCOOTER, NotificationClass.REQUEST_SCOOTER,
                             NotificationClass.REQUEST_SCOOTER.getTypes().get(0))
                        );
    }
    
    @BeforeEach
    void setup() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        ((TripApproveProcessorImpl) processor).setService(notificationService);
        ((TripApproveProcessorImpl) processor).setProcessors(processors);
        ((TripApproveProcessorImpl) processor).setObjectMapper(objectMapper);
    }
    
    @ParameterizedTest
    @DisplayName("Процессинг")
    @MethodSource("typesSource")
    void test_processing(TransportTypeEnum transportTypeEnum, NotificationClass notificationClass,
                         NotificationType notificationType) throws JsonProcessingException {
        var authorId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var purposeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        
        var author = new Employee();
        author.setId(authorId);
        author.setDepartmentId(departmentId);
        var approver = new Employee();
        approver.setId(approverId);
        approver.setDepartmentId(departmentId);
        var passenger = new Employee();
        passenger.setId(passengerId);
        passenger.setDepartmentId(departmentId);
        var request = new TripRequest();
        request.setId(requestId);
        request.setPurposeId(purposeId);
        request.setAuthorId(authorId);
        request.setPassengerId(passengerId);
        request.setTransportType(transportTypeEnum);
    
        var tripApprove = new TripApprove();
        tripApprove.setRequestId(requestId);
        tripApprove.setApproverId(approverId);
        var build = Department.builder().id(departmentId).organizationId(UUID.randomUUID()).build();
    
        var tripApproveWithStatus = new TripApprove();
        tripApproveWithStatus.setRequestId(requestId);
        tripApproveWithStatus.setApproverId(approverId);
        tripApproveWithStatus.setPassengerId(passengerId);
        tripApproveWithStatus.setPassenger(passenger);
        tripApproveWithStatus.setApproveStatus(ApproveStatus.APPROVED);
        
        when(tripRequestService.get(any(UUID.class))).thenReturn(request);
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(employeeService.get(approverId)).thenReturn(Optional.of(approver));
        when(departmentService.get(any(UUID.class))).thenReturn(Optional.of(build));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));
        when(notificationSettingsService.get(any(), any(), any())).then(inv -> {
            var settings = new NotificationSettings();
            settings.setId(UUID.randomUUID());
            settings.setParentId(inv.getArgument(0, UUID.class));
            settings.setNotificationClass(inv.getArgument(1, NotificationClass.class));
            settings.setType(inv.getArgument(2, NotificationType.class));
            return settings;
        });
        
        processor.process(tripApprove);
        verify(notificationSettingsService, times(1)).get(any(), any(), any());
        
        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        
        verify(processors.get(0)).process(notificationCaptor.capture());
        
        var actual = notificationCaptor.getValue();
        
        assertThat(actual.getReceiverId()).isEqualTo(approverId);
        assertThat(actual.getSettings().getType()).isEqualTo(notificationType);
        assertThat(actual.getSettings().getNotificationClass()).isEqualTo(notificationClass);
        
        processor.process(tripApproveWithStatus);
    
        var notificationCaptor_1 = ArgumentCaptor.forClass(Notification.class);
        verify(processors.get(0), times(3)).process(notificationCaptor_1.capture());
    
        var actual_1 = notificationCaptor_1.getAllValues().get(1);
        assertThat(actual_1.getReceiverId()).isEqualTo(approverId);
        assertThat(actual_1.getSettings().getType()).isEqualTo(NotificationType.COOP_TRIP_ATTACHMENT_APPROVE);
        assertThat(actual_1.getSettings().getNotificationClass()).isEqualTo(NotificationClass.REQUEST_PERSONAL);
    
        var actual_2 = notificationCaptor_1.getAllValues().get(2);
        assertThat(actual_2.getReceiverId()).isEqualTo(passengerId);
        assertThat(actual_2.getSettings().getType()).isEqualTo(NotificationType.COOP_TRIP_ATTACHMENT_APPROVE);
        assertThat(actual_2.getSettings().getNotificationClass()).isEqualTo(NotificationClass.REQUEST_PERSONAL);
    }
    
    @Test
    @DisplayName("Процессинг. Статус - нет согласования")
    void test_processing_noApproving() throws JsonProcessingException {
        var authorId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var purposeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
    
        var author = new Employee();
        author.setId(authorId);
        author.setDepartmentId(departmentId);
        var approver = new Employee();
        approver.setId(approverId);
        approver.setDepartmentId(departmentId);
        var passenger = new Employee();
        passenger.setId(passengerId);
        passenger.setDepartmentId(departmentId);
        var request = new TripRequest();
        request.setId(requestId);
        request.setPurposeId(purposeId);
        request.setAuthorId(authorId);
        request.setPassengerId(passengerId);
        request.setTransportType(TransportTypeEnum.TAXI);
        var build = Department.builder().id(departmentId).organizationId(UUID.randomUUID()).build();
    
        var tripApprove = new TripApprove();
        tripApprove.setApproverId(approverId);
        tripApprove.setRequestId(requestId);
        
        when(tripRequestService.get(any(UUID.class))).thenReturn(request);
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(employeeService.get(approverId)).thenReturn(Optional.of(approver));
        when(departmentService.get(any(UUID.class))).thenReturn(Optional.of(build));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));
        when(notificationSettingsService.get(any(), any(), any())).then(inv -> {
            var settings = new NotificationSettings();
            settings.setId(UUID.randomUUID());
            settings.setParentId(inv.getArgument(0, UUID.class));
            settings.setNotificationClass(inv.getArgument(1, NotificationClass.class));
            settings.setType(inv.getArgument(2, NotificationType.class));
            return settings;
        });
        
        processor.process(tripApprove);
        
        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    
        verify(processors.get(0)).process(notificationCaptor.capture());
    
        var actual = notificationCaptor.getValue();
    
        assertThat(actual.getReceiverId()).isEqualTo(approverId);
        assertThat(actual.getSettings().getNotificationClass()).isEqualTo(NotificationClass.REQUEST_TAXI);
    }
    
    @Test
    @DisplayName("Процессинг. Неверный тип транспорта")
    void test_processing_wrongType() {
        var authorId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var purposeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
    
        var author = new Employee();
        author.setId(authorId);
        author.setDepartmentId(departmentId);
        var approver = new Employee();
        approver.setId(approverId);
        approver.setDepartmentId(departmentId);
        var passenger = new Employee();
        passenger.setId(passengerId);
        passenger.setDepartmentId(departmentId);
        var request = new TripRequest();
        request.setId(requestId);
        request.setPurposeId(purposeId);
        request.setAuthorId(authorId);
        request.setPassengerId(passengerId);
        request.setTransportType(TransportTypeEnum.WALK);
        request.setId(purposeId);
    
        var tripApprove = new TripApprove();
        tripApprove.setApprover(approver);
        tripApprove.setRequest(request);
        tripApprove.setRequestId(requestId);
        tripApprove.setApproverId(approverId);
        var build = Department.builder().id(departmentId).organizationId(UUID.randomUUID()).build();
        
        when(tripRequestService.get(any(UUID.class))).thenReturn(request);
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(employeeService.get(approverId)).thenReturn(Optional.of(approver));
        when(departmentService.get(any(UUID.class))).thenReturn(Optional.of(build));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));
        
        assertThatThrownBy(() -> processor.process(tripApprove))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("There is no notification type for request with type WALK");
    }
    
    @Test
    @DisplayName("Процессинг. Нет типа транспорта")
    void test_processing_noType() {
        var authorId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var purposeId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
    
        var author = new Employee();
        author.setId(authorId);
        author.setDepartmentId(departmentId);
        var approver = new Employee();
        approver.setId(approverId);
        approver.setDepartmentId(departmentId);
        var passenger = new Employee();
        passenger.setId(passengerId);
        passenger.setDepartmentId(departmentId);
        var request = new TripRequest();
        request.setId(requestId);
        request.setPurposeId(purposeId);
        request.setAuthorId(authorId);
        request.setPassengerId(passengerId);
        request.setId(purposeId);
    
        var tripApprove = new TripApprove();
        tripApprove.setApprover(approver);
        tripApprove.setRequest(request);
        
        var build = Department.builder().id(departmentId).organizationId(UUID.randomUUID()).build();
        
        when(tripRequestService.get(any(UUID.class))).thenReturn(request);
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(employeeService.get(approverId)).thenReturn(Optional.of(approver));
        when(departmentService.get(any(UUID.class))).thenReturn(Optional.of(build));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));

        try {
            processor.process(tripApprove);
        } catch (Exception e) {
            assertThat(e)
                    .isInstanceOf(NullPointerException.class);
        }
    }
    
    @SneakyThrows
    @Test
    @DisplayName("Процессинг. Нет заявки")
    void test_processing_noRequest() {
        var authorId = UUID.randomUUID();
        var approverId = UUID.randomUUID();
        var passengerId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
    
        var author = new Employee();
        author.setId(authorId);
        author.setDepartmentId(departmentId);
        var approver = new Employee();
        approver.setId(approverId);
        approver.setDepartmentId(departmentId);
        var passenger = new Employee();
        passenger.setId(passengerId);
        passenger.setDepartmentId(departmentId);
    
        var tripApprove = new TripApprove();
        tripApprove.setApprover(approver);
        tripApprove.setRequestId(requestId);
        
        var build = Department.builder().id(departmentId).organizationId(UUID.randomUUID()).build();
        
        when(tripRequestService.get(any(UUID.class))).thenThrow(new NoSuchElementException());
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(employeeService.get(passengerId)).thenReturn(Optional.of(passenger));
        when(employeeService.get(approverId)).thenReturn(Optional.of(approver));
        when(departmentService.get(any(UUID.class))).thenReturn(Optional.of(build));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));
        
        processor.process(tripApprove);

        verify(notificationService, never()).save(any());
    }
    
}