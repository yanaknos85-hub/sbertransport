package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationProcessor;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка процессора заявок по лимитам")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class ApproveLimitRequestProcessorImplTest {

    private final NotificationSettingsService notificationSettingsService = mock(NotificationSettingsService.class);
    private final EmployeeService employeeService = mock(EmployeeService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final List<NotificationProcessor> processors = List.of(mock(NotificationProcessor.class));
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApproveLimitRequestProcessorImpl
            processor = new ApproveLimitRequestProcessorImpl(notificationService, employeeService,
            notificationSettingsService, objectMapper);

    private static Stream<Arguments> typesSource() {
        return Stream.of(
                Arguments.of(LimitType.DEPARTMENT, ApprovalState.AWAITING_APPROVAL, NotificationClass.REQUEST_LIMIT_DEPARTMENT,
                        NotificationClass.REQUEST_LIMIT_DEPARTMENT.getTypes().get(0)),
                Arguments.of(LimitType.DEPARTMENT, ApprovalState.APPROVED, NotificationClass.REQUEST_LIMIT_DEPARTMENT,
                        NotificationClass.REQUEST_LIMIT_DEPARTMENT.getTypes().get(2)),
                Arguments.of(LimitType.DEPARTMENT, ApprovalState.DECLINED, NotificationClass.REQUEST_LIMIT_DEPARTMENT,
                        NotificationClass.REQUEST_LIMIT_DEPARTMENT.getTypes().get(1)),
                Arguments.of(LimitType.EMPLOYEE, ApprovalState.AWAITING_APPROVAL, NotificationClass.REQUEST_LIMIT_PERSON,
                        NotificationClass.REQUEST_LIMIT_PERSON.getTypes().get(0)),
                Arguments.of(LimitType.EMPLOYEE, ApprovalState.APPROVED, NotificationClass.REQUEST_LIMIT_PERSON,
                        NotificationClass.REQUEST_LIMIT_PERSON.getTypes().get(2)),
                Arguments.of(LimitType.EMPLOYEE, ApprovalState.DECLINED, NotificationClass.REQUEST_LIMIT_PERSON,
                        NotificationClass.REQUEST_LIMIT_PERSON.getTypes().get(1))
        );
    }

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        processor.setService(notificationService);
        processor.setProcessors(processors);
        processor.setObjectMapper(objectMapper);
    }

    @DisplayName("Новые записи добавление заявки")
    @ParameterizedTest
    @MethodSource("typesSource")
    void process_new_approve(
            LimitType limitType, ApprovalState approvalState, NotificationClass notificationClass,
            NotificationType notificationType) throws JsonProcessingException {
        var settings = new NotificationSettings();
        var employeeId = UUID.randomUUID();
        var authorId = UUID.randomUUID();
        var approveLimitRequestId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var employee = new Employee();
        employee.setId(employeeId);
        var author = new Employee();
        author.setId(authorId);

        var approveLimitRequest = new ApproveLimitRequest();
        approveLimitRequest.setId(approveLimitRequestId);
        approveLimitRequest.setLimitType(limitType);
        approveLimitRequest.setApprovalState(approvalState);
        approveLimitRequest.setOrganizationId(organizationId);
        approveLimitRequest.setEmployeeId(employeeId);
        approveLimitRequest.setAuthorId(authorId);

        when(employeeService.get(employeeId)).thenReturn(Optional.of(employee));
        when(employeeService.get(authorId)).thenReturn(Optional.of(author));
        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));

        when(notificationService.save(any())).then(invocation -> invocation.getArgument(0));
        when(notificationSettingsService.get(any(), any(), any())).then(inv -> {
            settings.setId(UUID.randomUUID());
            settings.setParentId(inv.getArgument(0, UUID.class));
            settings.setNotificationClass(inv.getArgument(1, NotificationClass.class));
            settings.setType(inv.getArgument(2, NotificationType.class));
            return settings;
        });

        processor.process(approveLimitRequest);
        var notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(processors.get(0), times(2)).process(notificationCaptor.capture());
        List<Notification> notificationList = notificationCaptor.getAllValues();
        Notification actual_1 = notificationList.get(0);
        Notification actual_2 = notificationList.get(1);
        assertThat(actual_1.getReceiverId()).isEqualTo(employeeId);
        assertThat(actual_1.getSettings().getType()).isEqualTo(notificationType);
        assertThat(actual_1.getSettings().getNotificationClass()).isEqualTo(notificationClass);
        assertThat(actual_2.getReceiverId()).isEqualTo(authorId);
        assertThat(actual_2.getSettings().getType()).isEqualTo(notificationType);
        assertThat(actual_2.getSettings().getNotificationClass()).isEqualTo(notificationClass);
    }
}