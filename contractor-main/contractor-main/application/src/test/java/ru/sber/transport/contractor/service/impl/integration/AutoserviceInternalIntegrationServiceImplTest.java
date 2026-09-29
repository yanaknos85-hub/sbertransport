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
import ru.sber.transport.contractor.dto.internal.DispatcherResponseDTO;
import ru.sber.transport.contractor.dto.internal.InternalContractorRequestDto;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@DisplayName("Проверка сервисов интеграции по автосервису")
class AutoserviceInternalIntegrationServiceImplTest {

    @Mock
    private InternalClient internalClient;

    @Mock
    private IntegrationConfig integrationConfig;

    @Mock
    private ContractorMapper contractorMapper;

    @InjectMocks
    private AutoserviceInternalIntegrationServiceImpl service;

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
        UUID expectedId = UUID.randomUUID();

        when(integrationConfig.getClientUrl(any())).thenReturn("http://example.com");
        when(contractorMapper.map(contractor, password, null)).thenReturn(dto);
        when(internalClient.add(any(), any(), anyString())).thenReturn(new DispatcherResponseDTO(expectedId));

        UUID result = service.add(contractor, password, authToken, null);

        assertEquals(expectedId, result);
        verify(internalClient).add(eq(URI.create("http://example.com/autoservice")), any(), eq(authToken));
    }

    @Test
    @DisplayName("Проверка успешного обновления подрядчика")
    void testEditContractor() {
        UUID id = UUID.randomUUID();
        Contractor contractor = new Contractor();
        String authToken = "authToken";

        when(integrationConfig.getClientUrl(any())).thenReturn("http://example.com");
        when(contractorMapper.map(contractor, null, null)).thenReturn(dto);

        service.edit(id, contractor, authToken);

    }

    @Test
    @DisplayName("Проверка успешного удаления подрядчика")
    void testDeleteContractor() {
        UUID id = UUID.randomUUID();
        String authToken = "authToken";

        when(integrationConfig.getClientUrl(any())).thenReturn("http://example.com");

        service.delete(id, authToken);

    }

    @Test
    @DisplayName("Проверка возврата правильного типа подрядчика")
    void testGetType() {
        ContractorType type = service.getType();
        assertEquals(ContractorType.AUTOSERVICE_INTERNAL, type);
    }
}