package ru.sber.transport.dispatcher.controller;

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
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.DriverDTO;
import ru.sber.transport.dispatcher.dto.DriverLicenseDto;
import ru.sber.transport.dispatcher.dto.NewDriverDTO;
import ru.sber.transport.dispatcher.dto.PatchDataV2;
import ru.sber.transport.dispatcher.dto.enums.PatchField;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "SpringJavaInjectionPointsAutowiringInspection"})
@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest(properties = "authorization.consent-check=true")
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера водителей")
class DriverControllerTest extends KafkaTest implements DispatcherCreator {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private ShiftRepository shiftRepository;
    @Autowired
    private AutoparkRepository autoparkRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private AuthorizationManager<?> manager;

    private Contractor contractor;

    private Driver authDriver;

    private Dispatcher authDispatcher;

    public static final String DRIVER_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";

    @Override
    public JdbcTemplate jdbcTemplate() {
        return jdbcTemplate;
    }

    @BeforeEach
    void createRepository() {
        contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());

        Attribute noSmokeAttribute = Attribute.builder().contractor(contractor).status(ActiveStatus.ACTIVE).name("Не курит").build();
        Attribute childSeatAttribute = Attribute.builder().contractor(contractor).status(ActiveStatus.ACTIVE).name("Детское кресло").build();

        attributeRepository.save(noSmokeAttribute);
        attributeRepository.save(childSeatAttribute);

        authDriver = createTestDriver(UUID.fromString(DRIVER_ID), contractor);
    }

    private Driver createTestDriver(UUID id, Contractor contractor) {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.driver
                 (id, humanreadableid, last_name, first_name, patronymic, contact_phone_number, passport, online, consent, contractor_id)
                 VALUES
                 (?, 'HRIDRIVER1', 'Ivan', 'Petrov', 'Petrovich', '+79000000000', '1454222333', false, true, ?)
                 on conflict(id) do update set contractor_id = excluded.contractor_id
                """, id, contractor.getId());
        return driverRepository.getReferenceById(id);
    }

    @Test
    @DisplayName("Добавление")
    void addNewDriverTest() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var autopark = new Autopark(null, "name", contractor, Collections.EMPTY_LIST, true, null, null);

        autopark = autoparkRepository.save(autopark);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .set(Select.field(NewDriverDTO::autoparkId), autopark.getId())
                .set(Select.field(NewDriverDTO::lastName), "Фамилия-Двойная")
                .create();

        var response =
                mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                                .with(jwt().jwt(builder -> builder
                                        .claim("scope", "DRIVER")
                                        .claim("roles", List.of("ANY_ROLE"))
                                        .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newDriver))).
                        andExpect(status().isOk()).andReturn();

        var saved = contractorRepository.findAll().get(0);
        assertThat(saved.getEmployeeCount()).isEqualTo(1);

        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        assertThat(driverRepository.count()).isEqualTo(2);

        var actualDb = driverRepository.getReferenceById(actual.id());

        assertNotNull(actual.attributes());
        assertThat(actual.attributes()).hasSameSizeAs(newDriver.attributes());

        assertNotNull(actual.driverLicenses());
        assertThat(actual.driverLicenses()).hasSize(2);

        assertThat(actual.lastName()).isEqualTo(newDriver.lastName());
        assertThat(actual.firstName()).isEqualTo(newDriver.firstName());
        assertThat(actual.patronymic()).isEqualTo(newDriver.patronymic());

        assertThat(actual.id()).isEqualTo(actualDb.getId());
        assertThat(actual.lastName()).isEqualTo(actualDb.getLastName());
        assertThat(actual.firstName()).isEqualTo(actualDb.getFirstName());
        assertThat(actual.patronymic()).isEqualTo(actualDb.getPatronymic());
        assertEquals(newDriver.experience(), actual.experience());

        assertNotNull(actual.autoparkName());
        assertNotNull(autopark.getName());
        assertEquals(actual.autoparkName(), autopark.getName());
        assertEquals(actual.autoparkId(), autopark.getId());

    }

    @Test
    @DisplayName("Проверка обновления телефона")
    void test_update() throws Exception {
        var driver = Driver.builder().
                contactPhone("+7(800)9001234").
                driverLicenseNumber("12 34 567890").
                serviceLicenseNumber("AAA-12-12345").
                firstName("First name").
                lastName("Last name ").
                patronymic("Patronymic ").
                passport("2345100000").
                online(false).
                contractor(contractorRepository.findById(contractor.getId()).get()).build();

        driver = driverRepository.save(driver);
        var id = driver.getId();

        var patchDataV2 = List.of(new PatchDataV2(PatchField.PHONE, "+79999999999"));
        var content = objectMapper.writeValueAsString(patchDataV2);

        mockMvc.perform(patch("/self/driver/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(id.toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        var byId = driverRepository.findById(id);

        assertThat(byId)
                .isPresent();
        assertThat(byId.get().getContactPhone())
                .isEqualTo(patchDataV2.get(0).value());
    }

    @Test
    @DisplayName("Проверка обновления tin/snils/driverlicensenumber/issuedate/expirydate/driverlicenses/autoparkid")
    void test_update_tin_snils_drivernumber() throws Exception {
        var autopark = Instancio.of(Autopark.class)
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .ignore(Select.field(Autopark::getId))
                .create();

        autopark = autoparkRepository.save(autopark);

        var driver = Driver.builder().
                contactPhone("+7(800)9001234").
                driverLicenseNumber("12 34 567890").
                serviceLicenseNumber("AAA-12-12345").
                firstName("First name").
                lastName("Last name ").
                patronymic("Patronymic ").
                passport("2345100000").
                online(false).
                contractor(contractorRepository.findById(contractor.getId()).get()).build();

        driver = driverRepository.save(driver);
        var id = driver.getId();

        var date = LocalDate.of(2025, 01, 01);
        var patchDataV2 = List.of(
                new PatchDataV2(PatchField.TIN, "123456789012"),
                new PatchDataV2(PatchField.SNILS, "123-456-789"),
                new PatchDataV2(PatchField.DRIVER_LICENSE_NUMBER, "01 01 000111"),
                new PatchDataV2(PatchField.DRIVER_LICENSES, (Serializable) List.of(DriverLicenseDto.A)),
                new PatchDataV2(PatchField.ISSUE_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.EXPIRY_DATE, "01.01.2025"),
                new PatchDataV2(PatchField.AUTOPARK_ID, autopark.getId())
        );
        var content = objectMapper.writeValueAsString(patchDataV2);

        mockMvc.perform(patch("/" + contractor.getId() + "/drivers/" + driver.getId() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk());

        var byId = driverRepository.findById(id);

        assertThat(byId)
                .isPresent();
        assertThat(byId.get().getTin())
                .isEqualTo("123456789012");
        assertThat(byId.get().getSnils())
                .isEqualTo("123-456-789");
        assertThat(byId.get().getDriverLicenseNumber())
                .isEqualTo("01 01 000111");
        assertThat(byId.get().getIssueDate())
                .isEqualTo(date);
        assertThat(byId.get().getExpiryDate())
                .isEqualTo(date);
        Set<DriverLicense> driverLicenses = byId.get().getDriverLicenses();
        assertThat(driverLicenses).hasSize(1);
        assertThat(driverLicenses.stream().findFirst().get().name()).isEqualTo("A");
    }

    @Test
    @DisplayName("Добавление, дублирующийся паспорт")
    void addDriverTestDuplicatePassport() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                                .with(jwt().jwt(builder -> builder
                                        .claim("scope", "DRIVER")
                                        .claim("roles", List.of("ANY_ROLE"))
                                        .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newDriver))).
                        andExpect(status().isOk());

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000001")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .set(Select.field(NewDriverDTO::passport), newDriver.passport())
                .create();
        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Добавление, дублирующийся номер ВУ")
    void addDriverTestDuplicateDriverLicenseNumber() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk());

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000001")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), newDriver.driverLicenseNumber())
                .set(Select.field(NewDriverDTO::active), true)
                .create();
        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Добавление, дублирующийся СНИЛС")
    void addDriverTestDuplicateSnils() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk());

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000001")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456782")
                .set(Select.field(NewDriverDTO::snils), newDriver.snils())
                .set(Select.field(NewDriverDTO::active), true)
                .create();
        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Добавление, дублирующийся ИНН")
    void addDriverTestDuplicateTin() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk());

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000001")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456782")
                .set(Select.field(NewDriverDTO::tin), newDriver.tin())
                .set(Select.field(NewDriverDTO::active), true)
                .create();
        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isConflict());
    }

    @DisplayName("Проверка добавления свыше нормы")
    @Test
    void test_add_oversize() throws Exception {
        contractor.setEmployeeCount(5000);
        contractorRepository.save(contractor);

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .create();

        var response =
                mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                                .with(jwt().jwt(builder -> builder
                                        .claim("scope", "DRIVER")
                                        .claim("roles", List.of("ANY_ROLE"))
                                        .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .contentType(MediaType.APPLICATION_JSON).
                                content(objectMapper.writeValueAsString(newDriver)))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.entity.name").value("Contractor"))
                        .andExpect(jsonPath("$.problems").exists());
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Добавление в несуществующего контрагента")
    void add_nonExistsContractor() throws Exception {
        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        var result = mockMvc.perform(post("/" + UUID.randomUUID() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DRIVER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Изменение")
    void editDriverTest() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DRIVER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);


        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);
        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456799")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DRIVER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isOk());

        assertThat(driverRepository.count()).isEqualTo(2);

        var actualDb = driverRepository.getReferenceById(actual.id());

        Assertions.assertThat(actualDb.getLastName()).isEqualTo(newDriver.lastName());
        Assertions.assertThat(actualDb.getFirstName()).isEqualTo(newDriver.firstName());
        Assertions.assertThat(actualDb.getPatronymic()).isEqualTo(newDriver.patronymic());
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(contractor.getId());

        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456799")
                .set(Select.field(NewDriverDTO::active), false)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isOk());

        assertThat(driverRepository.count()).isEqualTo(2);

        var saved = contractorRepository.findAll().get(0);
        assertThat(saved.getEmployeeCount()).isZero();

        actualDb = driverRepository.getReferenceById(actual.id());

        Assertions.assertThat(actualDb.getLastName()).isEqualTo(newDriver.lastName());
        Assertions.assertThat(actualDb.getFirstName()).isEqualTo(newDriver.firstName());
        Assertions.assertThat(actualDb.getPatronymic()).isEqualTo(newDriver.patronymic());
        Assertions.assertThat(actualDb.getContractor().getId()).isEqualTo(contractor.getId());

        contractor.setEmployeeCount(5000);
        contractorRepository.save(contractor);

        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456799")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.entity.name").value("Contractor"))
                .andExpect(jsonPath("$.problems").exists());
    }

    @Test
    @DisplayName("Изменение, дубликация паспорта")
    void editDriverDuplicatePassportTest() throws Exception {
        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu1")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isOk()).andReturn();


        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);
        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e3@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000022")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "22 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .set(Select.field(NewDriverDTO::passport), newDriver2.passport())
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение, дубликация номера ВУ")
    void editDriverDuplicateDriverLicenseNumberTest() throws Exception {
        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu1")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isOk()).andReturn();


        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);
        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e3@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000022")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), newDriver2.driverLicenseNumber())
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение, дубликация СНИЛС")
    void editDriverDuplicateSnilsTest() throws Exception {
        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu1")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isOk()).andReturn();


        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);
        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e3@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000022")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "22 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .set(Select.field(NewDriverDTO::snils), newDriver2.snils())
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение, дубликация ИНН")
    void editDriverDuplicateTinTest() throws Exception {
        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        var newDriver2 = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu1")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver2))).
                andExpect(status().isOk()).andReturn();


        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);
        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e3@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNu2")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000022")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "22 12 456782")
                .set(Select.field(NewDriverDTO::active), true)
                .set(Select.field(NewDriverDTO::tin), newDriver2.tin())
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + actual.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Изменение у несуществующего КА")
    void edit_nonExists_Contractor() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);

        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "012 456789")
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        var result = mockMvc.perform(put("/" + UUID.randomUUID() + "/drivers/" + actual.id())
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Изменение несуществующего")
    void edit_nonExists() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .create();

        mockMvc.perform(post("/" + contractor.getId() + "/drivers/").
                        contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk());

        var editDriverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.C);

        newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), editDriverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 4)
                .set(Select.field(NewDriverDTO::email), "e2@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000002")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "012 456799")
                .create();

        var result = mockMvc.perform(put("/" + contractor.getId() + "/drivers/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Получение одного")
    void getDriverById() throws Exception {
        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "20 12 456789")
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult responsePost = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))).
                        contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();

        DriverDTO driverPost = objectMapper.readValue(responsePost.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        MvcResult responseGet = mockMvc.perform(get("/" + contractor.getId() + "/drivers/" + driverPost.id() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).
                andExpect(status().isOk()).andReturn();

        DriverDTO driverGet = objectMapper.readValue(responseGet.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);
        assertThat(driverPost.id()).isEqualTo(driverGet.id());
    }

    @Test
    @DisplayName("Получение одного несуществующего")
    void getOne_nonExists() throws Exception {
        var result = mockMvc.perform(get("/" + contractor.getId() + "/drivers/" + UUID.randomUUID() + "/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                )
                .andExpect(status().isNotFound());

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Получение всех")
    void getAll() throws Exception {
        var itemCount = 100;
        var autopark = Instancio.of(Autopark.class)
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .ignore(Select.field(Autopark::getId))
                .create();

        autopark = autoparkRepository.save(autopark);

        for (var i = 0; i < itemCount; i++) {
            driverRepository.save(Driver.builder().
                    contactPhone("+7(800)9001234").
                    driverLicenseNumber("12 34 567890").
                    serviceLicenseNumber("AAA-12-12345").
                    firstName("First name %03d".formatted(i)).
                    lastName("Last name %03d".formatted(i)).
                    patronymic("Patronymic %03d".formatted(i)).
                    passport("2345 " + (100000 + i)).
                    autopark(autopark).
                    online(false).
                    contractor(contractorRepository.findById(contractor.getId()).get()).build());
        }

        var response = mockMvc.perform(get("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        var expectedList = driverRepository.findAll(Sort.by(Driver_.LAST_NAME));
        for (var i = 0; i < 20; i++) {
            var expected = expectedList.get(i);

            response
                    .andExpect(jsonPath("$.content[%s].lastName".formatted(i)).value(expected.getLastName()))
                    .andExpect(jsonPath("$.content[%s].firstName".formatted(i)).value(expected.getFirstName()))
                    .andExpect(jsonPath("$.content[%s].patronymic".formatted(i)).value(expected.getPatronymic()));
            ;
        }
    }

    @Test
    @DisplayName("Получение всех. Нет КА")
    void getAll_noContractor() throws Exception {
        driverRepository.deleteAll();
        attributeRepository.deleteAll();
        contractorRepository.deleteAll();

        var response = mockMvc.perform(get("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Тест получения профиля водителя")
    void driverProfileTest() throws Exception {
        var result = mockMvc.perform(get("/self/driver/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isOk()).andReturn();

        DriverDTO actual = objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);
        assertNotNull(actual);
        assertThat(actual.id()).isEqualTo(UUID.fromString(DRIVER_ID));
    }

    @Test
    @DisplayName("Тест получения профиля водителя. Не найден")
    void driverProfileTest_notFound() throws Exception {
        driverRepository.deleteAll();

        var result = mockMvc.perform(get("/self/driver/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isNotFound()).andReturn();

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Тест получения авто водителя")
    void driverVehicleTest() throws Exception {
        var autopark = Instancio.of(Autopark.class)
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .ignore(Select.field(Autopark::getId))
                .create();

        autopark = autoparkRepository.save(autopark);

        var vehicle = Instancio.of(Vehicle.class)
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .ignore(Select.field(Vehicle::getId))
                .create();

        vehicle = vehicleRepository.save(vehicle);

        var driver = createTestDriver(UUID.fromString(DRIVER_ID), contractor);

        Shift shift = new Shift();
        shift.setDriver(driver);
        shift.setContractorId(driver.getContractor().getId());
        shift.setVehicle(vehicle);
        shift.setDeleted(false);
        shift.setStartDate(LocalDateTime.now(ZoneOffset.UTC));
        shift.setEndDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        shiftRepository.save(shift);

        var result = mockMvc.perform(get("/self/vehicle/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(vehicle.getVin()))
                .andExpect(jsonPath("$.passport").value(vehicle.getPassport()))
                .andExpect(jsonPath("$.stateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.insuranceNumber").value(vehicle.getInsuranceNumber()))
                .andExpect(jsonPath("$.model.brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.model.name").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.model.year").value(vehicle.getModel().getYear()))
                .andExpect(jsonPath("$.ecoClass").value(vehicle.getEcoClass().name()))
                .andExpect(jsonPath("$.fuelConsumption").value(vehicle.getFuelConsumption()))
                .andExpect(jsonPath("$.packageClass").value(vehicle.getPackageClass()))
                .andExpect(jsonPath("$.mileage").value(vehicle.getMileage()))
                .andExpect(jsonPath("$.color").value(vehicle.getColor()))
                .andExpect(jsonPath("$.manufactureYear").value(vehicle.getManufactureYear()))
                .andExpect(jsonPath("$.maxAllowedWeight").value(vehicle.getMaxAllowedWeight()))
                .andExpect(jsonPath("$.chassisType").value(vehicle.getChassisType()))
                .andExpect(jsonPath("$.transmissionType").value(vehicle.getTransmissionType()))
                .andExpect(jsonPath("$.bodyType").value(vehicle.getBodyType()))
                .andExpect(jsonPath("$.engineType").value(vehicle.getEngineType()))
                .andExpect(jsonPath("$.inExploitation").value(vehicle.isInExploitation()))
                .andExpect(jsonPath("$.autopark.id").value(vehicle.getAutopark().getId().toString()))
                .andExpect(jsonPath("$.autopark.name").value(vehicle.getAutopark().getName()))
                .andExpect(jsonPath("$.autopark.contractor.name").value(vehicle.getAutopark().getContractor().getName()))
                .andExpect(jsonPath("$.autopark.contractor.id").value(vehicle.getAutopark().getContractor().getId().toString()))
                .andExpect(jsonPath("$.autopark.active").value(vehicle.getAutopark().isActive()))
                .andExpect(jsonPath("$.id").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.active").value(vehicle.isActive()));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Тест получения авто водителя. Не найден")
    void driverVehicleTest_notFound() throws Exception {
        driverRepository.deleteAll();

        var result = mockMvc.perform(get("/self/vehicle/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                                .
                        contentType(MediaType.APPLICATION_JSON)).
                andExpect(status().isForbidden()).andReturn();
    }

    @Test
    @DisplayName("Тест подписания ПДн")
    void signPdnTest() throws Exception {
        createTestDispatcher(UUID.fromString(DRIVER_ID), contractor);

        mockMvc.perform(patch("/self/driver/consent/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).
                andExpect(status().isOk());

        var driver = driverRepository.findById(UUID.fromString(DRIVER_ID)).orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(driver.isConsent());
    }

    @Test
    @DisplayName("Тест частичного обновления данных")
    void selfPatchTest() throws Exception {
        createTestDispatcher(UUID.fromString(DRIVER_ID), contractor);
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

        mockMvc.perform(patch("/self/driver/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(authDriver.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(str)).
                andExpect(status().isOk());

        var driver = driverRepository.findById(UUID.fromString(DRIVER_ID)).orElseThrow();

        org.junit.jupiter.api.Assertions.assertTrue(driver.isConsent());
        org.junit.jupiter.api.Assertions.assertEquals(driver.getContactPhone(), "+70949393409");
    }

    @Test
    @DisplayName("Удаление")
    void deleteDriverTest() throws Exception {

        var driverLicenseDTOSet = Set.of(DriverLicenseDto.A, DriverLicenseDto.B);

        var newDriver = Instancio.of(NewDriverDTO.class)
                .set(Select.field(NewDriverDTO::driverLicenses), driverLicenseDTOSet)
                .set(Select.field(NewDriverDTO::rating), 3)
                .set(Select.field(NewDriverDTO::email), "e@mail.ru")
                .set(Select.field(NewDriverDTO::serviceLicenseNumber), "serverLicNum")
                .set(Select.field(NewDriverDTO::cargoLicenceNumber), null)
                .set(Select.field(NewDriverDTO::contactPhone), "+7(900)0000000")
                .set(Select.field(NewDriverDTO::driverLicenseNumber), "23 12 456789")
                .set(Select.field(NewDriverDTO::active), true)
                .ignore(Select.field(NewDriverDTO::attributes))
                .create();

        MvcResult response = mockMvc.perform(post("/" + contractor.getId() + "/drivers/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DRIVER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                        .contentType(MediaType.APPLICATION_JSON).
                        content(objectMapper.writeValueAsString(newDriver))).
                andExpect(status().isOk()).andReturn();
        DriverDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8), DriverDTO.class);

        mockMvc.perform(delete("/" + contractor.getId() + "/drivers/" + actual.id() +"/")
                        .with(jwt().jwt(builder -> builder
                                .claim("scope", "DRIVER")
                                .claim("roles", List.of("ANY_ROLE"))
                                .jti(DRIVER_ID)).authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk());

        var deleted = driverRepository.findById(actual.id());
        Assertions.assertThat(deleted.get().isActive()).isFalse();
    }
}