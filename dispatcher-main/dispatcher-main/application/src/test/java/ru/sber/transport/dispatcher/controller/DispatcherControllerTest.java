package ru.sber.transport.dispatcher.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.dto.DispatcherDto;
import ru.sber.transport.dispatcher.dto.PatchDataV2;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.dispatcher.consts.DateConstants.DDMMYYYY;
import static ru.sber.transport.dispatcher.exceptions.ConflictException.DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest(properties = "authorization.consent-check=true")
@EmbeddedPostgres
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера диспетчерской")
@Transactional
@MockBean(Key.class)
class DispatcherControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private DispatcherRepository dispatcherRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    private Contractor contractor;

    public Dispatcher authDispatcher;

    public static final String DISPATCHER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";

    @BeforeEach
    void beforeEach() {
        AuthorizeUtils.authorize(manager);
        contractor = contractorRepository.saveAndFlush(Contractor.builder()
                .name("Name")
                .tin("1234567890")
                .build());
        authDispatcher = createTestDispatcher(UUID.randomUUID(), contractor);
    }

    private Dispatcher createTestDispatcher(UUID id, Contractor contractor) {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, consent, contractor_id)
                 VALUES
                 (?, 'HRIDISPATCHER1', 'Ivan', 'Petrov', 'Petrovich', '+79000000010', 'aaa@list.ru', 'true', ?)
                """, id, contractor.getId());
        return dispatcherRepository.getReferenceById(id);
    }

    @DisplayName("Проверка валидации")
    @Test
    void test_validation() throws Exception {
        var content =
                """
                        {
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "DAFatkullin@sberbank.ru"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("lastName"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
        ;

        content =
                """
                        {
                            "lastName": "",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("lastName"))
                .andExpect(jsonPath("$.problems[0].value").value(""))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(2))
        ;

        content =
                """
                        {
                            "lastName": "Фамилия",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("firstName"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
        ;

        content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("firstName"))
                .andExpect(jsonPath("$.problems[0].value").value(""))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(2))
        ;

        content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+790000000000",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("phone"))
                .andExpect(jsonPath("$.problems[0].value").value("+790000000000"))
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("Pattern"))
                .andExpect(jsonPath("$.problems[0].constraints[0].value.pattern").value("(\\+7|8)\\d{10}"))
        ;

        content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.problems.length()").value(1))
                .andExpect(jsonPath("$.problems[0].field").value("phone"))
                .andExpect(jsonPath("$.problems[0].value").doesNotExist())
                .andExpect(jsonPath("$.problems[0].constraints.length()").value(1))
                .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
        ;
    }

    @Test
    @DisplayName("Проверка добавления при различных вариациях ewb requirments поля")
    void test_add_ewb_validation() throws Exception {
        var attorneyNumber = UUID.randomUUID().toString();

        // no attorney number
        var content =
                String.format("""
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com",
                            "ewbCreationPossibility" : "true",
                            "personnelNumber" : "%s",
                            "issueDate" : "01.01.2025",
                            "creationSystem" : "WEB",
                            "expiryDate" : "01.01.2026"
                        }
                        """, attorneyNumber);

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("attorneyNumber")))
                .andDo(print());

        // no issue date
        content =
                String.format("""
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com",
                            "ewbCreationPossibility" : "true",
                            "attorneyNumber" : "%s",
                            "personnelNumber" : "123",
                            "creationSystem" : "WEB",
                            "expiryDate" : "01.01.2026"
                        }
                        """, attorneyNumber);

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("issueDate")))
                .andDo(print());

        // no expiry date
        content =
                String.format("""
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com",
                            "ewbCreationPossibility" : "true",
                            "attorneyNumber" : "%s",
                            "personnelNumber" : "123",
                            "creationSystem" : "WEB",
                            "issueDate" : "01.01.2026"
                        }
                        """, attorneyNumber);

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("expiryDate")))
                .andDo(print());

        // no creation system
        content =
                String.format("""
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com",
                            "ewbCreationPossibility" : "true",
                            "attorneyNumber" : "%s",
                            "personnelNumber" : "123",
                            "expiryDate" : "01.01.2028",
                            "issueDate" : "01.01.2026"
                        }
                        """, attorneyNumber);

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("creationSystem")))
                .andDo(print());
    }

    @Test
    @DisplayName("Проверка обновления телефона")
    void test_update() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("your@email.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);
        var id = dispatcher.getId();

        var patchDataV2 = List.of(new PatchDataV2(PatchField.PHONE, "+79999999999"));
        var content = objectMapper.writeValueAsString(patchDataV2);

        mockMvc.perform(patch("/self/dispatcher/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(id.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        Optional<Dispatcher> byId = dispatcherRepository.findById(id);
        assertThat(byId)
                .isPresent();
        assertThat(byId.get().getPhone())
                .isEqualTo(patchDataV2.get(0).value());
    }

    @Test
    @DisplayName("Проверка частичного обновления диспетчера (patch)")
    void test_patch_dispatcher() throws Exception {
        var dispatcher = new Dispatcher();
        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("HRI-001");
        dispatcher.setEmail("old@example.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);
        dispatcher.setEwbCreationPossibility(false);
        dispatcher.setPersonnelNumber("12345");
        dispatcher.setAttorneyNumber("9876");
        dispatcher.setIssueDate(LocalDate.now().minusDays(100));
        dispatcher.setExpiryDate(LocalDate.now().plusDays(100));
        dispatcher.setCreationSystem("WEB");

        dispatcher = dispatcherRepository.save(dispatcher);
        var dispatcherId = dispatcher.getId();
        var contractorId = contractor.getId();

        var autopark = autoparkRepository.save(Autopark.builder().name("Test Autopark").contractor(contractor).build());

        // Подготовка данных для обновления
        var patchData = List.of(
            new PatchDataV2(PatchField.PHONE, "+79999999999"),
            new PatchDataV2(PatchField.CONSENT, "true"),
            new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
            new PatchDataV2(PatchField.ATTORNEY_NUMBER, "54321"),
            new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2023"),
            new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
            new PatchDataV2(PatchField.CREATION_SYSTEM, "MB"),
            new PatchDataV2(PatchField.AUTOPARK_ID, autopark.getId())
        );
        var content = objectMapper.writeValueAsString(patchData);

        // Выполнение patch запроса
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // Проверка обновленных данных
        var updatedDispatcher = dispatcherRepository.findById(dispatcherId).orElseThrow();
        assertEquals("+79999999999", updatedDispatcher.getPhone());
        assertTrue(updatedDispatcher.isConsent());
        assertTrue(updatedDispatcher.isEwbCreationPossibility());
        assertEquals("54321", updatedDispatcher.getAttorneyNumber());
        assertEquals("MB", updatedDispatcher.getCreationSystem());
        assertEquals(LocalDate.parse("01.01.2023", DateTimeFormatter.ofPattern(DDMMYYYY)), updatedDispatcher.getIssueDate());
        assertEquals(LocalDate.parse("01.01.2025", DateTimeFormatter.ofPattern(DDMMYYYY)), updatedDispatcher.getExpiryDate());
        assertNotNull(updatedDispatcher.getAutopark());
    }

    @Test
    @DisplayName("Проверка частичного обновления диспетчера (patch false ewb)")
    void test_patch_dispatcher_false_ewb() throws Exception {
        var dispatcher = new Dispatcher();
        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("HRI-001");
        dispatcher.setEmail("old@example.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);
        dispatcher.setEwbCreationPossibility(false);
        dispatcher.setPersonnelNumber("12345");
        dispatcher.setAttorneyNumber("9876");
        dispatcher.setIssueDate(LocalDate.now().minusDays(100));
        dispatcher.setExpiryDate(LocalDate.now().plusDays(100));
        dispatcher.setCreationSystem("WEB");

        dispatcher = dispatcherRepository.save(dispatcher);
        var dispatcherId = dispatcher.getId();
        var contractorId = contractor.getId();

        // Подготовка данных для обновления
        var patchData = List.of(
                new PatchDataV2(PatchField.PHONE, "+79999999999"),
                new PatchDataV2(PatchField.CONSENT, "true"),
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "false")
        );
        var content = objectMapper.writeValueAsString(patchData);

        // Выполнение patch запроса
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        // Проверка обновленных данных
        var updatedDispatcher = dispatcherRepository.findById(dispatcherId).orElseThrow();
        assertEquals("+79999999999", updatedDispatcher.getPhone());
        assertTrue(updatedDispatcher.isConsent());
        assertFalse(updatedDispatcher.isEwbCreationPossibility());
        assertNull(updatedDispatcher.getAttorneyNumber());
        assertNull(updatedDispatcher.getCreationSystem());
        assertNull(updatedDispatcher.getIssueDate());
        assertNull(updatedDispatcher.getExpiryDate());
    }

    @Test
    @DisplayName("Проверка частичного обновления диспетчера (с ошибками валидации) (patch)")
    void test_patch_dispatcher_exception() throws Exception {
        var dispatcher = new Dispatcher();
        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("HRI-001");
        dispatcher.setEmail("old@example.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);
        var dispatcherId = dispatcher.getId();
        var contractorId = contractor.getId();

        // no attorney number
        var patchData = List.of(
                new PatchDataV2(PatchField.PHONE, "79533000922"),
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
                new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2023"),
                new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.CREATION_SYSTEM, "MB"),
                new PatchDataV2(PatchField.AUTOPARK_ID, UUID.randomUUID().toString())
        );
        var content = objectMapper.writeValueAsString(patchData);
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        // no issue date
        patchData = List.of(
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
                new PatchDataV2(PatchField.ATTORNEY_NUMBER, "54321"),
                new PatchDataV2(PatchField.CREATION_SYSTEM, "54321"),
                new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.AUTOPARK_ID, UUID.randomUUID().toString())
        );
        content = objectMapper.writeValueAsString(patchData);
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        // no expiry date
        patchData = List.of(
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
                new PatchDataV2(PatchField.ATTORNEY_NUMBER, "54321"),
                new PatchDataV2(PatchField.CREATION_SYSTEM, "54321"),
                new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2023"),
                new PatchDataV2(PatchField.AUTOPARK_ID, UUID.randomUUID().toString())
        );
        content = objectMapper.writeValueAsString(patchData);
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());

        // no creation system
        patchData = List.of(
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
                new PatchDataV2(PatchField.ATTORNEY_NUMBER, "54321"),
                new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2023"),
                new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.AUTOPARK_ID, UUID.randomUUID().toString())
        );
        content = objectMapper.writeValueAsString(patchData);
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Проверка частичного обновления диспетчера (с имеющимся attorney number) (patch)")
    void test_patch_dispatcher_attorney_number_exists() throws Exception {
        var attorneyNumber = UUID.randomUUID().toString();
        var dispatcher = new Dispatcher();
        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("HRI-001");
        dispatcher.setEmail("old@example.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);
        var dispatcherId = dispatcher.getId();
        var contractorId = contractor.getId();

        var dispatcher2 = new Dispatcher();
        dispatcher2.setLastName("last-name");
        dispatcher2.setFirstName("first-name");
        dispatcher2.setPatronymic("patronymic");
        dispatcher2.setHumanReadableId("HRI-002");
        dispatcher2.setEmail("old@example.com");
        dispatcher2.setPhone("+79000000001");
        dispatcher2.setAttorneyNumber(attorneyNumber);
        dispatcher2.setContractor(contractor);
        dispatcherRepository.save(dispatcher2);

        // no attorney number
        var patchData = List.of(
                new PatchDataV2(PatchField.PHONE, "79533000922"),
                new PatchDataV2(PatchField.EWB_CREATION_POSSIBILITY, "true"),
                new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2023"),
                new PatchDataV2(PatchField.ATTORNEY_NUMBER, attorneyNumber),
                new PatchDataV2(PatchField.CREATION_SYSTEM, "test"),
                new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.AUTOPARK_ID, UUID.randomUUID().toString())
        );
        var content = objectMapper.writeValueAsString(patchData);
        mockMvc.perform(patch("/{contractorId}/dispatcher/{dispatcherId}/", contractorId, dispatcherId)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format(DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, attorneyNumber)))
                .andDo(print());

    }

    @DisplayName("Проверка добавления")
    @Test
    void test_add() throws Exception {
        var content =
                """
                        {
                            "lastName": "Фамилия-Двойная",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000001",
                            "email": "your@email.com",
                            "ewbCreationPossibility": true,
                            "personnelNumber": "123456789",
                            "attorneyNumber": "987654321",
                            "creationSystem": "WEB",
                            "issueDate": "01.05.1999",
                            "expiryDate": "01.05.2020"
                        }
                        """;

        assertThat(dispatcherRepository.count()).isEqualTo(1);

        var lastName = "Фамилия-Двойная";
        var firstName = "Имя";
        var patronymic = "Отчество";
        var phone = "+79000000001";
        var email = "your@email.com";
        var ewbCreationPossibility = true;
        var personnelNumber = "123456789";
        var attorneyNumber = "987654321";
        var issueDate = "01.05.1999";
        var expiryDate = "01.05.2020";
        var creationSystem = "WEB";

        var result = mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value(lastName))
                .andExpect(jsonPath("$.firstName").value(firstName))
                .andExpect(jsonPath("$.patronymic").value(patronymic))
                .andExpect(jsonPath("$.phone").value(phone))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.ewbCreationPossibility").value(ewbCreationPossibility))
                .andExpect(jsonPath("$.personnelNumber").value(personnelNumber))
                .andExpect(jsonPath("$.attorneyNumber").value(attorneyNumber))
                .andExpect(jsonPath("$.issueDate").value(issueDate))
                .andExpect(jsonPath("$.expiryDate").value(expiryDate))
                .andExpect(jsonPath("$.creationSystem").value(creationSystem))
                .andExpect(jsonPath("$.contractorId").value(contractor.getId().toString()));

        assertThat(dispatcherRepository.count()).isEqualTo(2);

        var actual = dispatcherRepository.findAll().stream().filter(d -> !d.getId().equals(authDispatcher.getId())).findFirst().orElse(null);

        var contractor = contractorRepository.findAll().get(0);

        assertThat(contractor.getEmployeeCount()).isEqualTo(1);
        result.andExpect(jsonPath("$.id").value(actual.getId().toString()));
        Assertions.assertThat(actual.getLastName()).isEqualTo(lastName);
        Assertions.assertThat(actual.getFirstName()).isEqualTo(firstName);
        Assertions.assertThat(actual.getPatronymic()).isEqualTo(patronymic);
        Assertions.assertThat(actual.getPhone()).isEqualTo(phone);
        Assertions.assertThat(actual.getEmail()).isEqualTo(email);
        Assertions.assertThat(actual.isEwbCreationPossibility()).isEqualTo(ewbCreationPossibility);
        Assertions.assertThat(actual.getAttorneyNumber()).isEqualTo(attorneyNumber);
        Assertions.assertThat(actual.getPersonnelNumber()).isEqualTo(personnelNumber);
        Assertions.assertThat(actual.getIssueDate()).isEqualTo(LocalDate.parse(issueDate, DateTimeFormatter.ofPattern(DDMMYYYY)));
        Assertions.assertThat(actual.getExpiryDate()).isEqualTo(LocalDate.parse(expiryDate, DateTimeFormatter.ofPattern(DDMMYYYY)));
        Assertions.assertThat(actual.getContractor().getId()).isEqualTo(contractor.getId());

//        var message = consumeMessage("service.users", UserMessage.class);
//
//        assertThat(message.id()).isEqualTo(actual.getId());
//        assertThat(message.email()).isEqualTo(actual.getEmail());
//        assertThat(message.firstName()).isEqualTo(actual.getFirstName());
//        assertThat(message.lastName()).isEqualTo(actual.getLastName());
//        assertThat(message.patronymic()).isEqualTo(actual.getPatronymic());

        content =
                """
                        {
                            "lastName": "ФамилияДва",
                            "firstName": "ИмяДва",
                            "patronymic": "ОтчествоДва",
                            "phone": "+79000000001",
                            "email": "your@email.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Dispatcher"))
                .andExpect(jsonPath("$.problems").exists())
        ;

        content =
                """
                        {
                            "lastName": "Фамилия2",
                            "firstName": "Имя2",
                            "patronymic": "Отчество2",
                            "phone": "+79000000000",
                            "email": "your@email2.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.problems").exists())
        ;
    }

    @DisplayName("Проверка добавления свыше нормы")
    @Test
    void test_add_oversize() throws Exception {
        contractor.setEmployeeCount(5000);
        contractorRepository.save(contractor);

        var content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        assertThat(dispatcherRepository.count()).isEqualTo(1);

        var lastName = "Фамилия";
        var firstName = "Имя";
        var patronymic = "Отчество";
        var phone = "+79000000000";
        var email = "your@email.com";

        var result = mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.problems").exists());
    }

    @DisplayName("Проверка добавления. Дубликат")
    @Test
    void test_add_duplicate() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Фамилия");
        dispatcher.setFirstName("Имя");
        dispatcher.setPatronymic("Отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("your@email.com");
        dispatcher.setPhone("+79000000000");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        var content =
                """
                        {
                            "lastName": "ФамилияОдин",
                            "firstName": "ИмяОдин",
                            "patronymic": "ОтчествоОдин",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        assertThat(dispatcherRepository.count()).isEqualTo(2);

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value(Dispatcher.class.getSimpleName()))
                .andExpect(jsonPath("$.problems").exists())
        ;

        assertThat(dispatcherRepository.count()).isEqualTo(2);

        content =
                """
                        {
                            "lastName": "ФамилияОдин",
                            "firstName": "ИмяОдин",
                            "patronymic": "ОтчествоОдин",
                            "phone": "+79000000000",
                            "email": "your@email2.com"
                        }
                        """;

        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value(Dispatcher.class.getSimpleName()))
                .andExpect(jsonPath("$.problems").exists());

        assertThat(dispatcherRepository.count()).isEqualTo(2);
    }

    @DisplayName("Добавление с имеющимся номером доверенности")
    @Test
    void test_add_with_attorney_number() throws Exception {
        var dispatcher = new Dispatcher();
        var attorneyNumber = UUID.randomUUID().toString();
        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setActive(true);
        dispatcher.setAttorneyNumber(attorneyNumber);
        dispatcher.setContractor(contractor);

        dispatcherRepository.save(dispatcher);

        var content =
                String.format("""
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000001",
                            "email": "your@email.com",
                            "ewbCreationPossibility": true,
                            "attorneyNumber": "%s",
                            "issueDate": "01.01.2023",
                            "creationSystem": "WEB",
                            "expiryDate": "01.01.2024"
                        }
                        """, attorneyNumber);
        mockMvc.perform(post("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(String.format(DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE, attorneyNumber)))
                .andDo(print());
    }


    @DisplayName("Проверка изменения")
    @Test
    void test_edit() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        assertThat(dispatcherRepository.count()).isEqualTo(2);
        assertThat(dispatcherRepository.existsById(dispatcher.getId())).isTrue();

        var content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000001",
                            "email": "your@email.com"
                        }
                        """;

        var lastName = "Фамилия";
        var firstName = "Имя";
        var patronymic = "Отчество";
        var phone = "+79000000001";
        var email = "your@email.com";

        mockMvc.perform(put("/%s/dispatcher/%s/".formatted(contractor.getId(), dispatcher.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        var actual = dispatcherRepository.findAll().stream().filter(d -> !d.getId().equals(authDispatcher.getId())).findFirst().get();

        assertThat(dispatcherRepository.count()).isEqualTo(2);
        Assertions.assertThat(actual.getLastName()).isEqualTo(lastName);
        Assertions.assertThat(actual.getId()).isEqualTo(dispatcher.getId());
        Assertions.assertThat(actual.getFirstName()).isEqualTo(firstName);
        Assertions.assertThat(actual.getPatronymic()).isEqualTo(patronymic);
        Assertions.assertThat(actual.getPhone()).isEqualTo(phone);
        Assertions.assertThat(actual.getEmail()).isEqualTo(email);
        Assertions.assertThat(actual.getContractor().getId()).isEqualTo(contractor.getId());

//        var message = consumeMessage("service.users", UserMessage.class);
//
//        assertThat(message.id()).isEqualTo(actual.getId());
//        assertThat(message.email()).isEqualTo(actual.getEmail());
//        assertThat(message.firstName()).isEqualTo(actual.getFirstName());
//        assertThat(message.lastName()).isEqualTo(actual.getLastName());
//        assertThat(message.patronymic()).isEqualTo(actual.getPatronymic());
    }

    @DisplayName("Проверка удаления")
    @Test
    void test_delete() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        assertThat(dispatcherRepository.count()).isEqualTo(2);
        assertThat(dispatcherRepository.existsById(dispatcher.getId())).isTrue();

        contractor.setEmployeeCount(1);
        contractorRepository.save(contractor);

        mockMvc.perform(delete("/%s/dispatcher/%s/".formatted(contractor.getId(), dispatcher.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        assertThat(dispatcherRepository.count()).isEqualTo(2);

        var saved = contractorRepository.findAll().get(0);
        assertThat(saved.getEmployeeCount()).isZero();

        var actual = dispatcherRepository.findAll().get(0);

        Assertions.assertThat(actual.getId()).isEqualTo(authDispatcher.getId());

//        var message = consumeMessage("service.users", UserMessage.class);
//
//        assertThat(message.id()).isEqualTo(dispatcher.getId());
//        assertThat(message.email()).isEqualTo(dispatcher.getEmail());
//        assertThat(message.firstName()).isEqualTo(dispatcher.getFirstName());
//        assertThat(message.lastName()).isEqualTo(dispatcher.getLastName());
//        assertThat(message.patronymic()).isEqualTo(dispatcher.getPatronymic());
//        assertThat(message.deleted()).isTrue();
    }

    @DisplayName("Проверка удаления. Диспетчер основной")
    @Test
    void test_delete_main_dispatcher() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        contractor.setMainDispatcher(dispatcher);
        contractorRepository.save(contractor);

        var slaves = new ArrayList<Dispatcher>();
        for (var i = 0; i < 10; i++) {
            var slave = new Dispatcher();

            slave.setLastName("Старая фамилия" + i);
            slave.setFirstName("Старое имя" + i);
            slave.setPatronymic("Старое отчество" + i);
            slave.setHumanReadableId("Старое HRI" + i);
            slave.setEmail("old@email.ru" + i);
            slave.setPhone("+79111111111" + i);
            slave.setContractor(contractor);

            slaves.add(slave);
        }
        slaves = new ArrayList<>(dispatcherRepository.saveAll(slaves));

        assertThat(dispatcherRepository.count()).isEqualTo(12);
        assertThat(dispatcherRepository.existsById(dispatcher.getId())).isTrue();

        contractor.setEmployeeCount(12);
        contractorRepository.save(contractor);

        mockMvc.perform(delete("/%s/dispatcher/%s/".formatted(contractor.getId(), dispatcher.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        var saved = contractorRepository.findAll().get(0);
        assertThat(saved.getEmployeeCount()).isZero();

        assertThat(contractorRepository.getReferenceById(contractor.getId()).getMainDispatcher()).isNull();

        assertThat(dispatcherRepository.count()).isEqualTo(12);

//        var messages = consumeMessages("service.users", UserMessage.class);
//        messages.sort(Comparator.comparing(UserMessage::lastName));
//
//        assertThat(messages).hasSize(11);
//        assertThat(messages.get(0).id()).isEqualTo(dispatcher.getId());
//        assertThat(messages.get(0).email()).isEqualTo(dispatcher.getEmail());
//        assertThat(messages.get(0).firstName()).isEqualTo(dispatcher.getFirstName());
//        assertThat(messages.get(0).lastName()).isEqualTo(dispatcher.getLastName());
//        assertThat(messages.get(0).patronymic()).isEqualTo(dispatcher.getPatronymic());
//        assertThat(messages.get(0).deleted()).isTrue();
//
//        for (var i = 0; i < 10; i++) {
//            var actual = messages.get(i + 1);
//            var expected = slaves.get(i);
//
//            assertThat(actual.id()).isEqualTo(expected.getId());
//            assertThat(actual.email()).isEqualTo(expected.getEmail());
//            assertThat(actual.firstName()).isEqualTo(expected.getFirstName());
//            assertThat(actual.lastName()).isEqualTo(expected.getLastName());
//            assertThat(actual.patronymic()).isEqualTo(expected.getPatronymic());
//            assertThat(actual.deleted()).isTrue();
//        }
    }

    @DisplayName("Проверка удаления. Диспетчерская")
    @Test
    void test_delete_dispatcher_room() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        contractor.setMainDispatcher(dispatcher);
        contractorRepository.save(contractor);

        var slaves = new ArrayList<Dispatcher>();
        for (var i = 0; i < 10; i++) {
            var slave = new Dispatcher();

            slave.setLastName("Старая фамилия" + i);
            slave.setFirstName("Старое имя" + i);
            slave.setPatronymic("Старое отчество" + i);
            slave.setHumanReadableId("Старое HRI" + i);
            slave.setEmail("old@email.ru" + i);
            slave.setPhone("+79111111111" + i);
            slave.setContractor(contractor);

            slaves.add(slave);
        }
        slaves = new ArrayList<>(dispatcherRepository.saveAll(slaves));

        assertThat(dispatcherRepository.count()).isEqualTo(12);
        assertThat(dispatcherRepository.existsById(dispatcher.getId())).isTrue();

        contractor.setEmployeeCount(12);
        contractorRepository.save(contractor);

        mockMvc.perform(delete("/%s/dispatcher/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());

        assertThat(contractorRepository.getReferenceById(contractor.getId()).getMainDispatcher()).isNull();

        assertThat(dispatcherRepository.count()).isEqualTo(12);

        var saved = contractorRepository.findAll().get(0);
        assertThat(saved.getEmployeeCount()).isZero();

//        var messages = consumeMessages("service.users", UserMessage.class);
//        messages.sort(Comparator.comparing(UserMessage::lastName));
//
//        assertThat(messages).hasSize(11);
//        assertThat(messages.get(0).id()).isEqualTo(dispatcher.getId());
//        assertThat(messages.get(0).email()).isEqualTo(dispatcher.getEmail());
//        assertThat(messages.get(0).firstName()).isEqualTo(dispatcher.getFirstName());
//        assertThat(messages.get(0).lastName()).isEqualTo(dispatcher.getLastName());
//        assertThat(messages.get(0).patronymic()).isEqualTo(dispatcher.getPatronymic());
//        assertThat(messages.get(0).deleted()).isTrue();
//
//        for (var i = 0; i < 10; i++) {
//            var actual = messages.get(i + 1);
//            var expected = slaves.get(i);
//
//            assertThat(actual.id()).isEqualTo(expected.getId());
//            assertThat(actual.email()).isEqualTo(expected.getEmail());
//            assertThat(actual.firstName()).isEqualTo(expected.getFirstName());
//            assertThat(actual.lastName()).isEqualTo(expected.getLastName());
//            assertThat(actual.patronymic()).isEqualTo(expected.getPatronymic());
//            assertThat(actual.deleted()).isTrue();
//        }
    }

    @DisplayName("Проверка получения")
    @Test
    void test_get() throws Exception {
        var dispatcher = new Dispatcher();

        dispatcher.setLastName("Старая фамилия");
        dispatcher.setFirstName("Старое имя");
        dispatcher.setPatronymic("Старое отчество");
        dispatcher.setHumanReadableId("Старое HRI");
        dispatcher.setEmail("old@email.ru");
        dispatcher.setPhone("+79111111111");
        dispatcher.setContractor(contractor);

        dispatcher = dispatcherRepository.save(dispatcher);

        var result = mockMvc.perform(get("/%s/dispatcher/%s/".formatted(contractor.getId(), dispatcher.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dispatcher.getId().toString()))
                .andExpect(jsonPath("$.lastName").value(dispatcher.getLastName()))
                .andExpect(jsonPath("$.firstName").value(dispatcher.getFirstName()))
                .andExpect(jsonPath("$.patronymic").value(dispatcher.getPatronymic()))
                .andExpect(jsonPath("$.humanReadableId").value(dispatcher.getHumanReadableId()))
                .andExpect(jsonPath("$.email").value(dispatcher.getEmail()))
                .andExpect(jsonPath("$.phone").value(dispatcher.getPhone()))
                .andExpect(jsonPath("$.contractorId").value(dispatcher.getContractor().getId().toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @DisplayName("Проверка получения всех")
    @Test
    void test_getAll() throws Exception {
        var dispatchers = new ArrayList<Dispatcher>();
        for (var i = 0; i < 10000; i++) {
            var dispatcher = new Dispatcher();

            dispatcher.setLastName("Старая фамилия %05d".formatted(i));
            dispatcher.setFirstName("Старое имя %05d".formatted(i));
            dispatcher.setPatronymic("Старое отчество %05d".formatted(i));
            dispatcher.setHumanReadableId("Старое HRI %05d".formatted(i));
            dispatcher.setEmail("%05dold@email.ru".formatted(i));
            dispatcher.setPhone("+791111%05d".formatted(i));
            dispatcher.setEwbCreationPossibility(true);
            dispatcher.setPersonnelNumber("pn%05d".formatted(i));
            dispatcher.setAttorneyNumber("an%05d".formatted(i));
            dispatcher.setExpiryDate(LocalDate.now().minusDays(i));
            dispatcher.setIssueDate(LocalDate.now().plusDays(i));
            dispatcher.setContractor(contractor);
            dispatcher.setCreationSystem("WEB");

            dispatchers.add(dispatcher);
        }
        dispatcherRepository.saveAll(dispatchers);

        var result = mockMvc.perform(get("/%s/dispatcher/?lastName=Старая фамилия 00000&firstName=Старое имя 00000&patronymic=Старое отчество 00000".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        var dispatcher = dispatchers.get(0);
        result
                .andExpect(jsonPath("$.content[%s].id".formatted(0)).value(dispatcher.getId().toString()))
                .andExpect(jsonPath("$.content[%s].lastName".formatted(0)).value(dispatcher.getLastName()))
                .andExpect(jsonPath("$.content[%s].firstName".formatted(0)).value(dispatcher.getFirstName()))
                .andExpect(jsonPath("$.content[%s].patronymic".formatted(0)).value(dispatcher.getPatronymic()))
                .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(0)).value(dispatcher.getHumanReadableId()))
                .andExpect(jsonPath("$.content[%s].email".formatted(0)).value(dispatcher.getEmail()))
                .andExpect(jsonPath("$.content[%s].phone".formatted(0)).value(dispatcher.getPhone()))
                .andExpect(jsonPath("$.content[%s].contractorId".formatted(0)).value(dispatcher.getContractor().getId().toString()))
                .andExpect(jsonPath("$.content[%s].ewbCreationPossibility".formatted(0)).value(dispatcher.isEwbCreationPossibility()))
                .andExpect(jsonPath("$.content[%s].personnelNumber".formatted(0)).value(dispatcher.getPersonnelNumber()))
                .andExpect(jsonPath("$.content[%s].attorneyNumber".formatted(0)).value(dispatcher.getAttorneyNumber()))
                .andExpect(jsonPath("$.content[%s].creationSystem".formatted(0)).value(dispatcher.getCreationSystem()))
                .andExpect(jsonPath("$.content[%s].expiryDate".formatted(0)).value(dispatcher.getExpiryDate().minusDays(0).format(DateTimeFormatter.ofPattern(DDMMYYYY))))
                .andExpect(jsonPath("$.content[%s].issueDate".formatted(0)).value(dispatcher.getIssueDate().plusDays(0).format(DateTimeFormatter.ofPattern(DDMMYYYY))));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @DisplayName("Проверка получения всех")
    @Test
    void test_getAll_withActiveFilter() throws Exception {
        var dispatchers = new ArrayList<Dispatcher>();
        for (var i = 0; i < 20; i++) {
            var dispatcher = new Dispatcher();

            dispatcher.setLastName("Старая фамилия %05d".formatted(i));
            dispatcher.setFirstName("Старое имя %05d".formatted(i));
            dispatcher.setPatronymic("Старое отчество %05d".formatted(i));
            dispatcher.setHumanReadableId("Старое HRI %05d".formatted(i));
            dispatcher.setEmail("%05dold@email.ru".formatted(i));
            dispatcher.setPhone("+791111%05d".formatted(i));
            dispatcher.setContractor(contractor);
            dispatcher.setActive(i % 2 == 0);

            dispatchers.add(dispatcher);
        }
        dispatcherRepository.saveAll(dispatchers);

        var result = mockMvc.perform(get("/%s/dispatcher/?active=TRUE".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(11));

        var expectedList = dispatcherRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Dispatcher::getLastName))
                .filter(Dispatcher::isActive)
                .toList();

        for (var i = 0; i < 10; i++) {
            var expected = expectedList.get(i);
            result
                    .andExpect(jsonPath("$.content[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(expected.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(expected.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].patronymic".formatted(i)).value(expected.getPatronymic()))
                    .andExpect(jsonPath("$.content[%s].humanReadableId".formatted(i)).value(expected.getHumanReadableId()))
                    .andExpect(jsonPath("$.content[%s].email".formatted(i)).value(expected.getEmail()))
                    .andExpect(jsonPath("$.content[%s].phone".formatted(i)).value(expected.getPhone()))
                    .andExpect(jsonPath("$.content[%s].contractorId".formatted(0)).value(expected.getContractor().getId().toString()));
        }
    }

    //Требуется переработка
    @DisplayName("Проверка получения всех. Проекция селекта")
    @Test
    @Disabled
    void test_getAll_select_projection() throws Exception {
        var dispatchers = new ArrayList<Dispatcher>();
        for (var i = 0; i < 10000; i++) {
            var dispatcher = new Dispatcher();

            dispatcher.setLastName("Old ln %05d".formatted(i));
            dispatcher.setFirstName("Old fn %05d".formatted(i));
            dispatcher.setPatronymic("Old mn %05d".formatted(i));
            dispatcher.setHumanReadableId("Old HRI %05d".formatted(i));
            dispatcher.setEmail("%05dold@email.ru".formatted(i));
            dispatcher.setPhone("+791111%05d".formatted(i));
            dispatcher.setContractor(contractor);

            dispatchers.add(dispatcher);
        }
        dispatcherRepository.saveAll(dispatchers);

        var result = mockMvc.perform(get("/%s/dispatcher/?projection=SELECT".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10000));

        dispatchers.sort(Comparator.comparing(Dispatcher::getLastName));
        for (var i = 0; i < 20; i++) {
            var dispatcher = dispatchers.get(i);
            result
                    .andExpect(jsonPath("$.[%s].id".formatted(i)).value(dispatcher.getId().toString()))
                    .andExpect(jsonPath("$.[%s].lastName".formatted(i)).value(dispatcher.getLastName()))
                    .andExpect(jsonPath("$.[%s].firstName".formatted(i)).value(dispatcher.getFirstName()))
                    .andExpect(jsonPath("$.[%s].patronymic".formatted(i)).value(dispatcher.getPatronymic()))
                    .andExpect(jsonPath("$.[%s].humanReadableId".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].email".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].phone".formatted(i)).doesNotExist())
                    .andExpect(jsonPath("$.[%s].contractorId".formatted(i)).doesNotExist());
        }
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(200);
    }

    @DisplayName("Проверка добавления. Нет контрагента")
    @Test
    void test_add_contractor_not_found() throws Exception {
        var contractor = UUID.randomUUID();
        var content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "+79000000000",
                            "email": "your@email.com"
                        }
                        """;

        var result = mockMvc.perform(post("/%s/dispatcher/".formatted(contractor))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.entity.id").value(contractor.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @DisplayName("Проверка изменения. Нет контрагента")
    @Test
    void test_edit_contractor_not_found() throws Exception {
        var contractor = UUID.randomUUID();
        var content =
                """
                        {
                            "lastName": "Фамилия",
                            "firstName": "Имя",
                            "patronymic": "Отчество",
                            "phone": "89000000000",
                            "email": "your@email.com"
                        }
                        """;

        var result = mockMvc.perform(put("/%s/dispatcher/%s/".formatted(contractor, UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.entity.id").value(contractor.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @DisplayName("Проверка удаления. Нет контрагента")
    @Test
    void test_delete_contractor_not_found() throws Exception {
        var contractor = UUID.randomUUID();
        var result = mockMvc.perform(delete("/%s/dispatcher/%s/".formatted(contractor, UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.entity.id").value(contractor.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @DisplayName("Проверка получения. Нет контрагента")
    @Test
    void test_get_contractor_not_found() throws Exception {
        var contractor = UUID.randomUUID();
        var result = mockMvc.perform(get("/%s/dispatcher/%s/".formatted(contractor, UUID.randomUUID()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.entity.id").value(contractor.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @DisplayName("Проверка получения. Нет диспетчера")
    @Test
    void test_get_dispatcher_not_found() throws Exception {
        var uuid = UUID.randomUUID();
        var result = mockMvc.perform(get("/%s/dispatcher/%s/".formatted(contractor.getId(), uuid))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Dispatcher"))
                .andExpect(jsonPath("$.entity.id").value(uuid.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @DisplayName("Проверка получения всех. Нет контрагента")
    @Test
    void test_getAll_contractor_not_found() throws Exception {
        var contractor = UUID.randomUUID();
        var result = mockMvc.perform(get("/%s/dispatcher/".formatted(contractor))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.entity.id").value(contractor.toString()));
        assertThat(result.andReturn().getResponse().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("Тест получения профиля диспетчера")
    void dispatcherProfileTest() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, consent, contractor_id)
                 VALUES
                 (?, 'HRIDISPATCHER2', 'Ivan', 'Petrov', 'Petrovich', '+79000000012', 'aaa122@list.ru', 'true', ?)
                """, UUID.fromString(DISPATCHER_ID), contractor.getId());

        var result = mockMvc.perform(get("/self/dispatcher/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DISPATCHER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();

        DispatcherDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), DispatcherDto.class);
        assertNotNull(actual);
        assertThat(actual.id()).isEqualTo(UUID.fromString(DISPATCHER_ID));
    }

    @Test
    @DisplayName("Тест получения профиля диспетчера (oauthId)")
    void dispatcherProfileByOauthTest() throws Exception {
        var oauthId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, consent, oauth_id, contractor_id)
                 VALUES
                 (?, 'HRIDISPATCHER2', 'Ivan', 'Petrov', 'Petrovich', '+79000000012', 'aaa122@list.ru', 'true', ?, ?)
                """, UUID.fromString(DISPATCHER_ID), oauthId, contractor.getId());

        var result = mockMvc.perform(get("/self/dispatcher/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(oauthId.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();

        DispatcherDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), DispatcherDto.class);
        assertNotNull(actual);
        assertThat(actual.id()).isEqualTo(UUID.fromString(DISPATCHER_ID));
    }

    @Test
    @DisplayName("Тест подписания ПДн")
    void signPdnTest() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, consent, contractor_id)
                 VALUES
                 (?, 'HRIDISPATCHER2', 'Ivan', 'Petrov', 'Petrovich', '+79000000012', 'aaa122@list.ru', 'true', ?)
                """, UUID.fromString(DISPATCHER_ID), contractor.getId());

        mockMvc.perform(patch("/self/dispatcher/consent/")
                        .with(jwt().jwt(builder -> builder.jti(DISPATCHER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk());

        var dispatcher = dispatcherRepository.findById(UUID.fromString(DISPATCHER_ID)).orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(dispatcher.isConsent());
    }

    @Test
    @DisplayName("Тест частичного обновления данных")
    void selfPatchTest() throws Exception {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, consent, contractor_id)
                 VALUES
                 (?, 'HRIDISPATCHER2', 'Ivan', 'Petrov', 'Petrovich', '+79000000012', 'aaa122@list.ru', 'true', ?)
                """, UUID.fromString(DISPATCHER_ID), contractor.getId());
        var str = """
                [
                    {
                        "field":"phone",
                        "value":"+70949393409"
                    },
                    {
                        "field":"consent",
                        "value":"true"
                    }
                ]
                """;

        mockMvc.perform(patch("/self/dispatcher/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(DISPATCHER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(str)).
                andExpect(status().isOk());

        var driver = dispatcherRepository.findById(UUID.fromString(DISPATCHER_ID)).orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(driver.isConsent());
        org.junit.jupiter.api.Assertions.assertEquals(driver.getPhone(), "+70949393409");
    }

    @DisplayName("Проверка получения всех с сортировкой и фильтрацией по автопарку")
    @Test
    void test_getAll_withAutoparkSortingAndFiltering() throws Exception {
        // Создаем контрагента и автопарки
        var autopark1 = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::getName), "Автопарк 1")
                .create());

        var autopark2 = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::getName), "Автопарк 2")
                .create());


        List<UUID> autoparkIds = new ArrayList<>();
        autoparkIds.add(autopark1.getId());
        autoparkIds.add(autopark2.getId());
        autoparkIds.sort(Comparator.comparing(UUID::toString));

        // Создаем диспетчеров с разными автопарками и без автопарка
        var dispatchers = new ArrayList<Dispatcher>();

        // Диспетчер с автопарком 1
        dispatchers.add(createDispatcher("Иван", "Иванов", "Иванович", "+79000000001", "ivan@example.com", autopark1));

        // Диспетчер с автопарком 2
        dispatchers.add(createDispatcher("Петр", "Петров", "Петрович", "+79000000002", "petr@example.com", autopark2));

        // Диспетчер с автопарком 1 (для проверки сортировки)
        dispatchers.add(createDispatcher("Алексей", "Алексеев", "Алексеевич", "+79000000003", "alex@example.com", autopark1));

        // Диспетчер без автопарка
        dispatchers.add(createDispatcher("Сергей", "Сергеев", "Сергеевич", "+79000000004", "sergey@example.com", null));

        // Диспетчер с автопарком 2 (для проверки сортировки)
        dispatchers.add(createDispatcher("Михаил", "Михайлов", "Михайлович", "+79000000005", "mikhail@example.com", autopark2));

        dispatcherRepository.saveAll(dispatchers);

        var filterResult = mockMvc.perform(get("/%s/dispatcher/?autoparkId=%s"
                        .formatted(contractor.getId(), autopark1.getId()))
                .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString()))
                        .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        // Проверяем, что все возвращенные диспетчеры принадлежат нужному автопарку
        filterResult
                .andExpect(jsonPath("$.content[0].autoparkId").value(autopark1.getId().toString()))
                .andExpect(jsonPath("$.content[1].autoparkId").value(autopark1.getId().toString()))
                .andExpect(jsonPath("$.content[0].lastName").value("Алексеев"))
                .andExpect(jsonPath("$.content[1].lastName").value("Иванов"));

        // Тест сортировки по автопарку (ASC)
        var sortAscResult = mockMvc.perform(get("/%s/dispatcher/?field=AUTOPARK_ID&direction=ASC".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());

        // Проверяем порядок: сначала диспетчеры с автопарком 1, потом с автопарком 2, потом без автопарка
        sortAscResult
                .andDo(print())
                .andExpect(jsonPath("$.content[0].autoparkId").value(autoparkIds.get(0).toString()))
                .andExpect(jsonPath("$.content[1].autoparkId").value(autoparkIds.get(0).toString()))
                .andExpect(jsonPath("$.content[2].autoparkId").value(autoparkIds.get(1).toString()))
                .andExpect(jsonPath("$.content[3].autoparkId").value(autoparkIds.get(1).toString()))
                .andExpect(jsonPath("$.content[4].autoparkId").doesNotExist())
                .andExpect(jsonPath("$.content[5].autoparkId").doesNotExist());

//        // Тест сортировки по автопарку (DESC)
        var sortDescResult = mockMvc.perform(get("/%s/dispatcher/?field=AUTOPARK_ID&direction=DESC".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DISPATCHER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                )
                .andExpect(status().isOk());

        // Проверяем обратный порядок
        sortDescResult
                .andDo(print())
                .andExpect(jsonPath("$.content[0].autoparkId").doesNotExist()) // созданынй нами
                .andExpect(jsonPath("$.content[1].autoparkId").doesNotExist()) // authDispatcher в before each
                .andExpect(jsonPath("$.content[2].autoparkId").value(autoparkIds.get(1).toString()))
                .andExpect(jsonPath("$.content[3].autoparkId").value(autoparkIds.get(1).toString()))
                .andExpect(jsonPath("$.content[4].autoparkId").value(autoparkIds.get(0).toString()))
                .andExpect(jsonPath("$.content[5].autoparkId").value(autoparkIds.get(0).toString()));
    }

    private Dispatcher createDispatcher(String firstName, String lastName, String patronymic,
                                        String phone, String email, Autopark autopark) {
        var dispatcher = new Dispatcher();
        dispatcher.setFirstName(firstName);
        dispatcher.setLastName(lastName);
        dispatcher.setPatronymic(patronymic);
        dispatcher.setPhone(phone);
        dispatcher.setEmail(email);
        dispatcher.setContractor(contractor);
        dispatcher.setAutopark(autopark);
        dispatcher.setHumanReadableId("HRIDISPATCHER " + firstName);
        return dispatcher;
    }

}