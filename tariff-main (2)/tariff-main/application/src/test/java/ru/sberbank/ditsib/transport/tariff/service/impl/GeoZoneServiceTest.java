package ru.sberbank.ditsib.transport.tariff.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.service.GeoZoneService;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка сервиса геозон")
@ActiveProfiles({"test", "kafka"})
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
        
        var actualDb = repository.findAll().getFirst();
        
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
        assertThat(actual.orElseThrow().getId()).isEqualTo(geoZone.getId());
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
    
    @Test
    @DisplayName("Поиск по имени")
    void test_findByName() {
        var id = UUID.randomUUID();
    
        var geoZone = new GeoZone();
    
        geoZone.setId(id);
        geoZone.setName("Name");
        geoZone.setCode(100 + "");
        geoZone.setParentId(UUID.randomUUID());
    
        repository.save(geoZone);
        
        var actual = geoZoneService.find("Name");
        
        assertThat(actual.isPresent()).isTrue();
        assertThat(actual.orElseThrow().getId()).isEqualTo(geoZone.getId());
        assertThat(actual.get().getName()).isEqualTo(geoZone.getName());
        assertThat(actual.get().getCode()).isEqualTo(geoZone.getCode());
        assertThat(actual.get().getParentId()).isEqualTo(geoZone.getParentId());
    }
    
    @Test
    @DisplayName("Поиск по несуществующему имени")
    void test_findByName_unexists() {
        var actual = geoZoneService.find("Name");
        
        assertThat(actual.isPresent()).isFalse();
    }
    
    @Test
    @DisplayName("Получение списка по идентификаторам")
    void test_findByIds() {
        var count = 100;
        var forCheck = 25;
        var forCheckList = new ArrayList<GeoZone>();
        
        for (var i = 0; i < forCheck; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setId(UUID.randomUUID());
            geoZone.setName("Name " + i);
            geoZone.setCode(i + "");
            
            forCheckList.add(repository.save(geoZone));
        }
        
        for (var i = 0; i < count - forCheck; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setId(UUID.randomUUID());
            geoZone.setName("Name " + i);
            geoZone.setCode(i + "");
            
            repository.save(geoZone);
        }
        
        var actualList = geoZoneService.get(forCheckList.stream().map(GeoZone::getId).collect(Collectors.toSet()));
        
        assertThat(actualList.size()).isEqualTo(forCheckList.size());
        
        for (var i = 0; i < forCheck; i++) {
            var actual = actualList.get(i);
            var expected = forCheckList.get(i);
            
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getCode()).isEqualTo(expected.getCode());
            assertThat(actual.getParentId()).isEqualTo(expected.getParentId());
        }
    }
    
}