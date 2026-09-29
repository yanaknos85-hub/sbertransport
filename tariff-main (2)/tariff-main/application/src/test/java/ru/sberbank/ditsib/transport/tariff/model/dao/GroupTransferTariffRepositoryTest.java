package ru.sberbank.ditsib.transport.tariff.model.dao;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.tariff.database.dao.GroupTransferTariffRepository;
import ru.sberbank.ditsib.transport.tariff.util.ConvertUtils;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка репозитория группового трансфера тарифов")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles({"test", "kafka"})
class GroupTransferTariffRepositoryTest extends KafkaTest {
    
    @Autowired
    private GroupTransferTariffRepository repository;
    
    
    @Test
    @DisplayName("Проверка нативных запросов")
    void test() {
        repository.findConflictTariff(LocalDate.now(), LocalDate.now(), UUID.randomUUID(), UUID.randomUUID(),
                                      ConvertUtils.setToStringArray(Set.of(UUID.randomUUID(), UUID.randomUUID())),
                                      ConvertUtils.setToStringArray(Set.of(UUID.randomUUID(), UUID.randomUUID())));
        repository.findAllTariff(LocalDate.now(), UUID.randomUUID(), ConvertUtils.setToStringArray(Set.of(UUID.randomUUID(),
                                                                                                          UUID.randomUUID())));
        repository.findAllTariff(LocalDate.now(), UUID.randomUUID(), ConvertUtils.setToStringArray(Set.of(UUID.randomUUID(),
                                                                                                          UUID.randomUUID())),
                                 ConvertUtils.setToStringArray(Set.of(UUID.randomUUID(),
                                                                      UUID.randomUUID())));
    }
    
    
}
