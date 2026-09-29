package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.request.service.GeoZoneService;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@Transactional
@DisplayName("Проверка сервиса геозон")
@MockitoBean(types = JwtDecoder.class)
class GeoZoneServiceTest extends KafkaTest {
    
    @Autowired
    private GeoZoneRepository repository;
    
    private GeoZoneService geoZoneService;
    
    @BeforeEach
    void setup() {
        geoZoneService = new GeoZoneServiceImpl(repository);
    }
    
    @Test
    @DisplayName("Добавление")
    void test_add() {
        assertThat(repository.count()).isEqualTo(0);
        
        var geoZone = new GeoZone();
        
        geoZone.setId(UUID.randomUUID());
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        geoZoneService.save(geoZone);
    
        assertThat(repository.count()).isEqualTo(1);
        
        var actualDb = repository.findAll().get(0);
        
        assertThat(actualDb.getId()).isEqualTo(geoZone.getId());
        assertThat(actualDb.getName()).isEqualTo(geoZone.getName());
        assertThat(actualDb.getCode()).isEqualTo(geoZone.getCode());
        assertThat(actualDb.getParentId()).isEqualTo(geoZone.getParentId());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var id = UUID.randomUUID();
        
        var geoZone = new GeoZone();
    
        geoZone.setId(id);
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        repository.save(geoZone);
        
        var actual = geoZoneService.get(id);
    
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.get().getId()).isEqualTo(geoZone.getId());
        assertThat(actual.get().getName()).isEqualTo(geoZone.getName());
        assertThat(actual.get().getCode()).isEqualTo(geoZone.getCode());
        assertThat(actual.get().getParentId()).isEqualTo(geoZone.getParentId());
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_unexists() {
        var id = UUID.randomUUID();
        
        var actual = geoZoneService.get(id);
    
        assertThat(actual.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() {
        var id = UUID.randomUUID();
        
        var geoZone = new GeoZone();
        
        geoZone.setId(id);
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
        
        repository.save(geoZone);
        
        assertThat(repository.count()).isEqualTo(1);
        
        geoZoneService.delete(geoZone);
    
        assertThat(repository.count()).isEqualTo(0);
    }
    
}