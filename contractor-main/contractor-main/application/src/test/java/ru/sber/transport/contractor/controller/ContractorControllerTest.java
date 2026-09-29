package ru.sber.transport.contractor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import feign.FeignException;
import feign.Request;
import io.qameta.allure.Feature;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.ditsib.encription.PasswordEncryption;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.dao.EmployeeRepository;
import ru.sber.transport.contractor.database.model.*;
import ru.sber.transport.contractor.dto.*;
import ru.sber.transport.contractor.dto.internal.DispatcherResponseDTO;
import ru.sber.transport.contractor.feign.InternalClient;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.contractor.testutils.TestContractors;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.contractor.database.model.ContractorType.*;
import static ru.sber.transport.contractor.database.model.ServiceType.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@SpringBootTest
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера контрагентов")
@MockBean(Key.class)
@TestPropertySource(properties = "spring.jpa.show-sql=true")
@ActiveProfiles("test")
class ContractorControllerTest {

    private final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";
    private final UUID USER_ID_UUID = UUID.fromString(USER_ID);
    private final String ORGANIZATION_ID = "f10b775b-51db-4e1c-a747-222296041234";
    private final UUID organizationId = UUID.fromString(ORGANIZATION_ID);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @MockBean(name = "usersOutput")
    private OutputBridge usersOutput;

    @MockBean(name = "contractorOutput")
    private OutputBridge contractorOutput;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncryption passwordEncryption;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private InternalClient internalClient;


    @Autowired
    private ObjectMapper mapper;


    @BeforeEach
    void setUp() {
        when(internalClient.add(any(), any(), any())).thenReturn(new DispatcherResponseDTO(UUID.randomUUID()));
        doNothing().when(internalClient).edit(any(), any(), any(), any());
        doNothing().when(internalClient).delete(any(), any(), any());
        AuthorizeUtils.authorize(manager);
        contractorRepository.deleteAll();
    }

    protected void createEmployee() {
        var employee = Employee.builder().id(USER_ID_UUID).userId(USER_ID_UUID)
                .organizationId(organizationId).build();
        employeeRepository.save(employee);
    }

    @ParameterizedTest
    @ValueSource(strings = {"000000000000000,000000000000,AUTOSERVICE,AUTOSERVICE_INTERNAL",
            "0000000000000,0000000000,AUTOSERVICE,API",
            "0000000000000,0000000000,EMPLOYEE_TRANSPORTATION,DISPATCHER_INTERNAL",
            "0000000000000,0000000000,EMPLOYEE_TRANSPORTATION,API",
            "0000000000000,0000000000,CARGO_TRANSPORTATION,DISPATCHER_INTERNAL",
            "0000000000000,0000000000,CARGO_TRANSPORTATION,API",
            "0000000000000,0000000000,INTERNAL_AUTO_PARK,DISPATCHER_INTERNAL",})
    @DisplayName("Добавление")
    void add(String str) throws Exception {
        var strs = str.split(",");
        var msrn = strs[0];
        var tin = strs[1];
        var serviceType = ServiceType.valueOf(strs[2]);
        var contractorType = ContractorType.valueOf(strs[3]);
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var email = "email@email.ru";
        var rating = 0;
        var vehicleCountNorm = 300;

        JsonIntegrationParamsDto jsonParam = null;
        if (contractorType.equals(API)) {
            jsonParam = new JsonIntegrationParamsDto("https://test.ru", "login", "password");
        }
        var newContractor = new NewContractorDTO(contractorName, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, email, UUID.randomUUID(),
                rating, null, jsonParam, null, serviceType, contractorType, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        var result = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newContractor)))
                .andExpect(status().isOk()).andReturn();
        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), ContractorDTO.class);
        assertContractorDto(newContractor, dto);
        if (API.equals(contractorType)) {
            assertIntegrationParams(jsonParam, dto.jsonIntegrationParams());
        }

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);

        assertThat(actualDb.getEmployeeCount()).isZero();

        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.getId()).isEqualTo(actualDb.getId());
        assertThat(actualMessage.msrn()).isEqualTo(actualDb.getMsrn());
        assertThat(actualMessage.name()).isEqualTo(actualDb.getName());
        assertThat(actualMessage.tin()).isEqualTo(actualDb.getTin());
        assertThat(actualMessage.deleted()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0000000000000000,000000000",
            "0000000000000000,0000000000",
            "000000000000000,0000000000",
            "000000000000000,00000000000",
            "0000000000000,00000000000",
            "0000000000000,000000000000",
            "000000000000,00000000000",
            "000000000000,000000000000",})
    @DisplayName("Добавление")
    void add_wrongTinAndMsrl(String str) throws Exception {
        var name = "Name";
        var strs = str.split(",");
        var msrn = strs[0];
        var tin = strs[1];
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var contractorRusName = "contactorRusName";
        var email = "email@email.ru";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;
        var contactPersonEmail = "test@test.ru";
        var vehicleCountNorm = 300;

        var newContractor = TestContractors.createContractorDto(name, msrn, tin, contactPersonFirstName, contactPersonLastName, contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(newContractor))
                .andExpect(status().isBadRequest());

    }

    @Test
    @DisplayName("Добавление c данными для интеграции через API")
    void add_api_integration() throws Exception {
        var name = "Name";
        var msrn = "0000000000000";
        var tin = "0000000000";
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var email = "email@email.ru";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;

        var url = "Last name";
        var login = "First name";
        var password = "Patronymic";
        var vehicleCountNorm = 300;

        var jsonParam = new JsonIntegrationParamsDto(url, login, password);

        var newContractor = new NewContractorDTO(name, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, email, null,
                rating, null, jsonParam, null, ServiceType.EMPLOYEE_TRANSPORTATION, API, vehicleCountNorm);

        assertThat(contractorRepository.count()).isZero();

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(mapper.writeValueAsString(newContractor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonIntegrationParams.url").value(url))
                .andExpect(jsonPath("$.jsonIntegrationParams.login").value(login))
                .andExpect(jsonPath("$.jsonIntegrationParams.password").value("[protected]"))
        ;

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actual = contractorRepository.findAll().get(0);

        assertThat(passwordEncryption.decode(actual.getJsonIntegrationParams().getPassword())).isEqualTo(password);
    }

    @Test
    @DisplayName("Добавление дубликата")
    void add_duplicate() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());

        var name = contractor.getName();
        var msrn = contractor.getMsrn();
        var tin = contractor.getTin();
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var contractorRusName = "contactorRusName";
        var email = "email@email.ru";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;
        var contactPersonEmail = "test@test.ru";
        var vehicleCountNorm = 300;

        var newContractor = TestContractors.createContractorDto(name, msrn.substring(0, msrn.length() - 1) + 0,
                tin.substring(0, tin.length() - 1) + 0, contactPersonFirstName, contactPersonLastName, contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(contractor.getOrganizations().iterator().next()).build();
        employeeRepository.save(employee);

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Paged", "true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(newContractor))
                .andExpect(status().isConflict());

        newContractor = TestContractors.createContractorDto(name + 1, msrn.substring(0, msrn.length() - 1) + 1, tin,
                contactPersonFirstName + 1, contactPersonLastName + 1, contactPersonPatronymic + 1, contactPersonPhone + 1,
                rating + 1, regionIds, TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail + 1, vehicleCountNorm + 1);


        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .content(newContractor))
                .andExpect(status().isOk());

        newContractor = TestContractors.createContractorDto(name + 2, msrn, tin.substring(0, tin.length() - 1) + 2,
                contactPersonFirstName + 2, contactPersonLastName + 2, contactPersonPatronymic + 2, contactPersonPhone + 2,
                rating + 1, regionIds, TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail + 2, vehicleCountNorm + 2);

        mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .content(newContractor))
                .andExpect(status().isOk());


        newContractor = TestContractors.createContractorDto(name, msrn.substring(0, msrn.length() - 1) + 3, tin,
                contactPersonFirstName + 3, contactPersonLastName + 3, contactPersonPatronymic + 3, contactPersonPhone + 3,
                rating + 3, regionIds, TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail, vehicleCountNorm + 3);

        var result = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .content(newContractor))
                .andExpect(status().isConflict());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Изменение")
    void edit() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var id = contractor.getId();

        var name = "Name";
        var msrn = "0000000000000";
        var tin = "0000000000";
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var contractorRusName = "contactorRusName";
        var email = "new@bbb.ccc";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;
        var contactPersonEmail = "test@test.ru";
        var vehicleCountNorm = 400;

        var newContractor = TestContractors.createContractorDto(name, msrn.substring(0, msrn.length() - 1) + 0,
                tin.substring(0, tin.length() - 1) + 0, contactPersonFirstName, contactPersonLastName, contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                TestContractors.createIntegrationParams(contractorName, contractorRusName, email), IntegrationTypeDto.EMAIL_XML_API, contactPersonEmail, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(contractor.getOrganizations().iterator().next()).build();
        employeeRepository.save(employee);

        mockMvc.perform(put("/" + id + "/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .header("X-Paged", "true")
                .content(newContractor)).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);
        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualDb.getId()).isEqualTo(id);
        assertThat(actualDb.getMsrn()).isEqualTo(msrn);
        assertThat(actualDb.getName()).isEqualTo(name);
        assertThat(actualDb.getTin()).isEqualTo(tin);
        assertThat(actualDb.getContactPersonFirstName()).isEqualTo(contactPersonFirstName);
        assertThat(actualDb.getContactPersonLastName()).isEqualTo(contactPersonLastName);
        assertThat(actualDb.getContactPersonPatronymic()).isEqualTo(contactPersonPatronymic);
        assertThat(actualDb.getContactPersonPhone()).isEqualTo(contactPersonPhone);
        assertThat(actualDb.getVehicleCountNorm()).isEqualTo(vehicleCountNorm);
        assertTrue(StringUtils.isNotBlank(actualDb.getIntegrationParams().getEmail()));

        assertThat(actualMessage.id()).isEqualTo(actualDb.getId());
        assertThat(actualMessage.msrn()).isEqualTo(actualDb.getMsrn());
        assertThat(actualMessage.name()).isEqualTo(actualDb.getName());
        assertThat(actualMessage.tin()).isEqualTo(actualDb.getTin());
        assertThat(actualMessage.contactPersonInfo()).isEqualTo(actualDb.getContactPersonInfo());
        assertThat(actualMessage.contactPersonPhone()).isEqualTo(actualDb.getContactPersonPhone());
        assertThat(actualMessage.deleted()).isFalse();
    }

    @Test
    @DisplayName("Изменение")
    void editInternalAutoPark() throws Exception {
        var msrn = "0000000000000";
        var tin = "0000000000";
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var contractorName = "contactorName";
        var email = "new@bbb.ccc";
        var rating = 0;
        var vehicleCountNorm = 300;

        var newContractor = new NewContractorDTO(contractorName, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, email, UUID.randomUUID(),
                rating, null, new JsonIntegrationParamsDto("https://test.ru", "login", "password"), null, INTERNAL_AUTO_PARK, ContractorType.DISPATCHER_INTERNAL, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        var result = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newContractor)))
                .andExpect(status().isOk()).andReturn();
        var dto = objectMapper.readValue(result.getResponse().getContentAsString(), ContractorDTO.class);


        mockMvc.perform(put("/" + dto.id() + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(objectMapper.writeValueAsString(newContractor)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Изменение интеграции по API")
    void edit_api() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor(0, API, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API));

        var name = "Name";
        var msrn = "0000000000000";
        var tin = "0000000000";
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var url = "contactorName";
        var login = "contactorRusName";
        var password = "new@bbb.ccc";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;
        var contactPersonEmail = "test@test.ru";
        var vehicleCountNorm = 300;

        var newContractor = TestContractors.createContractorJsonDto(name, msrn.substring(0, msrn.length() - 1) + 0,
                tin.substring(0, tin.length() - 1) + 0, contactPersonFirstName, contactPersonLastName, contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                TestContractors.createJsonData(url, login, password), IntegrationTypeDto.JSON_API_1_0, contactPersonEmail, vehicleCountNorm);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(contractor.getOrganizations().iterator().next()).build();
        employeeRepository.save(employee);

        assertThat(contractor.getJsonIntegrationParams().getPassword()).isEqualTo("test");

        mockMvc.perform(put("/" + contractor.getId() + "/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .header("X-Paged", "true")
                .content(newContractor)).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);
        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualDb.getId()).isEqualTo(contractor.getId());
        assertThat(actualDb.getMsrn()).isEqualTo(msrn);
        assertThat(actualDb.getName()).isEqualTo(name);
        assertThat(actualDb.getTin()).isEqualTo(tin);
        assertThat(actualDb.getContactPersonFirstName()).isEqualTo(contactPersonFirstName);
        assertThat(actualDb.getContactPersonLastName()).isEqualTo(contactPersonLastName);
        assertThat(actualDb.getContactPersonPatronymic()).isEqualTo(contactPersonPatronymic);
        assertThat(actualDb.getContactPersonPhone()).isEqualTo(contactPersonPhone);
        assertThat(actualDb.getJsonIntegrationParams().getUrl()).isEqualTo(url);
        assertThat(actualDb.getJsonIntegrationParams().getLogin()).isEqualTo(login);
        assertThat(passwordEncryption.decode(actualDb.getJsonIntegrationParams().getPassword())).isEqualTo(password);

        assertThat(actualMessage.id()).isEqualTo(actualDb.getId());
        assertThat(actualMessage.msrn()).isEqualTo(actualDb.getMsrn());
        assertThat(actualMessage.name()).isEqualTo(actualDb.getName());
        assertThat(actualMessage.tin()).isEqualTo(actualDb.getTin());
        assertThat(actualMessage.contactPersonInfo()).isEqualTo(actualDb.getContactPersonInfo());
        assertThat(actualMessage.contactPersonPhone()).isEqualTo(actualDb.getContactPersonPhone());
        assertThat(actualMessage.deleted()).isFalse();
        assertEquals(password, passwordEncryption.decode(actualMessage.password()));
        assertEquals(login, actualMessage.login());
        assertEquals(url, actualMessage.url());
    }

    @Test
    @DisplayName("Изменение интеграции по API. Пароль не меняется")
    void edit_api_noChangePassword() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor(0, API, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API));

        var name = "Name";
        var msrn = "0000000000000";
        var tin = "0000000000";
        var contactPersonFirstName = "John";
        var contactPersonLastName = "Doe";
        var contactPersonPatronymic = "Smith";
        var contactPersonPhone = "contact person phone";
        var url = "contactorName";
        var login = "contactorRusName";
        var password = "[protected]";
        var regionIds = List.of(UUID.randomUUID());
        var rating = 0;
        var contactPersonEmail = "test@test.ru";
        var vehicleCountNorm = 300;

        var newContractor = TestContractors.createContractorJsonDto(name, msrn.substring(0, msrn.length() - 1) + 0,
                tin.substring(0, tin.length() - 1) + 0, contactPersonFirstName, contactPersonLastName, contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                TestContractors.createJsonData(url, login, password), IntegrationTypeDto.JSON_API_1_0, contactPersonEmail, vehicleCountNorm);

        assertThat(contractor.getJsonIntegrationParams().getPassword()).isEqualTo("test");

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(contractor.getOrganizations().iterator().next()).build();
        employeeRepository.save(employee);

        mockMvc.perform(put("/" + contractor.getId() + "/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .header("X-Paged", "true")
                .content(newContractor)).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);
        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualDb.getId()).isEqualTo(contractor.getId());
        assertThat(actualDb.getMsrn()).isEqualTo(msrn);
        assertThat(actualDb.getName()).isEqualTo(name);
        assertThat(actualDb.getTin()).isEqualTo(tin);
        assertThat(actualDb.getContactPersonFirstName()).isEqualTo(contactPersonFirstName);
        assertThat(actualDb.getContactPersonLastName()).isEqualTo(contactPersonLastName);
        assertThat(actualDb.getContactPersonPatronymic()).isEqualTo(contactPersonPatronymic);
        assertThat(actualDb.getJsonIntegrationParams().getUrl()).isEqualTo(url);
        assertThat(actualDb.getJsonIntegrationParams().getLogin()).isEqualTo(login);
        assertThat(actualDb.getJsonIntegrationParams().getPassword()).isEqualTo("test");

        assertThat(actualMessage.id()).isEqualTo(actualDb.getId());
        assertThat(actualMessage.msrn()).isEqualTo(actualDb.getMsrn());
        assertThat(actualMessage.name()).isEqualTo(actualDb.getName());
        assertThat(actualMessage.tin()).isEqualTo(actualDb.getTin());
        assertThat(actualMessage.contactPersonInfo()).isEqualTo(actualDb.getContactPersonInfo());
        assertThat(actualMessage.contactPersonPhone()).isEqualTo(actualDb.getContactPersonPhone());
        assertThat(actualMessage.deleted()).isFalse();
        assertEquals("test", actualMessage.password());
        assertEquals(login, actualMessage.login());
        assertEquals(url, actualMessage.url());
    }

    @Test
    @DisplayName("Изменение несуществующего")
    void edit_nonExists() throws Exception {
        var newContractor = TestContractors.createTestContractorDto();

        var employee = Employee.builder().id(UUID.fromString(USER_ID))
                .userId(UUID.fromString(USER_ID))
                .organizationId(UUID.randomUUID())
                .consent(true)
                .build();
        employeeRepository.save(employee);

        var result = mockMvc.perform(put("/" + UUID.randomUUID() + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .content(newContractor))
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Изменение на дубликат")
    void edit_toDuplicate() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor(0, API, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API));
        var contractor1 = contractorRepository.save(TestContractors.createTestContractor(1, API, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API));
        var contractor2 = TestContractors.createTestContractor(0, API, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API);
        contractor2.setJsonIntegrationParams(new JsonIntegrationParams("test", "test", "test"));
        var id = contractor1.getId();

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(contractor.getOrganizations().iterator().next()).build();
        employeeRepository.save(employee);

        mockMvc.perform(put("/" + id + "/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .content(objectMapper.writeValueAsString(contractor2)))
                .andExpect(status().isConflict());

        assertThat(contractorRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Удаление")
    void deleteContractor() throws Exception {
        var id =
                contractorRepository
                        .save(TestContractors.createTestContractor())
                        .getId();

        assertThat(contractorRepository.count()).isEqualTo(1);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(delete("/" + id + "/").header("X-Paged", "true")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);
        assertThat(contractorRepository.findAll().get(0).isActive()).isFalse();

        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.id()).isEqualTo(actualMessage.id());
        assertThat(actualMessage.deleted()).isTrue();
    }

    @Test
    @DisplayName("Удаление (Внутренний автопарк)")
    void deleteInternalAutoPark() throws Exception {
        var id =
                contractorRepository
                        .save(TestContractors.createTestInternalAutoPark())
                        .getId();

        assertThat(contractorRepository.count()).isEqualTo(1);

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(organizationId).build();
        employeeRepository.save(employee);

        doNothing().when(internalClient).delete(any(), any(), any());

        mockMvc.perform(delete("/" + id + "/").header("X-Paged", "true")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);
        assertThat(contractorRepository.findAll().get(0).isActive()).isFalse();

        var messageCaptor = ArgumentCaptor.forClass(ContractorMessage.class);
        verify(contractorOutput).send(messageCaptor.capture());
        var actualMessage = messageCaptor.getValue();

        assertThat(actualMessage.id()).isEqualTo(actualMessage.id());
        assertThat(actualMessage.deleted()).isTrue();
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void deleteContractor_nonExists() throws Exception {
        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(delete("/" + UUID.randomUUID() + "/").header("X-Paged", "true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение одного")
    void getOne() throws Exception {
        var id = contractorRepository.save(TestContractors.createTestContractor()).getId();
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        var response = mockMvc.perform(get("/" + id + "/")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), ContractorDTO.class);
        var expected = contractorRepository.findAll().get(0);

        assertThat(actual.id()).isEqualTo(id);
        assertThat(actual.msrn()).isEqualTo(expected.getMsrn());
        assertThat(actual.name()).isEqualTo(expected.getName());
        assertThat(actual.tin()).isEqualTo(expected.getTin());
        assertThat(actual.contactPersonPhone()).isEqualTo(expected.getContactPersonPhone());
        assertThat(actual.contactPersonEmail()).isEqualTo(expected.getContactPersonEmail());
        assertThat(actual.contactPersonFirstName()).isEqualTo(expected.getContactPersonFirstName());
        assertThat(actual.contactPersonLastName()).isEqualTo(expected.getContactPersonLastName());
        assertThat(actual.contactPersonPatronymic()).isEqualTo(expected.getContactPersonPatronymic());
        assertThat(actual.contactPersonInfo()).isEqualTo(expected.getContactPersonInfo());
        assertThat(actual.rating()).isEqualTo(expected.getRating());
    }

    @Test
    @DisplayName("Получение одного несуществующего")
    void getOne_nonExists() throws Exception {
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        mockMvc.perform(get("/" + UUID.randomUUID() + "/")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех")
    void getAll() throws Exception {
        var itemCount = 100;
        var firstContractor = TestContractors.createTestContractor();
        for (var i = 0; i < itemCount; i++) {
            contractorRepository.save(TestContractors.incrementContractor(firstContractor, i));
        }

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true).build();
        employeeRepository.save(employee);

        var response = mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        var expectedList = contractorRepository.findAll(Sort.by(Contractor_.NAME));
        for (var i = 0; i < 20; i++) {
            var expected = expectedList.get(i);
            response
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(expected.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].name".formatted(i)).value(expected.getName()))
                    .andExpect(jsonPath("$.content[%s].tin".formatted(i)).value(expected.getTin()));
        }
    }

    @Test
    @DisplayName("Получение всех по типу услуги")
    void getAllByServiceType() throws Exception {
        var itemCount = 100;
        var firstContractor = TestContractors.createTestContractor();
        for (var i = 0; i < itemCount; i++) {
            contractorRepository.save(TestContractors.incrementContractor(firstContractor, i));
        }

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true).build();
        employeeRepository.save(employee);

        var response = mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")
                        .param("serviceType", "INTERNAL_AUTO_PARK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        response = mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")
                        .param("serviceType", "EMPLOYEE_TRANSPORTATION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        var expectedList = contractorRepository.findAll(Sort.by(Contractor_.NAME));
        for (var i = 0; i < 20; i++) {
            var expected = expectedList.get(i);
            response
                    .andExpect(jsonPath("$.content[%s].msrn".formatted(i)).value(expected.getMsrn()))
                    .andExpect(jsonPath("$.content[%s].name".formatted(i)).value(expected.getName()))
                    .andExpect(jsonPath("$.content[%s].tin".formatted(i)).value(expected.getTin()));
        }
    }

    @Test
    @DisplayName("Получение всех по фио контактного лица")
    void getAllByContactPerson() throws Exception {
        var itemCount = 100;
        var firstContractor = TestContractors.createTestContractor();
        for (var i = 0; i < itemCount; i++) {
            contractorRepository.save(TestContractors.incrementContractor(firstContractor, i));
        }

        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true).build();
        employeeRepository.save(employee);

        mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")
                        .param("personFirstName", "ContactFirstName00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")
                        .param("personLastName", "ContactLastName00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
        mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")
                        .param("personPatronymic", "ContactPatronymic00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Получение всех с ограничением по своей организации")
    void getAllRestricted() throws Exception {
        createEmployee();

        var itemCount = 20;
        var firstContractor = TestContractors.createTestContractor();
        for (var i = 0; i < itemCount; i++) {
            Contractor contractor = TestContractors.incrementContractor(firstContractor, i);
            contractor.getOrganizations().add(i % 2 == 0 ? organizationId : UUID.randomUUID());
            contractorRepository.save(contractor);
        }

        var employee = Employee.builder().id(UUID.fromString(USER_ID))
                .consent(true).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(get("/")
                        .header("X-Paged", "true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));
    }

    @Test
    @DisplayName("Получение всех с ограничением по организации. СМД")
    void getAll_organization_id_smd() throws Exception {
        createEmployee();

        var itemCount = 20;
        var firstContractor = TestContractors.createTestContractor();
        var expectedContractors = new ArrayList<Contractor>();
        for (var i = 0; i < itemCount; i++) {
            Contractor contractor = TestContractors.incrementContractor(firstContractor, i);
            contractor.getOrganizations().add(i % 2 == 0 ? organizationId : UUID.randomUUID());
            expectedContractors.add(contractorRepository.save(contractor));
        }
        expectedContractors.sort(Comparator.comparing(Contractor::getName));

        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(get("/?organizationId=" + organizationId)
                        .header("X-Paged", "true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.content.[0].id").value(expectedContractors.get(0).getId().toString()))
                .andExpect(jsonPath("$.content.[1].id").value(expectedContractors.get(2).getId().toString()))
                .andExpect(jsonPath("$.content.[2].id").value(expectedContractors.get(4).getId().toString()))
                .andExpect(jsonPath("$.content.[3].id").value(expectedContractors.get(6).getId().toString()))
                .andExpect(jsonPath("$.content.[4].id").value(expectedContractors.get(8).getId().toString()))
                .andExpect(jsonPath("$.content.[5].id").value(expectedContractors.get(10).getId().toString()))
                .andExpect(jsonPath("$.content.[6].id").value(expectedContractors.get(12).getId().toString()))
                .andExpect(jsonPath("$.content.[7].id").value(expectedContractors.get(14).getId().toString()))
                .andExpect(jsonPath("$.content.[8].id").value(expectedContractors.get(16).getId().toString()))
                .andExpect(jsonPath("$.content.[9].id").value(expectedContractors.get(18).getId().toString()))
        ;
    }

    @Test
    @DisplayName("Получение всех с ограничением по организации. СМД. Нет пагинации")
    void getAll_organization_id_smd_noPaged() throws Exception {
        createEmployee();

        var itemCount = 20;
        var firstContractor = TestContractors.createTestContractor();
        var expectedContractors = new ArrayList<Contractor>();
        for (var i = 0; i < itemCount; i++) {
            Contractor contractor = TestContractors.incrementContractor(firstContractor, i);
            contractor.getOrganizations().add(i % 2 == 0 ? organizationId : UUID.randomUUID());
            expectedContractors.add(contractorRepository.save(contractor));
        }
        expectedContractors.sort(Comparator.comparing(Contractor::getName));

        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(get("/?organizationId=" + organizationId)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$.[0].id").value(expectedContractors.get(0).getId().toString()))
                .andExpect(jsonPath("$.[1].id").value(expectedContractors.get(2).getId().toString()))
                .andExpect(jsonPath("$.[2].id").value(expectedContractors.get(4).getId().toString()))
                .andExpect(jsonPath("$.[3].id").value(expectedContractors.get(6).getId().toString()))
                .andExpect(jsonPath("$.[4].id").value(expectedContractors.get(8).getId().toString()))
                .andExpect(jsonPath("$.[5].id").value(expectedContractors.get(10).getId().toString()))
                .andExpect(jsonPath("$.[6].id").value(expectedContractors.get(12).getId().toString()))
                .andExpect(jsonPath("$.[7].id").value(expectedContractors.get(14).getId().toString()))
                .andExpect(jsonPath("$.[8].id").value(expectedContractors.get(16).getId().toString()))
                .andExpect(jsonPath("$.[9].id").value(expectedContractors.get(18).getId().toString()))
        ;
    }

    @Test
    @DisplayName("Получение всех с ограничением по организации. СМД. SELECT")
    void getAll_organization_id_smd_noPaged_select() throws Exception {
        createEmployee();

        var itemCount = 20;
        var firstContractor = TestContractors.createTestContractor();
        var expectedContractors = new ArrayList<Contractor>();
        for (var i = 0; i < itemCount; i++) {
            Contractor contractor = TestContractors.incrementContractor(firstContractor, i);
            contractor.getOrganizations().add(i % 2 == 0 ? organizationId : UUID.randomUUID());
            expectedContractors.add(contractorRepository.save(contractor));
        }
        expectedContractors.sort(Comparator.comparing(Contractor::getName));

        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);

        mockMvc.perform(get("/?organizationId=" + organizationId + "&projection=SELECT")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("data_master", true)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$.[0].id").value(expectedContractors.get(0).getId().toString()))
                .andExpect(jsonPath("$.[1].id").value(expectedContractors.get(2).getId().toString()))
                .andExpect(jsonPath("$.[2].id").value(expectedContractors.get(4).getId().toString()))
                .andExpect(jsonPath("$.[3].id").value(expectedContractors.get(6).getId().toString()))
                .andExpect(jsonPath("$.[4].id").value(expectedContractors.get(8).getId().toString()))
                .andExpect(jsonPath("$.[5].id").value(expectedContractors.get(10).getId().toString()))
                .andExpect(jsonPath("$.[6].id").value(expectedContractors.get(12).getId().toString()))
                .andExpect(jsonPath("$.[7].id").value(expectedContractors.get(14).getId().toString()))
                .andExpect(jsonPath("$.[8].id").value(expectedContractors.get(16).getId().toString()))
                .andExpect(jsonPath("$.[9].id").value(expectedContractors.get(18).getId().toString()))
                .andExpect(jsonPath("$.[0].name").value(expectedContractors.get(0).getName()))
                .andExpect(jsonPath("$.[1].name").value(expectedContractors.get(2).getName()))
                .andExpect(jsonPath("$.[2].name").value(expectedContractors.get(4).getName()))
                .andExpect(jsonPath("$.[3].name").value(expectedContractors.get(6).getName()))
                .andExpect(jsonPath("$.[4].name").value(expectedContractors.get(8).getName()))
                .andExpect(jsonPath("$.[5].name").value(expectedContractors.get(10).getName()))
                .andExpect(jsonPath("$.[6].name").value(expectedContractors.get(12).getName()))
                .andExpect(jsonPath("$.[7].name").value(expectedContractors.get(14).getName()))
                .andExpect(jsonPath("$.[8].name").value(expectedContractors.get(16).getName()))
                .andExpect(jsonPath("$.[9].name").value(expectedContractors.get(18).getName()))
                .andExpect(jsonPath("$.[0].tin").doesNotExist())
                .andExpect(jsonPath("$.[1].tin").doesNotExist())
                .andExpect(jsonPath("$.[2].tin").doesNotExist())
                .andExpect(jsonPath("$.[3].tin").doesNotExist())
                .andExpect(jsonPath("$.[4].tin").doesNotExist())
                .andExpect(jsonPath("$.[5].tin").doesNotExist())
                .andExpect(jsonPath("$.[6].tin").doesNotExist())
                .andExpect(jsonPath("$.[7].tin").doesNotExist())
                .andExpect(jsonPath("$.[8].tin").doesNotExist())
                .andExpect(jsonPath("$.[9].tin").doesNotExist())
        ;
    }

    @Test
    @DisplayName("Получение списка услуг")
    void getServices() throws Exception {
        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);
        mockMvc.perform(get("/services")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].name").value(Matchers.containsInAnyOrder(
                        AUTOSERVICE.name(),
                        EMPLOYEE_TRANSPORTATION.name(),
                        CARGO_TRANSPORTATION.name())))
                .andExpect(jsonPath("$.[*].rusName").value(Matchers.containsInAnyOrder(
                        AUTOSERVICE.getRusName(),
                        EMPLOYEE_TRANSPORTATION.getRusName(),
                        CARGO_TRANSPORTATION.getRusName())));
    }

    @Test
    @DisplayName("Получение списка услуг (спецуслуги)")
    void getServicesSpecial() throws Exception {
        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);

        var result = mockMvc.perform(get("/services?specialService=true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();
        var services = result.getResponse().getContentAsString(Charsets.UTF_8);
        assertThat(services).isEqualTo("""
                [{"name":"INTERNAL_AUTO_PARK","rusName":"Внутренний автопарк"}]""");
    }

    @ParameterizedTest
    @EnumSource(ServiceType.class)
    @DisplayName("Получение методов интеграции")
    void getServices(ServiceType serviceType) throws Exception {
        var employee = Employee.builder().id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID))
                .consent(true)
                .organizationId(UUID.randomUUID()).build();
        employeeRepository.save(employee);
        var apiDto = new EnumRusNameDTO(API.name(), API.getInternalRusName());
        var autoServiceInternalDto = new EnumRusNameDTO(AUTOSERVICE_INTERNAL.name(), AUTOSERVICE_INTERNAL.getInternalRusName());
        var autoServiceExternalDto = new EnumRusNameDTO(AUTOSERVICE_EXTERNAL.name(), AUTOSERVICE_EXTERNAL.getInternalRusName());
        var offlineDto = new EnumRusNameDTO(OFFLINE.name(), OFFLINE.getInternalRusName());
        var dispatcherInternalDto = new EnumRusNameDTO(DISPATCHER_INTERNAL.name(), DISPATCHER_INTERNAL.getInternalRusName());
        var dispatcherExternalDto = new EnumRusNameDTO(DISPATCHER_EXTERNAL.name(), DISPATCHER_EXTERNAL.getInternalRusName());

        var result = mockMvc.perform(get("/services/%s/".formatted(serviceType.name()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumRusNameDTO> actual = objectMapper.readerForListOf(EnumRusNameDTO.class)
                .readValue(result.getResponse().getContentAsString(Charsets.UTF_8));
        switch (serviceType) {
            case AUTOSERVICE -> assertThat(actual)
                    .containsExactlyInAnyOrder(apiDto, autoServiceInternalDto, autoServiceExternalDto, offlineDto);
            case INTERNAL_AUTO_PARK -> assertThat(actual)
                    .containsExactlyInAnyOrder(dispatcherInternalDto, dispatcherExternalDto);
            case CARGO_TRANSPORTATION, EMPLOYEE_TRANSPORTATION -> assertThat(actual)
                    .containsExactlyInAnyOrder(apiDto, dispatcherInternalDto, dispatcherExternalDto);
            default -> throw new NotImplementedException();
        }
    }

    @Test
    @DisplayName("Частичное изменение контрагента")
    void patchContractorTest() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestInternalAutoPark());
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        doNothing().when(internalClient).patchContractor(any(), any(), any(), any());
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                	{
                                		"field":"contactPerson",
                                		"value":{
                                			"externalId":"%s",
                                			"firstName":"Тест",
                                			"lastName":"Тестов",
                                			"patronymic":"Тестович",
                                			"email":"internalautopark@mail.ru",
                                			"phone":"+79270020085"
                                		}
                                	}
                                ]
                                """.formatted(employee.getId())))
                .andExpect(status().isOk()).andReturn();
        var actual = contractorRepository.findById(contractor.getId()).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractor.getId()));

        assertThat("+79270020085").isEqualTo(actual.getContactPersonPhone());
        assertThat("internalautopark@mail.ru").isEqualTo(actual.getContactPersonEmail());
        assertThat("Тест").isEqualTo(actual.getContactPersonFirstName());
        assertThat("Тестов").isEqualTo(actual.getContactPersonLastName());
        assertThat("Тестович").isEqualTo(actual.getContactPersonPatronymic());
    }


    @Test
    @DisplayName("Частичное изменение норматива автомобилей")
    void patchContractorVehicleCountNormTest() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestInternalAutoPark());
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        doNothing().when(internalClient).patchContractor(any(), any(), any(), any());

        final int vehicleCountNormRequest = 500;
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                	{
                                		"field":"vehicleCountNorm",
                                		"value": "%s"
                                	}
                                ]
                                """.formatted(vehicleCountNormRequest)))
                .andExpect(status().isOk()).andReturn();
        Assertions.assertEquals(vehicleCountNormRequest, contractorRepository.findById(contractor.getId()).get().getVehicleCountNorm());
    }

    @Test
    @DisplayName("Частичное изменение норматива автомобилей (исключение)")
    void patchContractorVehicleCountNormTestException() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestInternalAutoPark());
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        FeignException.FeignClientException exception = new FeignException.BadRequest("", Mockito.mock(Request.class), new byte[0], Map.of());
        doThrow(exception).when(internalClient).patchContractor(any(), any(), any(), any());

        final int vehicleCountNormRequest = 500;
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                	{
                                		"field":"vehicleCountNorm",
                                		"value": "%s"
                                	}
                                ]
                                """.formatted(vehicleCountNormRequest)))
                .andExpect(status().isBadRequest()).andReturn();
        Assertions.assertEquals(vehicleCountNormRequest, contractorRepository.findById(contractor.getId()).get().getVehicleCountNorm());
    }


    @Test
    @DisplayName("Получение нормы автомобилей (успешно)")
    void getVehicleNorm_Success() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestInternalAutoPark());
        var organizationId = contractor.getOrganizations().iterator().next();
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        employeeRepository.save(employee);
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        doNothing().when(internalClient).patchContractor(any(), any(), any(), any());

        VehicleNormDto expectedNorm = new VehicleNormDto();
        expectedNorm.setTotalCount(100);
        expectedNorm.setAvailableCount(50);
        when(internalClient.getVehicleNorm(any(), any(), any())).thenReturn(expectedNorm);

        mockMvc.perform(get("/internal-auto-park/vehicle-norm")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", organizationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.availableCount").value(50))
                .andReturn();
    }

    @Test
    void getVehicleNorm_InternalAutoParkNotFound() throws Exception {
        var employee = Employee.builder()
                .consent(true).id(UUID.fromString(USER_ID)).userId(UUID.fromString(USER_ID)).organizationId(organizationId).build();
        var jwt = jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"));
        employeeRepository.save(employee);
        mockMvc.perform(get("/internal-auto-park/vehicle-norm")
                        .with(jwt)
                        .header("Authorization", jwt)
                        .param("organizationId", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    private void assertContractorDto(NewContractorDTO newDto, ContractorDTO dto) {
        // Проверка обязательных полей
        assertEquals(newDto.name(), dto.name());
        assertEquals(newDto.msrn(), dto.msrn());
        assertEquals(newDto.tin(), dto.tin());
        assertEquals(newDto.serviceType(), dto.serviceType());
        assertEquals(newDto.contractorType(), dto.contractorType());
        assertEquals(newDto.vehicleCountNorm(), dto.vehicleCountNorm());

        // Контактные данные
        assertEquals(newDto.contactPersonFirstName(), dto.contactPersonFirstName());
        assertEquals(newDto.contactPersonLastName(), dto.contactPersonLastName());
        assertEquals(newDto.contactPersonPatronymic(), dto.contactPersonPatronymic());
        assertEquals(newDto.contactPersonPhone(), dto.contactPersonPhone());
        assertEquals(newDto.contactPersonEmail(), dto.contactPersonEmail());

        // Дополнительные поля
        assertEquals(newDto.rating(), dto.rating());
        assertEquals(newDto.img(), dto.img());
        assertEquals(newDto.mainDispatcherId(), dto.mainDispatcherId());
    }

    private void assertIntegrationParams(JsonIntegrationParamsDto newDto, JsonIntegrationParamsDto dto) {
        assertEquals(newDto.login(), dto.login());
        assertEquals(newDto.url(), dto.url());
        assertEquals("[protected]", dto.password());

    }
}
