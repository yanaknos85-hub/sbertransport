package ru.sber.transport.contractor.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.IntegrationType;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.internal.InternalContractorRequestDto;
import ru.sber.transport.contractor.dto.internal.LinkRequestDTO;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNull;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@DisplayName("Проверка фабрики сервисов интеграции")
class ContractorMapperTest {

    private Contractor sourceContract;

    private final ContractorMapper contractorMapper = new ContractorMapperImpl(new EmailIntegrationParamsMapperImpl(),
            new BooleanMapperImpl(), new JsonIntegrationParamsMapperImpl());

    @BeforeEach
    void setUp() {
        this.sourceContract = Contractor.builder()
                .id(UUID.randomUUID())
                .name("Sberbank Transport Ltd.")
                .msrn("1234567890123") // OGRN
                .tin("1234567890") // INN
                .contactPersonFirstName("Иван")
                .contactPersonLastName("Иванов")
                .contactPersonPatronymic("Иванович")
                .contactPersonPhone("+79991234567")
                .contactPersonEmail("ivanov@sber.ru")
                .integrationType(IntegrationType.JSON_API_1_0)
                .build();
    }

    @DisplayName("InternalContractorRequestDto")
    @Test
    void testMapWithFullData() {
        final String password = "supersecret";

        sourceContract.setServiceType(ServiceType.INTERNAL_AUTO_PARK);
        InternalContractorRequestDto dtoResult = contractorMapper.map(sourceContract, password, null);

        Assertions.assertEquals(sourceContract.getName(), dtoResult.name());
        Assertions.assertEquals(sourceContract.getTin(), dtoResult.tin());
        Assertions.assertEquals(sourceContract.getMsrn(), dtoResult.msrn());

        InternalContractorRequestDto.NewDispatcherDto dispatcher = dtoResult.mainDispatcher();
        Assertions.assertEquals(sourceContract.getContactPersonFirstName(), dispatcher.firstName());
        Assertions.assertEquals(sourceContract.getContactPersonLastName(), dispatcher.lastName());
        Assertions.assertEquals(sourceContract.getContactPersonPatronymic(), dispatcher.patronymic());
        Assertions.assertEquals(sourceContract.getContactPersonPhone(), dispatcher.phone());
        Assertions.assertEquals(sourceContract.getContactPersonEmail(), dispatcher.email());

        // Проверка интегрированного типа
        Assertions.assertEquals(sourceContract.getIntegrationType().name(), dtoResult.integrationType());

        // Проверка пароля
        Assertions.assertEquals(password, dtoResult.technicalAccountPassword());

        // Проверка флага внутреннего автопарка
        Assertions.assertTrue(dtoResult.isInternal());
    }

    @DisplayName("InternalContractorRequestDto, без пароля")
    @Test
    void testMapWithoutPassword() {
        // выполнение маппинга без пароля
        InternalContractorRequestDto dtoResult = contractorMapper.map(sourceContract, null, null);

        // проверка отсутствия пароля
        assertNull(dtoResult.technicalAccountPassword());
    }

    @Test
    @DisplayName("LinkRequestDTO с полным набором данных")
    void testMapToLinkWithFullData() {
        final String password = "supersecret";
        // Вызываем проверяемый метод
        LinkRequestDTO linkRequestDTO = contractorMapper.mapToLink(sourceContract, password);

        // ПРОВЕРКА РЕЗУЛЬТАТА
        Assertions.assertEquals(sourceContract.getMsrn(), linkRequestDTO.msrn());
        Assertions.assertEquals(sourceContract.getTin(), linkRequestDTO.tin());
        Assertions.assertEquals(sourceContract.getContactPersonEmail(), linkRequestDTO.contactPersonEmail());
        Assertions.assertEquals(sourceContract.getJsonIntegrationParams().getLogin(), linkRequestDTO.login());
        Assertions.assertEquals(password, linkRequestDTO.password());
    }

    @Test
    @DisplayName("ContractorMessage с полным набором данных")
    void testMapToContractorMessageWithFullData() {
        var contractor = Instancio.create(Contractor.class);

        var message = contractorMapper.toMessage(contractor);

        assertThat(message).isNotNull();
        assertThat(message.contractorName()).isEqualTo(contractor.getIntegrationParams().getContractorName());
        assertThat(message.contractorRusName()).isEqualTo(contractor.getIntegrationParams().getContractorRusName());
        assertThat(message.integrationEmail()).isEqualTo(contractor.getIntegrationParams().getEmail());
        assertThat(message.url()).isEqualTo(contractor.getJsonIntegrationParams().getUrl());
        assertThat(message.login()).isEqualTo(contractor.getJsonIntegrationParams().getLogin());
        assertThat(message.password()).isEqualTo(contractor.getJsonIntegrationParams().getPassword());
        assertThat(message.deleted()).isEqualTo(!contractor.isActive());
        assertThat(message.serviceType()).isEqualTo(contractor.getServiceType().name());
        assertThat(message.contractorType()).isEqualTo(contractor.getContractorType().name());
    }

}
