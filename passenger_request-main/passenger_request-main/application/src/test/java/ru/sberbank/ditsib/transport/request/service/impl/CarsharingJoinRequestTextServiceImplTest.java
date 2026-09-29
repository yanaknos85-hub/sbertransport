package ru.sberbank.ditsib.transport.request.service.impl;

import io.qameta.allure.Feature;
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
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingJoinRequestTextRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;
import ru.sberbank.ditsib.transport.request.service.DeadlineSettingsService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка сохранения текстовых полей по умолчанию")
class CarsharingJoinRequestTextServiceImplTest extends KafkaTest {
    
    @Autowired
    private CarsharingJoinRequestTextRepository textRepository;
    
    @MockitoBean
    private DeadlineSettingsService deadlineSettingsService;
    
    @Test
    @DisplayName("Если настройки не найдены")
    void setToDefaults() {
        
        var cut = new CarsharingJoinRequestTextServiceImpl(textRepository, deadlineSettingsService);
        
        var organizationId = UUID.randomUUID();
        cut.setToDefaults(organizationId);
        
        var result = textRepository.findByOrganizationId(organizationId).get();
        
        assertEquals(organizationId, result.getOrganizationId());
        
    }
    
    @Test
    @DisplayName("Формы заявки в БД нет")
    void getOrThrowException() {
        var cut = new CarsharingJoinRequestTextServiceImpl(textRepository, deadlineSettingsService);
        
        var organizationId = UUID.randomUUID();
        var result = assertThrows(EntityNotFoundException.class, ()->cut.getOrThrowException(organizationId));
    }
    
    @Test
    @DisplayName("Сохранение текстовых полей в БД")
    void save() {
        var cut = new CarsharingJoinRequestTextServiceImpl(textRepository, deadlineSettingsService);
        
        var organizationId = UUID.randomUUID();
        CarsharingJoinRequestText text = CarsharingJoinRequestText.builder().organizationId(organizationId).build();
        cut.save(text);
        var result = textRepository.findByOrganizationId(organizationId).get();
        
        assertEquals(organizationId, result.getOrganizationId());
    }
}