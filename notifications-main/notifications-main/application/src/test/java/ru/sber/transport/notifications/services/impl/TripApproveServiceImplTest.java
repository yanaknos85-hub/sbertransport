package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.approve.TripApproveRepository;
import ru.sber.transport.notifications.database.model.approve.TripApprove;
import ru.sber.transport.notifications.services.TripApproveService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@DisplayName("Проверка сервиса по работе с согласованиями")
@RequiredArgsConstructor
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class TripApproveServiceImplTest {
    
    @Autowired
    private TripApproveRepository repository;
    
    @Autowired
    private TripApproveService service;
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        assertThat(repository.count()).isZero();
        
        var approve = new TripApprove();
        approve.setApproverId(UUID.randomUUID());
        approve.setStatus(true);
        approve.setRequestId(UUID.randomUUID());
        
        var actual = service.save(approve);
        
        assertThat(repository.count()).isEqualTo(1);
    
        assertThat(actual.getStatus()).isEqualTo(approve.getStatus());
        assertThat(actual.getApproverId()).isEqualTo(approve.getApproverId());
        assertThat(actual.getRequestId()).isEqualTo(approve.getRequestId());
        
        var database = repository.findAll().get(0);
    
        assertThat(database.getStatus()).isEqualTo(approve.getStatus());
        assertThat(database.getApproverId()).isEqualTo(approve.getApproverId());
        assertThat(database.getRequestId()).isEqualTo(approve.getRequestId());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var approve = new TripApprove();
        approve.setApproverId(UUID.randomUUID());
        approve.setStatus(true);
        approve.setRequestId(UUID.randomUUID());
        
        repository.save(approve);
        
        var actual = service.get(approve.getRequestId());
    
        assertThat(actual.getStatus()).isEqualTo(approve.getStatus());
        assertThat(actual.getApproverId()).isEqualTo(approve.getApproverId());
        assertThat(actual.getRequestId()).isEqualTo(approve.getRequestId());
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_nonExists() {
        assertThat(repository.count()).isZero();
    
        var id = UUID.randomUUID();
        var actual = service.get(id);
        
        assertThat(repository.count()).isEqualTo(1);
        
        assertThat(actual.getStatus()).isNull();
        assertThat(actual.getApproverId()).isNull();
        assertThat(actual.getRequestId()).isEqualTo(id);
    }
    
    @Test
    @DisplayName("Поиск по идентификатору заявки")
    void test_findByRequestId() {
        var approve = new TripApprove();
        approve.setApproverId(UUID.randomUUID());
        approve.setStatus(true);
        approve.setRequestId(UUID.randomUUID());
    
        repository.save(approve);
    
        var actual = service.getByRequestId(approve.getRequestId());
    
        assertThat(actual).isPresent();
        assertThat(actual.get().getStatus()).isEqualTo(approve.getStatus());
        assertThat(actual.get().getApproverId()).isEqualTo(approve.getApproverId());
        assertThat(actual.get().getRequestId()).isEqualTo(approve.getRequestId());
    }
    
    @Test
    @DisplayName("Поиск по идентификатору заявки. Нет согласования")
    void test_findByRequestId_nonExists() {
        var actual = service.getByRequestId(UUID.randomUUID());
    
        assertThat(actual).isEmpty();
    }
    
}