package ru.sber.transport.contractor.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.qameta.allure.Feature;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.sber.ditsib.encription.PasswordEncryption;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.config.IntegrationConfig;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.dto.AuthResponseDTO;
import ru.sber.transport.contractor.dto.EmailIntegrationParamsDto;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.dto.search.TransportSearchDTO;
import ru.sber.transport.contractor.exceptions.ClientFeignException;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.mappers.JsonIntegrationParamsMapper;
import ru.sber.transport.contractor.messaging.senders.ContractorSender;
import ru.sber.transport.contractor.messaging.senders.EmployeeRoleSender;
import ru.sber.transport.contractor.service.ContractorService;
import ru.sber.transport.contractor.service.IntegrationService;
import ru.sber.transport.contractor.service.IntegrationServiceFactory;
import ru.sber.transport.contractor.service.grpc.ContractGrpcService;
import ru.sber.transport.contractor.service.impl.integration.DefaultIntegrationServiceImpl;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static ru.sber.transport.contractor.database.model.ContractorType.AUTOSERVICE_INTERNAL;
import static ru.sber.transport.contractor.database.model.ContractorType.OFFLINE;
import static ru.sber.transport.contractor.database.model.ServiceType.AUTOSERVICE;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@AutoConfigureMockMvc
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@SpringBootTest
@DisplayName("Проверка контроллер-сервиса контрагентов")
@ActiveProfiles("test")
@Transactional
class ContractorControllerServiceImplTest {

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private ContractorMapper mapper;

    @Test
    @DisplayName("Проверка работы прокси по поиску свободных автомобилей")
    void getAllFreeTransportTest() {
        var contractorService = mock(ContractorService.class);
        var passwordEncryption = mock(PasswordEncryption.class);
        var factory = mock(IntegrationServiceFactory.class);
        var config = new IntegrationConfig();
        var restTemplate = mock(RestTemplate.class);
        var internalClient = mock(InternalClient.class);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                null, null, null, null, null, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var searchDto = Instancio.create(TransportSearchDTO.class);
        var contractor = Instancio.create(Contractor.class);
        var auth = new AuthResponseDTO("token");
        var password = "password";
        var success = "success";

        Mockito.when(contractorService.get(Mockito.any())).thenReturn(Optional.of(contractor));
        Mockito.when(passwordEncryption.decode(Mockito.any())).thenReturn(password);
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), eq(AuthResponseDTO.class))).thenReturn(new ResponseEntity<>(auth, HttpStatus.OK));
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), eq(new ParameterizedTypeReference<>() {
        }))).thenReturn(new ResponseEntity<>(success, HttpStatus.OK));

        var obj = contractorControllerService.getAllFreeTransport(UUID.randomUUID(), searchDto);

        Assertions.assertNotNull(obj);
        Assertions.assertEquals(success, obj);
    }

    @Test
    @DisplayName("Проверка работы прокси по поиску конкретного автомобиля")
    void getTransportTest() {
        var contractorService = mock(ContractorService.class);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var config = new IntegrationConfig();
        var internalClient = mock(InternalClient.class);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                null, null, null, null, null, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var searchDto = Instancio.create(TransportSearchDTO.class);
        var contractor = Instancio.create(Contractor.class);
        var auth = new AuthResponseDTO("token");
        var password = "password";
        var success = "success";

        Mockito.when(contractorService.get(Mockito.any())).thenReturn(Optional.of(contractor));
        Mockito.when(passwordEncryption.decode(Mockito.any())).thenReturn(password);
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), eq(AuthResponseDTO.class))).thenReturn(new ResponseEntity<>(auth, HttpStatus.OK));
        Mockito.when(restTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(HttpEntity.class), eq(new ParameterizedTypeReference<>() {
        }))).thenReturn(new ResponseEntity<>(success, HttpStatus.OK));

        var obj = contractorControllerService.getTransport(UUID.randomUUID(), UUID.randomUUID(), searchDto);

        Assertions.assertNotNull(obj);
        Assertions.assertEquals(success, obj);
    }

    @DisplayName("Проверка запрета на сохранение некорректного email ")
    @Test
    void checkEmail_wrong() {
        try (var factory = Validation.byDefaultProvider().configure().buildValidatorFactory()) {
            var validator = factory.getValidator();
            Set<ConstraintViolation<EmailIntegrationParamsDto>> violations;

            var email = new EmailIntegrationParamsDto("name", "name", "sbexchange kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", ".sbexchange_kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sb..exchange_kirov@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov.@taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@.taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru.");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@-taxi700700.ru");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());

            email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru-");
            violations = validator.validate(email);
            assertFalse(violations.isEmpty());
        }
    }

    @DisplayName("Проверка отсутствия запрета на сохранение корректного email ")
    @Test
    void checkEmail_correct() {

        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<EmailIntegrationParamsDto>> violations;

        var email = new EmailIntegrationParamsDto("name", "name", "sbexchange_kirov@taxi700700.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());

        email = new EmailIntegrationParamsDto("name", "name", "robot-sber-test@yandex-team.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());

        email = new EmailIntegrationParamsDto("name", "name", "dmitry.ivanov@yandex.team.ru");
        violations = validator.validate(email);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Проверка существования контрагента с аналогичными телефоном")
    void checkContractorExistsWithPhoneTest() {
        var contractorService = new ContractorServiceImpl(contractorRepository);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var config = new IntegrationConfig();
        var internalClient = mock(InternalClient.class);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                null, null, null, null, null, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var newContractor = Instancio.create(NewContractorDTO.class);
        var contractor = new Contractor();
        mapper.update(contractor, newContractor);
        contractor.setName("nameCotractor");
        contractorService.save(contractor);

        String regex = String.format(
                "Conflict data on entity Contractor\\. Conflicted: \\{(phone=%s, id=%s|id=%s, phone=%s)\\}",
                newContractor.contactPersonPhone(), contractor.getId(), contractor.getId(), newContractor.contactPersonPhone()
        );

        Exception exception = assertThrows(DuplicateDataException.class, () -> {
            contractorControllerService.add(newContractor, UUID.randomUUID(), null);
        });
        assertTrue(exception.getMessage().matches(regex));
    }

    @Test
    @DisplayName("Проверка существования контрагента с аналогичными email")
    void checkContractorExistsWithEmailTest() {
        var contractorService = new ContractorServiceImpl(contractorRepository);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var config = new IntegrationConfig();
        var internalClient = mock(InternalClient.class);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                null, null, null, null, null, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var newContractor = Instancio.create(NewContractorDTO.class);
        var contractor = new Contractor();
        mapper.update(contractor, newContractor);
        contractor.setName("nameCotractor");
        contractor.setContactPersonPhone("+7111111111");
        contractorService.save(contractor);

        String regex = String.format(
                "Conflict data on entity Contractor\\. Conflicted: \\{(email=%s, id=%s|id=%s, email=%s)\\}",
                newContractor.contactPersonEmail(), contractor.getId(), contractor.getId(), newContractor.contactPersonEmail()
        );

        Exception exception = assertThrows(DuplicateDataException.class, () -> {
            contractorControllerService.add(newContractor, UUID.randomUUID(), null);
        });
        assertTrue(exception.getMessage().matches(regex));
    }

    @Test
    @DisplayName("Проверка добавления контрагента без интеграции")
    void addOfflineIntegrationContractorTest() {
        var contractorService = new ContractorServiceImpl(contractorRepository);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var paramsMapper = mock(JsonIntegrationParamsMapper.class);
        var contractorSender = mock(ContractorSender.class);
        var employeeRoleSender = mock(EmployeeRoleSender.class);
        var internalClient = mock(InternalClient.class);

        var config = new IntegrationConfig();
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                contractorSender, employeeRoleSender, mapper, null, paramsMapper, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var newContractor = Instancio.of(NewContractorDTO.class)
                .set(field(NewContractorDTO::contractorType), OFFLINE)
                .create();
        doReturn(new DefaultIntegrationServiceImpl()).when(factory).getService(newContractor.contractorType());
        var actualAdded = contractorControllerService.add(newContractor, UUID.randomUUID(), null);
        assertNotNull(actualAdded);
    }

    @Test
    @DisplayName("Проверка добавления контрагента, внешний сервис вернул 4xx")
    void addClientFeignException() {
        var contractorService = new ContractorServiceImpl(contractorRepository);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var paramsMapper = mock(JsonIntegrationParamsMapper.class);
        var contractorSender = mock(ContractorSender.class);
        var employeeRoleSender = mock(EmployeeRoleSender.class);
        var integrationService = mock(IntegrationService.class);
        var ex = mock(FeignException.FeignClientException.class);
        var internalClient = mock(InternalClient.class);

        var config = new IntegrationConfig();
        config.setIsInternal(true);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                contractorSender, employeeRoleSender, mapper, null, paramsMapper, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var newContractor = Instancio.of(NewContractorDTO.class)
                .set(field(NewContractorDTO::contractorType), AUTOSERVICE_INTERNAL)
                .create();
        doReturn(integrationService).when(factory).getService(newContractor.contractorType());
        when(integrationService.add(any(), any(), any(), any())).thenThrow(ex);
        assertThrows(ClientFeignException.class, () -> contractorControllerService.add(newContractor, UUID.randomUUID(), null));
    }

    @Test
    @DisplayName("Проверка изменения контрагента, внешний сервис вернул 4xx")
    void editClientFeignException() {
        var contractorService = mock(ContractorService.class);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var paramsMapper = mock(JsonIntegrationParamsMapper.class);
        var contractorSender = mock(ContractorSender.class);
        var employeeRoleSender = mock(EmployeeRoleSender.class);
        var integrationService = mock(IntegrationService.class);
        var ex = mock(FeignException.FeignClientException.class);
        var internalClient = mock(InternalClient.class);

        var config = new IntegrationConfig();
        config.setIsInternal(true);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                contractorSender, employeeRoleSender, mapper, null, paramsMapper, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);

        var newContractor = Instancio.of(NewContractorDTO.class)
                .set(field(NewContractorDTO::contractorType), AUTOSERVICE_INTERNAL)
                .create();
        when(contractorService.get(any())).thenReturn(Optional.of(Contractor.builder().contractorType(newContractor.contractorType()).build()));
        when(contractorService.save(any())).thenReturn(Contractor.builder().contractorType(newContractor.contractorType()).build());
        doReturn(integrationService).when(factory).getService(newContractor.contractorType());
        doThrow(ex).when(integrationService).edit(any(), any(), any());
        assertThrows(ClientFeignException.class, () -> contractorControllerService.edit(UUID.randomUUID(), newContractor, UUID.randomUUID(), null));
    }

    @Test
    @DisplayName("Проверка удаления контрагента, внешний сервис вернул 4xx")
    void deleteClientFeignException() {
        var contractorService = mock(ContractorService.class);
        var passwordEncryption = mock(PasswordEncryption.class);
        var restTemplate = mock(RestTemplate.class);
        var factory = mock(IntegrationServiceFactory.class);
        var paramsMapper = mock(JsonIntegrationParamsMapper.class);
        var contractorSender = mock(ContractorSender.class);
        var employeeRoleSender = mock(EmployeeRoleSender.class);
        var integrationService = mock(IntegrationService.class);
        var ex = mock(FeignException.FeignClientException.class);
        var contractGrpcService = mock(ContractGrpcService.class);
        var internalClient = mock(InternalClient.class);

        var config = new IntegrationConfig();
        config.setIsInternal(true);
        var contractorControllerService = new ContractorControllerServiceImpl(contractorService,
                contractorSender, employeeRoleSender, mapper, contractGrpcService, paramsMapper, restTemplate, passwordEncryption, factory, config, new ObjectMapper(), internalClient);
        var contractorId = UUID.randomUUID();
        var externalId = UUID.randomUUID();
        when(contractorService.get(contractorId)).thenReturn(Optional.of(Contractor.builder().id(contractorId).build()));
        var newContractor = Instancio.of(NewContractorDTO.class)
                .set(field(NewContractorDTO::contractorType), AUTOSERVICE_INTERNAL)
                .set(field(NewContractorDTO::serviceType), AUTOSERVICE)
                .create();
        when(contractorService.get(contractorId)).thenReturn(Optional.of(Contractor.builder()
                .id(contractorId)
                .contractorType(newContractor.contractorType())
                .serviceType(newContractor.serviceType())
                .externalId(externalId)
                .build()));

        when(contractorService.delete(any())).thenReturn(Contractor.builder()
                .id(contractorId)
                .contractorType(newContractor.contractorType())
                .externalId(externalId)
                .build());
        doReturn(integrationService).when(factory).getService(newContractor.contractorType());
        doThrow(ex).when(integrationService).delete(eq(externalId), any());

        assertThrows(ClientFeignException.class, () -> contractorControllerService.delete(contractorId, null));
        verify(contractGrpcService).haveActiveContractsByContractorId(contractorId);
    }
}