package ru.sber.transport.dispatcher.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.ditsib.encription.PasswordEncryption;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.dao.DispatcherRepository;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.dao.IntegrationClientRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.dao.TripsRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Contractor_;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.Trip;
import ru.sber.transport.dispatcher.dto.ContractorDTO;
import ru.sber.transport.dispatcher.dto.LinkRequestDTO;
import ru.sber.transport.dispatcher.dto.feign.RegistrationResponseDto;
import ru.sber.transport.dispatcher.feign.RegistrationClient;
import ru.sber.transport.dispatcher.service.IntegrationClientService;
import ru.sber.transport.dispatcher.testutils.TestAutoparks;
import ru.sber.transport.dispatcher.testutils.TestContractors;
import ru.sber.transport.dispatcher.testutils.TestVehicles;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@DisplayName("Проверка контроллера контрагентов")
@MockBean(Key.class)
@TestPropertySource(properties = "spring.jpa.show-sql=true")
class ContractorControllerTest extends KafkaTest {

    private final static String DISPATCHER_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TripsRepository tripsRepository;

    @Autowired
    private DispatcherRepository dispatcherRepository;

    @Autowired
    private PasswordEncryption passwordEncryption;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IntegrationClientRepository integrationClientRepository;

    @MockBean
    private AuthorizationManager<?> manager;

    @Autowired
    private IntegrationClientService integrationClientService;

    @MockBean
    private RegistrationClient registrationClient;

    private static final String USER_ID = "95a9ddc6-e62d-4061-9e65-47982ec2cf4c";
    public static final UUID userId = UUID.fromString(USER_ID);

    public static final String ORGANIZATION_ID = "f10b775b-51db-4e1c-a747-222296041234";
    public static final UUID organizationId = UUID.fromString(ORGANIZATION_ID);

    public static final UUID REGISTRATION_USER_ID = UUID.randomUUID();


    @BeforeEach
    void setUp() {
        AuthorizeUtils.authorize(manager);
        contractorRepository.deleteAll();
        when(registrationClient.register(any())).thenReturn(RegistrationResponseDto.builder().userId(REGISTRATION_USER_ID).build());
    }

    @Test
    @DisplayName("Добавление")
    void add() throws Exception {

        var newContractor = TestContractors.createTestContractorDto(1);

        var result = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON)
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .content(newContractor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Contractor1"))
                .andExpect(jsonPath("$.technicalAccountOwnerEmail").value("ownerEmail1@mail.ru"))
                .andExpect(jsonPath("$.technicalAccountOwner").value("technicalAccountOwner1"));

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);

        result
                .andExpect(jsonPath("$.id").value(actualDb.getId().toString()))
                .andExpect(jsonPath("$.name").value(actualDb.getName()))
        ;

        assertThat(actualDb.getEmployeeCount()).isEqualTo(1);
        var integrationClients = integrationClientRepository.findAll();
        assertThat(integrationClients).hasSize(1);
        var client = integrationClients.getFirst();
        assertThat(client.getContractorId()).isEqualTo(actualDb.getId());
        assertThat(client.isActive()).isTrue();
    }

    @Test
    @DisplayName("Изменение")
    void edit() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var id = contractor.getId();

        var newContractor = TestContractors.createTestContractorDto(1);

        mockMvc.perform(put("/" + id + "/").contentType(MediaType.APPLICATION_JSON)
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .header("X-Paged", "true")
                .content(newContractor)).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);

        var actualDb = contractorRepository.findAll().get(0);

        assertThat(actualDb.getId()).isEqualTo(id);
        assertThat(actualDb.getName()).isEqualTo("Contractor1");
    }

    @Test
    @DisplayName("Удаление")
    void deleteContractor() throws Exception {
        var contractor =
                contractorRepository
                        .save(TestContractors.createTestContractor());
        var autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var vehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 1));
        var driver = new Driver();
        driver.setLastName("Фамилия");
        driver.setFirstName("Имя");
        driver.setPatronymic("Отчество");
        driver.setHumanReadableId("HRI");
        driver.setEmail("email@mail.ru");
        driver.setContactPhone("+79111111111");
        driver.setContractor(contractor);
        driverRepository.save(driver);
        var shift = new Shift();
        shift.setContractorId(contractor.getId());
        shift.setDriver(driver);
        shift.setVehicle(vehicle);
        shift.setStartDate(LocalDateTime.now());
        shift.setEndDate(LocalDateTime.now().plusHours(1));
        shiftRepository.save(shift);

        assertThat(contractorRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/" + contractor.getId()+ "/").header("X-Paged", "true")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);
        assertThat(contractorRepository.findAll().get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("Удаление. С диспетчерами")
    void deleteContractor_withDispatchers() throws Exception {
        var contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());

        var testDispatcher = createTestDispatcher(UUID.randomUUID(), contractor);

        dispatcherRepository.save(testDispatcher);

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
        dispatcherRepository.saveAll(slaves);

        assertThat(contractorRepository.count()).isEqualTo(1);

        mockMvc.perform(delete("/" + contractor.getId() + "/")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true")).andExpect(status().isOk());

        assertThat(contractorRepository.count()).isEqualTo(1);
        assertThat(contractorRepository.findAll().get(0).isActive()).isFalse();

        assertThat(dispatcherRepository.count()).isEqualTo(11);
        dispatcherRepository.findAll().forEach(d -> assertThat(d.isActive()).isFalse());
    }

    @Test
    @DisplayName("Удаление несуществующего")
    void deleteContractor_nonExists() throws Exception {
        mockMvc.perform(delete("/" + UUID.randomUUID() + "/").header("X-Paged", "true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение одного")
    void getOne() throws Exception {
        var id = contractorRepository.save(TestContractors.createTestContractor()).getId();
        var response = mockMvc.perform(get("/" + id + "/")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))).andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), ContractorDTO.class);
        var expected = contractorRepository.findAll().get(0);

        assertThat(actual.id()).isEqualTo(id);
        assertThat(actual.name()).isEqualTo(expected.getName());
    }

    @Test
    @DisplayName("Частичное изменение контрагента")
    void patchContractor() throws Exception {
        var contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        var dispatcher = createTestDispatcher(UUID.randomUUID(), contractor);
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "field": "mainDispatcherId",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(dispatcher.getId())))
                .andExpect(status().isOk()).andReturn();

        var edited = contractorRepository.findById(contractor.getId()).get();

        assertThat(edited.getMainDispatcher().getId()).isEqualTo(dispatcher.getId());
    }

    @Test
    @DisplayName("Частичное изменение контрагента")
    void patchContractor_editVehicleCountNorm() throws Exception {
        var contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(100);
        contractorRepository.saveAndFlush(contractor);
        var dispatcher = createTestDispatcher(UUID.randomUUID(), contractor);

        final int vehicleCountNorm = 777;
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "field": "vehicleCountNorm",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(vehicleCountNorm)))
                .andExpect(status().isOk()).andReturn();

        var edited = contractorRepository.findById(contractor.getId()).get();
        assertThat(edited.getVehicleCountNorm()).isEqualTo(vehicleCountNorm);
    }

    @Test
    @DisplayName("Частичное изменение контрагента (переданное значение норматива меньше общей суммы)")
    void patchContractor_editVehicleCountNormException() throws Exception {
        var contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(100);
        contractorRepository.saveAndFlush(contractor);

        var autopark1 = TestAutoparks.createTestAutopark(contractor);
        autopark1.setVehicleCountNorm(25);
        autoparkRepository.saveAndFlush(autopark1);

        var autopark2 = TestAutoparks.createTestAutopark(contractor);
        autopark2.setVehicleCountNorm(null); // null значение
        autoparkRepository.saveAndFlush(autopark2);

        var autopark3 = TestAutoparks.createTestAutopark(contractor);
        autopark3.setVehicleCountNorm(15);
        autoparkRepository.saveAndFlush(autopark3);

        var dispatcher = createTestDispatcher(UUID.randomUUID(), contractor);

        final int vehicleCountNorm = 39; // 39 < 25 + 15
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "field": "vehicleCountNorm",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(vehicleCountNorm)))
                .andExpect(status().isConflict()).andReturn();

        var edited = contractorRepository.findById(contractor.getId()).get();
        assertThat(edited.getVehicleCountNorm()).isEqualTo(contractor.getVehicleCountNorm());
    }

    @Test
    @DisplayName("Частичное изменение контрагента (переданное значение норматива больше или равно общей суммы)")
    void patchContractor_editVehicleCountNormSuccess() throws Exception {
        var contractor = contractorRepository.saveAndFlush(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(100);
        contractorRepository.saveAndFlush(contractor);

        var autopark1 = TestAutoparks.createTestAutopark(contractor);
        autopark1.setVehicleCountNorm(25);
        autoparkRepository.saveAndFlush(autopark1);

        var autopark2 = TestAutoparks.createTestAutopark(contractor);
        autopark2.setVehicleCountNorm(null); // null значение
        autoparkRepository.saveAndFlush(autopark2);

        var autopark3 = TestAutoparks.createTestAutopark(contractor);
        autopark3.setVehicleCountNorm(15);
        autoparkRepository.saveAndFlush(autopark3);

        var dispatcher = createTestDispatcher(UUID.randomUUID(), contractor);

        final int vehicleCountNorm = 40; // 40 >= 25 + 15
        mockMvc.perform(patch("/" + contractor.getId() + "/")
                        .with(jwt().jwt(builder -> builder.jti(dispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                    {
                                        "field": "vehicleCountNorm",
                                        "value": "%s"
                                    }
                                ]
                                """.formatted(vehicleCountNorm)))
                .andExpect(status().isOk()).andReturn();

        var edited = contractorRepository.findById(contractor.getId()).get();
        assertThat(edited.getVehicleCountNorm()).isEqualTo(vehicleCountNorm);
    }

    @Test
    @DisplayName("Получение одного несуществующего")
    void getOne_nonExists() throws Exception {
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

        var response = mockMvc.perform(get("/")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(20));

        var expectedList = contractorRepository.findAll(Sort.by(Contractor_.NAME));
        for (var i = 0; i < 20; i++) {
            var expected = expectedList.get(i);
            response
                    .andExpect(jsonPath("$.content[%s].name".formatted(i)).value(expected.getName()));
        }
    }

    @Test
    @DisplayName("Получение всех с фильтрацией по name и isInternal")
    void getAllWithFilters() throws Exception {
        // Создаем тестовые данные
        var externalContractor = TestContractors.createTestContractor();
        externalContractor.setName("External Contractor");
        externalContractor.setInternal(false);
        contractorRepository.save(externalContractor);

        var internalContractor = TestContractors.createTestContractor();
        internalContractor.setName("Internal Contractor");
        internalContractor.setInternal(true);
        contractorRepository.save(internalContractor);

        // Тест фильтрации по name
        var responseByName = mockMvc.perform(get("/?name=External")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andReturn();

        var resultByName = objectMapper.readValue(responseByName.getResponse().getContentAsString(), new TypeReference<Page<ContractorDTO>>(){});
        assertThat(resultByName.getContent()).hasSize(1);
        assertThat(resultByName.getContent().getFirst().name()).isEqualTo("External Contractor");

        // Тест фильтрации по isInternal
        var responseByIsInternal = mockMvc.perform(get("/?isInternal=true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andReturn();

        var resultByIsInternal = objectMapper.readValue(responseByIsInternal.getResponse().getContentAsString(), new TypeReference<Page<ContractorDTO>>(){});
        assertThat(resultByIsInternal.getContent()).hasSize(1);
        assertThat(resultByIsInternal.getContent().getFirst().name()).isEqualTo("Internal Contractor");

        // Тест комбинированной фильтрации
        var responseCombined = mockMvc.perform(get("/?name=Internal&isInternal=true")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andReturn();

        var resultCombined = objectMapper.readValue(responseCombined.getResponse().getContentAsString(), new TypeReference<Page<ContractorDTO>>(){});
        assertThat(resultCombined.getContent()).hasSize(1);
        assertThat(resultCombined.getContent().getFirst().name()).isEqualTo("Internal Contractor");

        // Тест отрицательного сценария - несуществующее имя
        mockMvc.perform(get("/?name=NonExistent")
                        .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Получение свободных автомобилей (ТУЗ)")
    void getAllFreeTransport() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var vehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0));
        var driver = new Driver();
        driver.setHumanReadableId("HRIDRIVER1");
        driver.setFirstName("ksjd");
        driver.setLastName("lldsld");
        driver.setPatronymic("ldslp");
        driver.setContactPhone("+79042294486");
        driver.setEmail("test@test.ru");
        driver.setContractor(contractor);
        driver = driverRepository.save(driver);
        var shift = shiftRepository.save(new Shift(null,
                contractor.getId(), driver, vehicle,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(4).withNano(0),
                LocalDateTime.now(ZoneOffset.UTC).plusHours(4).withNano(0),
                false, false, null, null, null));
        var trip = tripsRepository.save(new Trip(UUID.randomUUID(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1).withNano(0),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).withNano(0),
                contractor.getId(), vehicle.getId()));
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        //Получение свободных автомобилей по марке без временной выборки
        mockMvc.perform(get("/transport/?search=" + vehicle.getModel().getName() + "&timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.content[0].stateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.content[0].trips.length()").value(1))
                .andExpect(jsonPath("$.content[0].trips[0].start").value(trip.getStartTime().format(formatTime)))
                .andExpect(jsonPath("$.content[0].trips[0].end").value(trip.getEndTime().format(formatTime)))
                .andReturn();

        //Получение свободных автомобилей по модели c временной выборкой
        mockMvc.perform(get("/transport/?search=" + vehicle.getModel().getBrand() + "&startDate=" + shift.getStartDate().atOffset(ZoneOffset.UTC) + "&endDate=" + shift.getEndDate().atOffset(ZoneOffset.UTC) + "&timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.content[0].stateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.content[0].trips.length()").value(1))
                .andExpect(jsonPath("$.content[0].trips[0].start").value(trip.getStartTime().format(formatTime)))
                .andExpect(jsonPath("$.content[0].trips[0].end").value(trip.getEndTime().format(formatTime)))
                .andReturn();

        //Получение свободных автомобилей по гос номеру c одной датой во временной выборке
        mockMvc.perform(get("/transport/?search=" + vehicle.getStateNumber() + "&startDate=" + shift.getStartDate().atOffset(ZoneOffset.UTC) + "&timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.content[0].stateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.content[0].trips.length()").value(1))
                .andExpect(jsonPath("$.content[0].trips[0].start").value(trip.getStartTime().format(formatTime)))
                .andExpect(jsonPath("$.content[0].trips[0].end").value(trip.getEndTime().format(formatTime)))
                .andReturn();

        //Получение свободных всех автомобилей только с маркой в ответе
        mockMvc.perform(get("/transport/?result=BRAND&timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model").doesNotExist())
                .andExpect(jsonPath("$.content[0].stateNumber").doesNotExist())
                .andExpect(jsonPath("$.content[0].trips.length()").value(0))
                .andReturn();
    }

    @Test
    @DisplayName("Получение свободных автомобилей (Диспетчер)")
    void getAllFreeTransportByDispatcher() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var dispatcher = new Dispatcher();
        dispatcher.setHumanReadableId("HRIDISPATCHER1");
        dispatcher.setFirstName("ksjd");
        dispatcher.setLastName("lldsld");
        dispatcher.setPatronymic("ldslp");
        dispatcher.setPhone("+79042294486");
        dispatcher.setEmail("test@test.ru");
        dispatcher.setContractor(contractor);
        dispatcher = dispatcherRepository.save(dispatcher);
        var autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var vehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0));
        var vehicle2 = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 1));
        var driver = new Driver();
        driver.setHumanReadableId("HRIDRIVER1");
        driver.setFirstName("ksjd");
        driver.setLastName("lldsld");
        driver.setPatronymic("ldslp");
        driver.setContactPhone("+79042294486");
        driver.setEmail("test@test.ru");
        driver.setContractor(contractor);
        driver = driverRepository.save(driver);
        var shift = shiftRepository.save(new Shift(null,
                contractor.getId(), driver, vehicle,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(4).withNano(0),
                LocalDateTime.now(ZoneOffset.UTC).plusHours(4).withNano(0),
                false, false, null, null, null));
        var trip = tripsRepository.save(new Trip(UUID.randomUUID(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1).withNano(0),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).withNano(0),
                contractor.getId(), vehicle.getId()));
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Dispatcher finalDispatcher = dispatcher;
        //Получение свободных автомобилей c временной выборкой
        mockMvc.perform(get("/transport/?startDate=" + shift.getStartDate().atOffset(ZoneOffset.UTC) + "&endDate=" + shift.getEndDate().atOffset(ZoneOffset.UTC) + "&timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(finalDispatcher.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .header("X-Paged", "true")
                        .header("x-version", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value(vehicle2.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model").value(vehicle2.getModel().getName()))
                .andExpect(jsonPath("$.content[0].stateNumber").value(vehicle2.getStateNumber()))
                .andExpect(jsonPath("$.content[0].trips.length()").value(0))
                .andReturn();
    }

    @Test
    @DisplayName("Связывание диспетчерских")
    void link() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var link = new LinkRequestDTO(
                contractor.getMsrn(),
                contractor.getTin(),
                contractor.getTechnicalAccountOwnerEmail(),
                "login",
                "password");
        mockMvc.perform(post("/link")
                .header("Content-Type", "application/json")
                .content(objectMapper.writeValueAsString(link))).andExpect(status().isOk());
        var integrationClients = integrationClientRepository.findAll();
        assertThat(integrationClients).hasSize(1);
        var client = integrationClients.getFirst();
        assertThat(client.getContractorId()).isEqualTo(contractor.getId());
        assertThat(client.getId()).isEqualTo(REGISTRATION_USER_ID);
        assertThat(client.isActive()).isTrue();
    }

    @Test
    @DisplayName("Получение занятости автомобиля")
    void get_transport_test() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var vehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 0));
        var driver = new Driver();
        driver.setHumanReadableId("HRIDRIVER1");
        driver.setFirstName("ksjd");
        driver.setLastName("lldsld");
        driver.setPatronymic("ldslp");
        driver.setContactPhone("+79042294486");
        driver.setEmail("test@test.ru");
        driver.setContractor(contractor);
        driver = driverRepository.save(driver);
        var shift = shiftRepository.save(new Shift(null,
                contractor.getId(), driver, vehicle,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(4).withNano(0), LocalDateTime.now(ZoneOffset.UTC).plusHours(4).withNano(0),
                false, false, null, null, null));
        var trip = tripsRepository.save(new Trip(UUID.randomUUID(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(1).withNano(0), OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).withNano(0),
                contractor.getId(), vehicle.getId()));
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mockMvc.perform(get("/transport/" + vehicle.getId() + "/trips/?timeZone=Z")
                        .with(jwt().jwt(builder -> builder.jti(contractor.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER"))).header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].start").value(trip.getStartTime().format(formatTime)))
                .andExpect(jsonPath("$.[0].end").value(trip.getEndTime().format(formatTime)))
                .andReturn();
    }

    @Test
    @DisplayName("Получение нормативов для контрагента с автопарками")
    void getVehicleNorm_withAutoparks() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(100);
        contractorRepository.save(contractor);

        var autopark1 = TestAutoparks.createTestAutopark(contractor);
        autopark1.setVehicleCountNorm(30);
        autoparkRepository.save(autopark1);

        var autopark2 = TestAutoparks.createTestAutopark(contractor);
        autopark2.setVehicleCountNorm(20);
        autoparkRepository.save(autopark2);

        mockMvc.perform(get("/" +  contractor.getId().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractorCountNorm").value(100))
                .andExpect(jsonPath("$.totalCount").value(50))
                .andExpect(jsonPath("$.availableCount").value(50));
    }

    @Test
    @DisplayName("Получение нормативов для контрагента без автопарков")
    void getVehicleNorm_withoutAutoparks() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(50);
        contractorRepository.save(contractor);

        mockMvc.perform(get("/" +  contractor.getId().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractorCountNorm").value(50))
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.availableCount").value(50));
    }

    @Test
    @DisplayName("Получение нормативов для контрагента с нулевым нормативом")
    void getVehicleNorm_withZeroNorm() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(0);
        contractorRepository.save(contractor);

        var autopark = TestAutoparks.createTestAutopark(contractor);
        autopark.setVehicleCountNorm(10);
        autoparkRepository.save(autopark);

        mockMvc.perform(get("/" +  contractor.getId().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractorCountNorm").value(0))
                .andExpect(jsonPath("$.totalCount").value(10))
                .andExpect(jsonPath("$.availableCount").value(-10));
    }

    @Test
    @DisplayName("Получение нормативов для несуществующего контрагента")
    void getVehicleNorm_nonExistentContractor() throws Exception {
        mockMvc.perform(get("/" +  UUID.randomUUID().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение нормативов для неактивного контрагента")
    void getVehicleNorm_inactiveContractor() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(50);
        contractor.setActive(false);
        contractorRepository.save(contractor);

        mockMvc.perform(get("/" +  contractor.getId().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение нормативов с несколькими автопарками и null значениями")
    void getVehicleNorm_withNullValues() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        contractor.setVehicleCountNorm(100);
        contractorRepository.save(contractor);

        var autopark1 = TestAutoparks.createTestAutopark(contractor);
        autopark1.setVehicleCountNorm(25);
        autoparkRepository.save(autopark1);

        var autopark2 = TestAutoparks.createTestAutopark(contractor);
        autopark2.setVehicleCountNorm(null); // null значение
        autoparkRepository.save(autopark2);

        var autopark3 = TestAutoparks.createTestAutopark(contractor);
        autopark3.setVehicleCountNorm(15);
        autoparkRepository.save(autopark3);

        mockMvc.perform(get("/" +  contractor.getId().toString() + "/vehicle-norm")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractorCountNorm").value(100))
                .andExpect(jsonPath("$.totalCount").value(40)) // 25 + 0 + 15 = 40
                .andExpect(jsonPath("$.availableCount").value(60));
    }

    @Test
    @DisplayName("Получение транспортных средств контрагента")
    void getVehicles() throws Exception {
        var contractor = contractorRepository.save(TestContractors.createTestContractor());
        var autopark = autoparkRepository.save(TestAutoparks.createTestAutopark(contractor));
        var vehicle = vehicleRepository.save(TestVehicles.createTestVehicle(autopark, 1));

        mockMvc.perform(get("/" + contractor.getId() + "/vehicle/")
                .with(jwt().jwt(builder -> builder.jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .header("X-Paged", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].vin").value(vehicle.getVin()))
                .andExpect(jsonPath("$.content[0].passport").value(vehicle.getPassport()))
                .andExpect(jsonPath("$.content[0].stateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.content[0].insuranceNumber").value(vehicle.getInsuranceNumber()))
                .andExpect(jsonPath("$.content[0].model.brand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.content[0].model.name").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.content[0].model.year").value(vehicle.getModel().getYear()))
                .andExpect(jsonPath("$.content[0].bodyType").value(vehicle.getBodyType()))
                .andExpect(jsonPath("$.content[0].color").value(vehicle.getColor()))
                .andExpect(jsonPath("$.content[0].ecoClass").value(vehicle.getEcoClass().name()))
                .andExpect(jsonPath("$.content[0].chassisType").value(vehicle.getChassisType()))
                .andExpect(jsonPath("$.content[0].transmissionType").value(vehicle.getTransmissionType()))
                .andExpect(jsonPath("$.content[0].fuelConsumption").value(vehicle.getFuelConsumption()))
                .andExpect(jsonPath("$.content[0].manufactureYear").value(vehicle.getManufactureYear()))
                .andExpect(jsonPath("$.content[0].mileage").value(vehicle.getMileage()))
                .andExpect(jsonPath("$.content[0].packageClass").value(vehicle.getPackageClass()))
                .andExpect(jsonPath("$.content[0].maxAllowedWeight").value(vehicle.getMaxAllowedWeight()))
                .andExpect(jsonPath("$.content[0].engineType").value(vehicle.getEngineType()))
                .andExpect(jsonPath("$.content[0].inExploitation").value(vehicle.isInExploitation()))
                .andExpect(jsonPath("$.content[0].vehicleType").value(vehicle.getVehicleType().name()))
                .andReturn();
    }

    private Dispatcher createTestDispatcher(UUID id, Contractor contractor) {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id)
                 VALUES
                 (?, 'HRI', 'LastName', 'FirstName', 'Patronymic', '+79000000000', 'your@email.com', ?)
                """, id, contractor.getId());
        return dispatcherRepository.getReferenceById(id);
    }
}
