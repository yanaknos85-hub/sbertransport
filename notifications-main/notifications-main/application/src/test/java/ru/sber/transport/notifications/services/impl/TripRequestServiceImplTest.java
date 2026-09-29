package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripRequestRepository;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.TripRequestService;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(properties = "spring.main.lazy-initialization=true")
@DisplayName("Сервис по работе с заявками на поездку")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class TripRequestServiceImplTest {

    @Autowired
    private TripRequestRepository repository;
    
    @Autowired
    private TripRequestService service;
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var requestId = UUID.randomUUID();
        
        var request = new TripRequest();
        request.setId(requestId);
        request.setPassengerId(UUID.randomUUID());
        request.setTransportType(TransportTypeEnum.CARSHARING);
        request.setPurposeId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        repository.save(request);
        
        var actual = service.get(requestId);
        
        assertThat(actual)
                .matches(act -> act.getId().equals(requestId), "Request ID")
                .matches(act -> act.getPassengerId().equals(request.getPassengerId()), "Passenger ID")
                .matches(act -> act.getTransportType().equals(request.getTransportType()), "Transport type")
                .matches(act -> act.getPurposeId().equals(request.getPurposeId()), "Purpose ID")
                .matches(act -> act.getAuthorId().equals(request.getAuthorId()), "Author ID");
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_nonExists() {
        assertThatThrownBy(() -> service.get(UUID.randomUUID())).isInstanceOf(NoSuchElementException.class);
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var requestId = UUID.randomUUID();
        
        var request = new TripRequest();
        request.setId(requestId);
        request.setPassengerId(UUID.randomUUID());
        request.setTransportType(TransportTypeEnum.CARSHARING);
        request.setPurposeId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        repository.save(request);
        
        assertThat(repository.count()).isEqualTo(1);
        
        service.delete(requestId);
    
        assertThat(repository.count()).isZero();
    }
    
    @Test
    @DisplayName("Сохранение")
    void test_save() {
        var requestId = UUID.randomUUID();
        var request = new TripRequest();
        request.setId(requestId);
        request.setPassengerId(UUID.randomUUID());
        request.setTransportType(TransportTypeEnum.CARSHARING);
        request.setPurposeId(UUID.randomUUID());
        request.setAuthorId(UUID.randomUUID());
        
        assertThat(repository.count()).isZero();
        
        service.save(request);
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().get(0);
        
        assertThat(actual)
                .matches(act -> act.getId().equals(requestId), "Request ID")
                .matches(act -> act.getPassengerId().equals(request.getPassengerId()), "Passenger ID")
                .matches(act -> act.getTransportType().equals(request.getTransportType()), "Transport type")
                .matches(act -> act.getPurposeId().equals(request.getPurposeId()), "Purpose ID")
                .matches(act -> act.getAuthorId().equals(request.getAuthorId()), "Author ID");
    }
    
}