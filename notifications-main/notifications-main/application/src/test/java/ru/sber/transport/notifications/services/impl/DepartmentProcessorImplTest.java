package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@DisplayName("Проверка процессора подразделений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class DepartmentProcessorImplTest {
    
    private final DepartmentService departmentService = mock(DepartmentService.class);
    
    private final NotificationService notificationService = mock(NotificationService.class);
    
    private final NotificationSettingsService settingsService = mock(NotificationSettingsService.class);
    
    private final EmployeeService employeeService = mock(EmployeeService.class);
    
    private final List<NotificationProcessor> notificationProcessors = List.of(mock(
            NotificationProcessor.class));
    
    private final Processor<Department> processor = new DepartmentProcessorImpl(departmentService,
                                                                                notificationService, settingsService,
                                                                                employeeService, notificationProcessors, new ObjectMapper());
    
    @Test
    @DisplayName("Процессинг")
    void test_process() throws JsonProcessingException {
        var data = Department.builder().id(UUID.randomUUID())
                             .departmentHeadId(UUID.randomUUID())
                             .organizationId(UUID.randomUUID())
                .build();
        
        var head = Employee.builder().departmentId(data.getId()).id(data.getDepartmentHeadId())
                .email("Email").firstName("First name").lastName("Last name").patronymic("Patronymic")
                .userId(UUID.randomUUID()).build();
        
        when(departmentService.get(data.getId())).thenReturn(Optional.of(data));
        when(employeeService.get(data.getDepartmentHeadId())).thenReturn(Optional.of(head));
        when(settingsService.get(any(UUID.class), any(NotificationClass.class),
                                 any(NotificationType.class))).then(inv -> {
            var settings = new NotificationSettings();
            settings.setParentId(inv.getArgument(0));
            settings.setNotificationClass(inv.getArgument(1));
            settings.setType(inv.getArgument(2));
            return settings;
        });
        when(notificationService.save(any(Notification.class))).then(inv -> {
            var saved = inv.getArgument(0, Notification.class);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        
        processor.process(data);
        
        var repositoryCaptor = ArgumentCaptor.forClass(Notification.class);
        var processorCaptor = ArgumentCaptor.forClass(Notification.class);
        
        verify(notificationService).save(repositoryCaptor.capture());
        verify(notificationProcessors.get(0)).process(processorCaptor.capture());
        
        assertThat(repositoryCaptor.getValue())
                .matches(actual -> actual.getReceiverId().equals(head.getId()), "Receiver ID")
                .matches(actual -> {
                    try {
                        return actual.getEntityFromJson(Department.class).getId().equals(data.getId());
                    } catch (JsonProcessingException e) {
                        log.error("Processing failed", e);
                        return false;
                    }
                }, "Department ID")
                .matches(actual -> {
                             try {
                                 return actual.getEntityFromJson(Department.class).getOrganizationId().equals(data.getOrganizationId());
                             } catch (JsonProcessingException e) {
                                 log.error("Processing failed", e);
                                 return false;
                             }
                         },
                         "Organization ID")
                .matches(actual -> {
                             try {
                                 return actual.getEntityFromJson(Department.class).getDepartmentHeadId().equals(data.getDepartmentHeadId());
                             } catch (JsonProcessingException e) {
                                 log.error("Processing failed", e);
                                 return false;
                             }
                         },
                         "Head ID");
        
        assertThat(processorCaptor.getValue())
                .matches(actual -> actual.getReceiverId().equals(head.getId()), "Receiver ID")
                .matches(actual -> {
                    try {
                        return actual.getEntityFromJson(Department.class).getId().equals(data.getId());
                    } catch (JsonProcessingException e) {
                        log.error("Processing failed", e);
                        return false;
                    }
                }, "Department ID")
                .matches(actual -> {
                             try {
                                 return actual.getEntityFromJson(Department.class).getOrganizationId().equals(data.getOrganizationId());
                             } catch (JsonProcessingException e) {
                                 log.error("Processing failed", e);
                                 return false;
                             }
                         },
                         "Organization ID")
                .matches(actual -> {
                             try {
                                 return actual.getEntityFromJson(Department.class).getDepartmentHeadId().equals(data.getDepartmentHeadId());
                             } catch (JsonProcessingException e) {
                                 log.error("Processing failed", e);
                                 return false;
                             }
                         },
                         "Head ID");
    }
}