package ru.sber.transport.contractor.service.impl.integration;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.IntegrationType;
import ru.sber.transport.contractor.dto.internal.InternalContractorRequestDto;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@DisplayName("Проверка сервисов интеграции по автосервису")
class AutoserviceExternalIntegrationServiceImplTest {

    @Mock
    private InternalClient internalClient;

    @Mock
    private IntegrationConfig integrationConfig;

    @Mock
    private ContractorMapper contractorMapper;

    @InjectMocks
    private AutoserviceExternalIntegrationServiceImpl service;

    private InternalContractorRequestDto dto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dto = new InternalContractorRequestDto("",
                "",
                "",
                null,
                "",
                "",
                IntegrationType.JSON_API_1_0.name(),
                "",
                "",
                1,
                false);
    }

    @Test
    @DisplayName("Проверка успешного добавления подрядчика")
    void testAddContractor() {
        Contractor contractor = new Contractor();
        String password = "testPassword";
        String authToken = "authToken";

        when(integrationConfig.getClientUrl(any())).thenReturn("http://example.com");

        service.add(contractor, password, authToken, null);

        verify(internalClient).linkContractor(eq(URI.create("http://example.com/autoservice")), any());
    }

    @Test
    @DisplayName("Проверка возврата правильного типа подрядчика")
    void testGetType() {
        ContractorType type = service.getType();
        assertEquals(ContractorType.AUTOSERVICE_EXTERNAL, type);
    }
}