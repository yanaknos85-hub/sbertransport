package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.DepartmentRepository;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@EmbeddedPostgres
@Transactional
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Сервис по работе с подразделениями.")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class DepartmentServiceImplTest {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    private DepartmentService service;
    
    @BeforeEach
    void setup() {
        service = new DepartmentServiceImpl(departmentRepository);
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var department = Department.builder().id(UUID.randomUUID())
                .organizationId(UUID.randomUUID()).build();
        
        departmentRepository.save(department);
        
        var actual = service.get(department.getId()).orElseThrow();
        
        assertThat(actual.getOrganizationId()).isEqualTo(department.getOrganizationId());
    }
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        var department = Department.builder().id(UUID.randomUUID())
                                   .organizationId(UUID.randomUUID()).build();
    
        var actual = service.save(department);
    
        assertThat(actual.getOrganizationId()).isEqualTo(department.getOrganizationId());
        assertThat(actual.getId()).isEqualTo(department.getId());
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var department = Department.builder().id(UUID.randomUUID())
                                   .organizationId(UUID.randomUUID()).build();
    
        departmentRepository.save(department);
        
        assertThat(departmentRepository.count()).isEqualTo(1);
    
        service.delete(department.getId());
    
        assertThat(departmentRepository.count()).isZero();
    }
    
}