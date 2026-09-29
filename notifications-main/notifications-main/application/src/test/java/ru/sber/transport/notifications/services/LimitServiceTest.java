package ru.sber.transport.notifications.services;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.notifications.database.dao.messages.limits.LimitRepository;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.services.impl.LimitServiceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Проверка сервиса лимитов")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@EmbeddedPostgres
@Transactional
class LimitServiceTest {
    
    @Autowired
    private LimitRepository limitRepository;
    
    private LimitService limitService;
    
    @BeforeEach
    void setup() {
        limitService = new LimitServiceImpl(limitRepository);
    }
    
    @Test
    @DisplayName("Добавление")
    void test_add() {
        var limit = new Limit();
        limit.setId(UUID.randomUUID());
        limit.setLimitId(UUID.randomUUID());
        limit.setLimitType("Limit type");
        limit.setLimitStatus("Limit status");
        limit.setLimitSharingType("Limit sharing type");
        limit.setAuthorId(UUID.randomUUID());
        limit.setDistributed(true);
        limit.setCreationTime(LocalDateTime.now());
        limit.setOwnerId(UUID.randomUUID());
        
        limitService.save(limit);
        
        assertThat(limitRepository.count()).isEqualTo(1);
        
        var actual = limitRepository.findAll().get(0);
        assertThat(actual.getLimitId()).isEqualTo(limit.getLimitId());
        assertThat(actual.getLimitType()).isEqualTo(limit.getLimitType());
        assertThat(actual.getLimitStatus()).isEqualTo(limit.getLimitStatus());
        assertThat(actual.getLimitSharingType()).isEqualTo(limit.getLimitSharingType());
        assertThat(actual.getAuthorId()).isEqualTo(limit.getAuthorId());
        assertThat(actual.isDistributed()).isEqualTo(limit.isDistributed());
        assertThat(actual.getCreationTime()).isEqualTo(limit.getCreationTime());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() {
        var limit = new Limit();
        limit.setLimitId(UUID.randomUUID());
        limit.setLimitType("Limit type");
        limit.setLimitStatus("Limit status");
        limit.setLimitSharingType("Limit sharing type");
        limit.setAuthorId(UUID.randomUUID());
        limit.setDistributed(true);
        limit.setCreationTime(LocalDateTime.now());
        limit.setId(UUID.randomUUID());
    
        limitRepository.save(limit);
        
        var actual = limitService.get(limit.getId());
        
        assertThat(actual).isPresent();
        assertThat(actual.get().getLimitId()).isEqualTo(limit.getLimitId());
        assertThat(actual.get().getLimitType()).isEqualTo(limit.getLimitType());
        assertThat(actual.get().getLimitStatus()).isEqualTo(limit.getLimitStatus());
        assertThat(actual.get().getLimitSharingType()).isEqualTo(limit.getLimitSharingType());
        assertThat(actual.get().getAuthorId()).isEqualTo(limit.getAuthorId());
        assertThat(actual.get().isDistributed()).isEqualTo(limit.isDistributed());
        assertThat(actual.get().getCreationTime()).isEqualTo(limit.getCreationTime());
    }
    
    @Test
    @DisplayName("Получение. Нет лимита")
    void test_get_noLimit() {
        var actual = limitService.get(UUID.randomUUID());
        
        assertThat(actual).isNotPresent();
    }
    
    @Test
    @DisplayName("Получение с типом транспорта")
    void test_get_transportType() {
        var limit = new Limit();
        limit.setOwnerId(UUID.randomUUID());
        limit.setLimitId(UUID.randomUUID());
        limit.setLimitType("Limit type");
        limit.setLimitStatus("Limit status");
        limit.setLimitSharingType("Limit sharing type");
        limit.setTransportType(TransportTypeEnum.TAXI.getName());
        limit.setAuthorId(UUID.randomUUID());
        limit.setDistributed(true);
        limit.setCreationTime(LocalDateTime.now());
        limit.setYear(LocalDateTime.now(Clock.systemUTC()).getYear());
        limit.setId(UUID.randomUUID());
        limit.setDepartmentId(UUID.randomUUID());
    
        limitRepository.save(limit);
        
        var actual = limitService.get(limit.getDepartmentId(), TransportTypeEnum.TAXI);
        
        assertThat(actual).isPresent();
        assertThat(actual.get().getLimitId()).isEqualTo(limit.getLimitId());
        assertThat(actual.get().getLimitType()).isEqualTo(limit.getLimitType());
        assertThat(actual.get().getLimitStatus()).isEqualTo(limit.getLimitStatus());
        assertThat(actual.get().getLimitSharingType()).isEqualTo(limit.getLimitSharingType());
        assertThat(actual.get().getAuthorId()).isEqualTo(limit.getAuthorId());
        assertThat(actual.get().isDistributed()).isEqualTo(limit.isDistributed());
        assertThat(actual.get().getCreationTime()).isEqualTo(limit.getCreationTime());
    }
    
    @Test
    @DisplayName("Получение с типом транспорта. Другой тип")
    void test_get_transportType_otherType() {
        var limit = new Limit();
        limit.setOwnerId(UUID.randomUUID());
        limit.setLimitId(UUID.randomUUID());
        limit.setLimitType("Limit type");
        limit.setLimitStatus("Limit status");
        limit.setLimitSharingType("Limit sharing type");
        limit.setTransportType(TransportTypeEnum.TAXI.getName());
        limit.setAuthorId(UUID.randomUUID());
        limit.setDistributed(true);
        limit.setCreationTime(LocalDateTime.now());
        limit.setId(UUID.randomUUID());
    
        limitRepository.save(limit);
        
        var actual = limitService.get(limit.getId(), TransportTypeEnum.PERSONAL);
        
        assertThat(actual).isNotPresent();
    }
    
    @Test
    @DisplayName("Получение с типом транспорта. Нет лимита")
    void test_get_transportType_noLimit() {
        var actual = limitService.get(UUID.randomUUID(), TransportTypeEnum.PERSONAL);
        
        assertThat(actual).isNotPresent();
    }
    
}