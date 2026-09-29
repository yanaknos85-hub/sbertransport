package ru.sber.transport.contractor.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.service.IntegrationService;
import ru.sber.transport.contractor.service.impl.integration.AutoserviceInternalIntegrationServiceImpl;
import ru.sber.transport.contractor.service.impl.integration.DefaultIntegrationServiceImpl;
import ru.sber.transport.contractor.service.impl.integration.DispatcherInternalIntegrationServiceImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@DisplayName("Проверка фабрики сервисов интеграции")
@ActiveProfiles("test")
class IntegrationServiceFactoryImplTest {

    @Autowired
    private IntegrationServiceFactoryImpl factory;

    @Test
    @DisplayName("Получение сервиса")
    void testGetServiceReturnsDefaultWhenNoMatchingServiceFound() {
        IntegrationService result = factory.getService(ContractorType.AUTOSERVICE_INTERNAL);
        assertThat(result).isInstanceOf(AutoserviceInternalIntegrationServiceImpl.class);
        result = factory.getService(ContractorType.DISPATCHER_INTERNAL);
        assertThat(result).isInstanceOf(DispatcherInternalIntegrationServiceImpl.class);
        result = factory.getService(ContractorType.API);
        assertThat(result).isInstanceOf(DefaultIntegrationServiceImpl.class);
    }

}