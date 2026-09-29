package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripPurposeRepository;
import ru.sber.transport.notifications.database.model.TripPurpose;
import ru.sber.transport.notifications.services.TripPurposeService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Сервис по работе с целями")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class TripPurposeServiceImplTest {

    @Autowired
    private TripPurposeRepository repository;
    
    @Autowired
    private TripPurposeService service;
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var id = UUID.randomUUID();
        
        var request = new TripPurpose();
        request.setId(id);
        request.setPurpose("Purpose");
        request.setOrganizationId(UUID.randomUUID());
        repository.save(request);
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = service.get(id);
        
        assertThat(actual)
                .matches(act -> act.getId().equals(id), "Purpose ID")
                .matches(act -> act.getPurpose().equals(request.getPurpose()), "Purpose")
                .matches(act -> act.getOrganizationId().equals(request.getOrganizationId()), "Organization ID");
    }
    
    @Test
    @DisplayName("Получение несущестующего")
    void test_get_nonExists() {
        var id = UUID.randomUUID();
    
        assertThat(repository.count()).isZero();
        
        var actual = service.get(id);
        
        assertThat(repository.count()).isEqualTo(1);
        
        var database = repository.findAll().get(0);
        
        assertThat(actual)
                .matches(act -> act.getId().equals(id), "Purpose ID");
        
        assertThat(database)
                .matches(act -> act.getId().equals(id), "Purpose ID");
    }
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        assertThat(repository.count()).isZero();
        
        var purpose = new TripPurpose();
        purpose.setId(UUID.randomUUID());
        purpose.setPurpose("Text");
        purpose.setOrganizationId(UUID.randomUUID());
        
        service.save(purpose);
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
    
        assertThat(actual)
                .matches(act -> act.getId().equals(purpose.getId()), "Purpose ID")
                .matches(act -> act.getPurpose().equals(purpose.getPurpose()), "Purpose")
                .matches(act -> act.getOrganizationId().equals(purpose.getOrganizationId()), "Organization ID");
    }
    
}