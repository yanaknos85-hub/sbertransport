package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.EmployeeRepository;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationContactService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Сервис по работе с подразделениями.")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class EmployeeServiceImplTest {
    
    @Autowired
    private EmployeeRepository repository;
    
    private EmployeeService service;

    @Autowired
    private NotificationContactService notificationContactService;
    
    @BeforeEach
    void setup() {
        service = new EmployeeServiceImpl(repository, notificationContactService);
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var employee = Employee.builder().id(UUID.randomUUID())
                                 .userId(UUID.randomUUID())
                               .email("Email")
                               .departmentId(UUID.randomUUID())
                               .firstName("First name")
                               .lastName("Last name")
                               .patronymic("Patronymic")
                               .build();
        
        repository.save(employee);
        
        var actual = service.get(employee.getId());
        
        assertThat(actual)
                .isPresent();
        assertThat(actual.get())
                .hasFieldOrPropertyWithValue("userId", employee.getUserId())
                .hasFieldOrPropertyWithValue("email", employee.getEmail())
                .hasFieldOrPropertyWithValue("departmentId", employee.getDepartmentId())
                .hasFieldOrPropertyWithValue("firstName", employee.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", employee.getLastName())
                .hasFieldOrPropertyWithValue("patronymic", employee.getPatronymic());
    }
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        var employee = Employee.builder().id(UUID.randomUUID())
                               .userId(UUID.randomUUID())
                               .email("Email")
                               .departmentId(UUID.randomUUID())
                               .firstName("First name")
                               .lastName("Last name")
                               .patronymic("Patronymic")
                               .build();
    
        assertThat(repository.count()).isZero();
        
        service.save(employee);
    
        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().get(0);
        
        assertThat(actual)
                .hasFieldOrPropertyWithValue("userId", employee.getUserId())
                .hasFieldOrPropertyWithValue("email", employee.getEmail())
                .hasFieldOrPropertyWithValue("departmentId", employee.getDepartmentId())
                .hasFieldOrPropertyWithValue("firstName", employee.getFirstName())
                .hasFieldOrPropertyWithValue("lastName", employee.getLastName())
                .hasFieldOrPropertyWithValue("patronymic", employee.getPatronymic());
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var employee = Employee.builder().id(UUID.randomUUID())
                               .userId(UUID.randomUUID())
                               .email("Email")
                               .departmentId(UUID.randomUUID())
                               .firstName("First name")
                               .lastName("Last name")
                               .patronymic("Patronymic")
                               .build();
    
        repository.save(employee);
        
        assertThat(repository.count()).isEqualTo(1);
    
        service.delete(employee.getId());
    
        assertThat(repository.count()).isZero();
    }
    
}