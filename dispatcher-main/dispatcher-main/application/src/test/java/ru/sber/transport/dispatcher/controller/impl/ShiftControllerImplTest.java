package ru.sber.transport.dispatcher.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.dispatcher.DispatcherApplication;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.DeleteShiftRowDTO;
import ru.sber.transport.dispatcher.dto.FirstTitleRequestDto;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.dto.ShiftDTO;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.dto.VehicleShiftsStatusDto;
import ru.sber.transport.dispatcher.service.EwbGrpcService;
import ru.sber.transport.dispatcher.service.ShiftService;
import ru.sber.transport.ewb.grpc.dto.Dto;
import ru.sber.transport.ewb.grpc.service.EwbServiceGrpc;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@IsolatedTest
@Feature("app_platform_dispatcher")
@EmbeddedPostgres
@SpringBootTest(classes = DispatcherApplication.class)
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера смен")
@TestPropertySource(properties = "spring.main.lazy-initialization=true")
@Transactional
@MockBean(JwtDecoder.class)
@MockBean(Key.class)
class ShiftControllerImplTest extends KafkaTest {

    private static final String USER_ID = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private DispatcherRepository dispatcherRepository;

    @SpyBean
    private ShiftService shiftService;

    @MockBean
    private AuthorizationManager<?> manager;

    @MockBean
    private EwbGrpcService ewbGrpcService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(manager);
        contractorRepository.saveAndFlush(Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getId))
                .create()
        );
    }

    @Test
    @DisplayName("Проверка добавления")
    void test_add() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = createShifts(new Random().nextInt(0, 10), vehicle.getId(), driver.getId());
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].driverId".formatted(i)).value(expected.get("driverId").toString()))
                    .andExpect(jsonPath("$.[%s].vehicleId".formatted(i)).value(expected.get("vehicleId").toString()))
                    .andExpect(jsonPath("$.[%s].startDate".formatted(i)).value(expected.get("startDate")))
                    .andExpect(jsonPath("$.[%s].endDate".formatted(i)).value(expected.get("endDate")))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Дата окончания раньше даты начала")
    void test_add_endDateBefore() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].message".formatted(i)).value("Дата начала смены должна быть раньше даты окончания!"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Валидация дто")
    void test_add_validation() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var shifts = List.of(
                Map.of(
                        "index", 0
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.jti(USER_ID).claim("roles", List.of("ANY_ROLE"))).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].body.message".formatted(i)).value("Bad Request"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Совпадение контрагентов водителя и диспетчера")
    void test_add_driverDispatcherMatch() throws Exception {
        var contractor = contractorRepository.findAll().get(0);
        var contractor2 = contractorRepository.saveAndFlush(Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getId))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .create()
        );

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .ignore(Select.field(Autopark::getId))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .ignore(Select.field(Vehicle::getId))
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .set(Select.field(Driver::getContractor), contractor2)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .ignore(Select.field(Driver::getId))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].message".formatted(i)).value("Водитель и диспетчер относятся к разным контрагентам!"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Совпадение филиала водителя и автомобиля (ошибка)")
    void test_add_driverVehicleAutoparkMatchError() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].message".formatted(i)).value("Водитель и автомобиль относятся к разным филиалам!"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Совпадение филиала водителя и автомобиля")
    void test_add_driverVehicleAutoparkMatch() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .ignore(Select.field(Autopark::getId))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .ignore(Select.field(Vehicle::getId))
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), autopark)
                .ignore(Select.field(Driver::getAttributes))
                .ignore(Select.field(Driver::getId))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].driverId".formatted(i)).value(driver.getId().toString()))
                    .andExpect(jsonPath("$.[%s].vehicleId".formatted(i)).value(vehicle.getId().toString()))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Автопарки")
    void test_add_autoparks() throws Exception {
        var contractor = contractorRepository.findAll().get(0);
        var contractor2 = contractorRepository.saveAndFlush(Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getId))
                .create()
        );

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor2)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].message".formatted(i)).value("Автомобиль не относится к контрагенту диспетчера или не активен!"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка добавления. Пересечение смен водителя")
    void test_add_startEndDatesIntersection() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var vehicle2 = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                Map.of(
                        "index", 1,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle2.getId(),
                        "startDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        var expected = shifts.get(1);
        result
                .andExpect(jsonPath("$.[%s].index".formatted(1)).value(expected.get("index")))
                .andExpect(jsonPath("$.[%s].message".formatted(1)).value("Даты начала смены пересекаются с существующей сменой водителя!"));
    }

    @Test
    @DisplayName("Проверка добавления. Пересечение смен автомобиля")
    void test_add_carShiftsIntersection() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var driver2 = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ),
                Map.of(
                        "index", 1,
                        "driverId", driver2.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        var expected = shifts.get(1);
        result
                .andExpect(jsonPath("$.[%s].index".formatted(1)).value(expected.get("index")))
                .andExpect(jsonPath("$.[%s].message".formatted(1)).value("Даты начала смены пересекаются с существующей сменой автомобиля!"));
    }

    @Test
    @DisplayName("Проверка добавления. Ошибка сохранения")
    void test_add_saveException() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = List.of(
                Map.of(
                        "index", 0,
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));

        doThrow(new RuntimeException("toster")).when(shiftService).save(any(), any());

        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()));

        for (var i = 0; i < shifts.size(); i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.[%s].index".formatted(i)).value(expected.get("index")))
                    .andExpect(jsonPath("$.[%s].message".formatted(i)).value("toster"))
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения")
    void test_get() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        List<Shift> shifts = new ArrayList<>();
        for (var i = 0; i < 99; i++) {
            shifts.add(Instancio.of(Shift.class)
                    .ignore(Select.field(Shift::getId))
                    .set(Select.field(Shift::getVehicle), vehicle)
                    .set(Select.field(Shift::getDriver), driver)
                    .set(Select.field(Shift::getContractorId), contractor.getId())
                    .set(Select.field(Shift::isDeleted), false)
                    .set(Select.field(Shift::isActive), false)
                    .create());
        }
        shifts.add(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .create());
        shifts = shiftRepository.saveAllAndFlush(shifts);
        shifts.sort(Comparator.comparing(Shift::getStartDate));

        var result = mockMvc.perform(get("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(shifts.size()))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.content.length()").value(20));

        for (var i = 0; i < 20; i++) {
            var expected = shifts.get(i);
            result
                    .andExpect(jsonPath("$.content.[%s].id".formatted(i)).value(expected.getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].driver.id".formatted(i)).value(expected.getDriver().getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].vehicle.id".formatted(i)).value(expected.getVehicle().getId().toString()))
                    .andExpect(jsonPath("$.content.[%s].startDate".formatted(i)).value(expected.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    .andExpect(jsonPath("$.content.[%s].endDate".formatted(i)).value(expected.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
            ;
        }
    }

    @Test
    @DisplayName("Проверка получения по автомобилям")
    void test_get_by_vehicle() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shift = Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), LocalDateTime.now().minusDays(1))
                .set(Select.field(Shift::getEndDate), LocalDateTime.now().plusDays(1))
                .create();
        shift = shiftRepository.saveAndFlush(shift);

        mockMvc.perform(get("/%s/vehicle-shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .queryParam("startDate", LocalDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .queryParam("endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .queryParam("search", vehicle.getStateNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[%s].vehicle.id".formatted(0)).value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.content.[%s].shifts.[%s].id".formatted(0, 0)).value(shift.getId().toString()));
    }

    @Test
    @DisplayName("Проверка получения статуса по автомобилям")
    void test_get_status_by_vehicle() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shift = Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), LocalDateTime.now().minusDays(1))
                .set(Select.field(Shift::getEndDate), LocalDateTime.now().plusDays(1))
                .create();
        shift = shiftRepository.saveAndFlush(shift);

        mockMvc.perform(post("/%s/vehicle-shift/status/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VehicleShiftsStatusDto(List.of(vehicle.getId())))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[%s].vehicleId".formatted(0)).value(vehicle.getId().toString()))
                .andExpect(jsonPath("$.[%s].driverId".formatted(0)).value(shift.getDriver().getId().toString()))
                .andExpect(jsonPath("$.[%s].online".formatted(0)).value(shift.isActive()));
    }

    @Test
    @DisplayName("Проверка получения одной")
    void test_get_one() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        List<Shift> shifts = new ArrayList<>();
        for (var i = 0; i < 99; i++) {
            shifts.add(Instancio.of(Shift.class)
                    .ignore(Select.field(Shift::getId))
                    .set(Select.field(Shift::getVehicle), vehicle)
                    .set(Select.field(Shift::getDriver), driver)
                    .set(Select.field(Shift::getContractorId), contractor.getId())
                    .set(Select.field(Shift::isDeleted), false)
                    .set(Select.field(Shift::isActive), false)
                    .ignore(Select.field(Shift::getRouteId))
                    .create());
        }
        shifts.add(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), LocalDateTime.now(ZoneOffset.UTC))
                .set(Select.field(Shift::getEndDate), LocalDateTime.now(ZoneOffset.UTC).plusHours(1))
                .ignore(Select.field(Shift::getRouteId))
                .create());
        shifts = shiftRepository.saveAllAndFlush(shifts);
        shifts.sort(Comparator.comparing(Shift::getStartDate));
        var shift = shifts.get(0);

        var result = mockMvc.perform(get("/%s/shift/%s/".formatted(contractor.getId(), shift.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shift.getId().toString()))
                .andExpect(jsonPath("$.driver.id").value(shift.getDriver().getId().toString()))
                .andExpect(jsonPath("$.vehicle.id").value(shift.getVehicle().getId().toString()))
                .andExpect(jsonPath("$.startDate").value(shift.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.endDate").value(shift.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Проверка удаления ряда")
    void test_delete_row() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shifts = createShifts(10, vehicle.getId(), driver.getId());
        var result = mockMvc.perform(post("/%s/shift/".formatted(contractor.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shifts)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(shifts.size()))
                .andReturn();
        var resultShifts = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<ShiftDTO>>() {
        });
        Assertions.assertEquals(10, resultShifts.size());
        mockMvc.perform(delete("/%s/shift/row/%s/".formatted(contractor.getId(), resultShifts.get(0).getRowId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DeleteShiftRowDTO(LocalDate.now().plusDays(2)))))
                .andExpect(status().isOk());
        var shiftsOfRowIsNotDeleted = shiftRepository.findAll().stream().filter(shift -> !shift.isDeleted()).toList();
        Assertions.assertEquals(1, shiftsOfRowIsNotDeleted.size());
    }

    @Test
    @DisplayName("Проверка изменения. Конфликт сохранения")
    void test_edit_saveException() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shift = shiftRepository.save(new Shift(null, contractor.getId(), driver, vehicle, LocalDateTime.now(), LocalDateTime.now().plusDays(2), false, false, null, null, null));

        var shiftMap =
                Map.of(
                        "index", 0,
                        "id", shift.getId(),
                        "driverId", driver.getId(),
                        "vehicleId", vehicle.getId(),
                        "startDate", LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "endDate", LocalDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                );

        var existingShiftList = new ArrayList<Shift>();
        existingShiftList.add((Instancio.of(Shift.class)
                .set(Select.field(Shift::getId), UUID.randomUUID()).create()));
        when(shiftService.findAllByVehicleIdAndDate(any(), any(), any())).thenReturn(existingShiftList);

        mockMvc.perform(put("/%s/shift/%s/".formatted(contractor.getId(), shift.getId()))
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(USER_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shiftMap)))
                .andExpect(status().isConflict());

    }

    private List<Map<String, Object>> createShifts(int count, UUID vehicleId, UUID driverId) {
        return IntStream.range(0, count).mapToObj(idx -> createShift(idx, vehicleId, driverId)).collect(Collectors.toList());
    }

    private Map<String, Object> createShift(int index, UUID vehicleId, UUID driverId) {
        return Map.of(
                "index", index,
                "driverId", driverId,
                "vehicleId", vehicleId,
                "startDate", LocalDateTime.now().plusDays(2L * index).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "endDate", LocalDateTime.now().plusDays(2L * index + 1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

    @Test
    @DisplayName("Проверка получения списка смен. Роль федерального диспетчера")
    void test_getShiftList_federalDispatcherRole() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var searchDate = OffsetDateTime.now(ZoneOffset.UTC);
        var startDate = searchDate.plusHours(2).toLocalDateTime();
        var endDate = startDate.plusDays(1);
        var shift = shiftRepository.save(new ru.sber.transport.dispatcher.database.model.Shift(
                null,
                contractor.getId(),
                driver,
                vehicle,
                startDate,
                endDate,
                false,
                false,
                null,
                null,
                null
        ));

        var result = mockMvc.perform(get("/shifts/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ROLE_FEDERAL_DISPATCHER_CONTRACTOR")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("startDate", searchDate.toString()))
                .andExpect(status().isOk());

        result.andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(shift.getId().toString()))
                .andExpect(jsonPath("$.[0].driverName").value(driver.getLastName() + " " + driver.getFirstName() + " " + driver.getPatronymic()))
                .andExpect(jsonPath("$.[0].vehicleBrand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.[0].vehicleModel").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.[0].vehicleStateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.[0].startDate").exists())
                .andExpect(jsonPath("$.[0].endDate").exists());
    }

    @Test
    @DisplayName("Проверка получения списка смен. Роль главного диспетчера контрагента")
    void test_getShiftList_mainDispatcherRole() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var searchDate = OffsetDateTime.now(ZoneOffset.UTC);
        var startDate = searchDate.plusHours(2).toLocalDateTime();
        var endDate = startDate.plusDays(1);
        var shift = shiftRepository.save(new ru.sber.transport.dispatcher.database.model.Shift(
                null,
                contractor.getId(),
                driver,
                vehicle,
                startDate,
                endDate,
                false,
                false,
                null,
                null,
                null
        ));

        var result = mockMvc.perform(get("/shifts/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("startDate", searchDate.toString()))
                .andExpect(status().isOk());

        result.andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(shift.getId().toString()))
                .andExpect(jsonPath("$.[0].driverName").value(driver.getLastName() + " " + driver.getFirstName() + " " + driver.getPatronymic()))
                .andExpect(jsonPath("$.[0].vehicleBrand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.[0].vehicleModel").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.[0].vehicleStateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.[0].startDate").exists())
                .andExpect(jsonPath("$.[0].endDate").exists());
    }

    @Test
    @DisplayName("Проверка получения списка смен. Роль диспетчера с автопарком")
    void test_getShiftList_dispatcherRole() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), autopark)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var searchDate = OffsetDateTime.now(ZoneOffset.UTC);
        var startDate = searchDate.plusHours(2).toLocalDateTime();
        var endDate = startDate.plusDays(1);
        var shift = shiftRepository.save(new ru.sber.transport.dispatcher.database.model.Shift(
                null,
                contractor.getId(),
                driver,
                vehicle,
                startDate,
                endDate,
                false,
                false,
                null,
                null,
                null
        ));

        // Обновляем диспетчера с автопарком
        dispatcher.setAutopark(autopark);
        dispatcherRepository.saveAndFlush(dispatcher);

        var result = mockMvc.perform(get("/shifts/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("startDate", searchDate.toString()))
                .andExpect(status().isOk());

        result.andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(shift.getId().toString()))
                .andExpect(jsonPath("$.[0].driverName").value(driver.getLastName() + " " + driver.getFirstName() + " " + driver.getPatronymic()))
                .andExpect(jsonPath("$.[0].vehicleBrand").value(vehicle.getModel().getBrand()))
                .andExpect(jsonPath("$.[0].vehicleModel").value(vehicle.getModel().getName()))
                .andExpect(jsonPath("$.[0].vehicleStateNumber").value(vehicle.getStateNumber()))
                .andExpect(jsonPath("$.[0].startDate").exists())
                .andExpect(jsonPath("$.[0].endDate").exists());
    }

    @Test
    @DisplayName("Проверка получения списка смен. Нет смен на указанную дату")
    void test_getShiftList_noShifts() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var startDate = OffsetDateTime.now().minusDays(1);

        var result = mockMvc.perform(get("/shifts/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("startDate", startDate.toString()))
                .andExpect(status().isOk());

        result.andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Проверка получения списка смен. Удаленные смены не возвращаются")
    void test_getShiftList_deletedShifts() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var searchDate = OffsetDateTime.now();
        var startDate = searchDate.plusHours(2).toLocalDateTime();
        var endDate = startDate.plusDays(1);
        // Активная смена
        var shift = shiftRepository.save(new ru.sber.transport.dispatcher.database.model.Shift(
                null,
                contractor.getId(),
                driver,
                vehicle,
                startDate,
                endDate,
                false,
                false,
                null,
                null,
                null
        ));
        // Удаленная смена
        var deletedShift = shiftRepository.save(new ru.sber.transport.dispatcher.database.model.Shift(
                null,
                contractor.getId(),
                driver,
                vehicle,
                startDate.minusDays(1),
                endDate.minusDays(1),
                true,
                false,
                null,
                null,
                null
        ));

        var result = mockMvc.perform(get("/shifts/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ROLE_MAIN_DISPATCHER_CONTRACTOR")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .param("startDate", searchDate.toString()))
                .andExpect(status().isOk());

        result.andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].id").value(shift.getId().toString()));
    }

    private Dispatcher createTestDispatcher(UUID id, Contractor contractor) {
        jdbcTemplate.update("""
                INSERT INTO dispatcher.dispatcher
                 (id, human_readable_id, last_name, first_name, patronymic, phone, email, contractor_id, oauth_id)
                 VALUES
                 (?, 'HRIDISPATCHER1', 'Ivan', 'Petrov', 'Petrovich', '+79000000000', 'aaa@list.ru', ?, ?)
                """, id, contractor.getId(), UUID.randomUUID());
        return dispatcherRepository.getReferenceById(id);
    }

    @Test
    @DisplayName("Проверка получения EWB смен")
    void test_getEwbShifts() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), autopark)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var shift = shiftRepository.save(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), LocalDateTime.now())
                .set(Select.field(Shift::getEndDate), LocalDateTime.now().plusHours(2))
                .set(Select.field(Shift::getRouteId), "ROUTE-001")
                .create());

        var shiftIds = List.of(shift.getId());
        var request = new FirstTitleRequestDto(shiftIds, "+3");

        doAnswer(invocation -> {
            var shifts = invocation.getArgument(0, List.class);
            return shifts.stream().map(s -> {
                var response = new FirstTitleResponseDto();
                response.setHumanReadableId("ROUTE-001");
                response.setFileName("EWB_ROUTE-001.pdf");
                response.setContent(new byte[0]);
                response.setCreationTime(LocalDateTime.now());
                response.setErrorText(null);
                return response;
            }).toList();
        }).when(ewbGrpcService).send(any());

        var result = mockMvc.perform(post("/shifts/ewb/first-title/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].humanReadableId").value("ROUTE-001"))
                .andExpect(jsonPath("$.[0].fileName").value("EWB_ROUTE-001.pdf"))
                .andExpect(jsonPath("$.[0].creationTime").exists())
                .andReturn();

        var response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<FirstTitleResponseDto>>() {});
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("ROUTE-001", response.get(0).getHumanReadableId());
        Assertions.assertEquals("EWB_ROUTE-001.pdf", response.get(0).getFileName());
        Assertions.assertNotNull(response.get(0).getContent());
        Assertions.assertEquals(0, response.get(0).getContent().length);
        Assertions.assertNotNull(response.get(0).getCreationTime());
        Assertions.assertNull(response.get(0).getErrorText());
    }

    @Test
    @DisplayName("Проверка подписания EWB документов")
    void test_signEwbDocument() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .set(Select.field(Autopark::getRoutingId), UUID.randomUUID())
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), autopark)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var startDate = LocalDateTime.now();
        var endDate = LocalDateTime.now().plusHours(2);
        var shift = shiftRepository.save(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), startDate)
                .set(Select.field(Shift::getEndDate), endDate)
                .set(Select.field(Shift::getRouteId), "ROUTE-001")
                .create());

        var signRequest = new SignEwbRequestDto();
        signRequest.setId(shift.getId());
        signRequest.setFileName("EWB_FILE.pdf");
        signRequest.setContent("base64content");
        signRequest.setSignature("signature");
        signRequest.setCreationTime(LocalDateTime.now());
        signRequest.setHumanReadableId("HRID-001");
        signRequest.setTimeZone("+3");
        signRequest.setEwbUuid(UUID.randomUUID());

        var signRequests = List.of(signRequest);
        var result = mockMvc.perform(post("/shifts/ewb/sign/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signRequests)))
                .andExpect(status().isOk())
                .andReturn();

        // Проверяем, что метод вызвался успешно (без ошибок)
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Проверка подписания EWB документов. Пустой список")
    void test_signEwbDocument_empty() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        var signRequests = List.<SignEwbRequestDto>of();
        var result = mockMvc.perform(post("/shifts/ewb/sign/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signRequests)))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    @DisplayName("Проверка валидации количества элементов списка. Более 100 элементов")
    void test_getEwbShifts_maxSizeExceeded() throws Exception {
        var contractor = contractorRepository.findAll().get(0);

        var dispatcher = createTestDispatcher(UUID.fromString(USER_ID), contractor);

        // Генерируем список из 101 UUID (превышение лимита)
        var shiftIds = IntStream.range(0, 101)
                .mapToObj(i -> UUID.randomUUID())
                .toList();

        var request = new FirstTitleRequestDto(shiftIds, "+3");

        var result = mockMvc.perform(post("/shifts/ewb/first-title/")
                        .with(jwt().jwt(builder -> builder.claim("roles", List.of("ANY_ROLE")).jti(dispatcher.getOauthId().toString())).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andReturn();

        // Проверяем, что возвращается ошибка 400
        Assertions.assertEquals(400, result.getResponse().getStatus());
    }
}