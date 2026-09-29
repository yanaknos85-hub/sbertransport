package ru.sber.transport.telemechanic.controller.impl;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.predict.CarNumberDto;
import ru.sber.transport.telemechanic.dto.predict.CarPlateResponseDto;
import ru.sber.transport.telemechanic.dto.predict.PredictResponseDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.exception.RequestNotFoundException;
import ru.sber.transport.telemechanic.handler.RequestExceptionHandler;
import ru.sber.transport.telemechanic.service.FileService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.*;
import static ru.sber.transport.telemechanic.enumerate.Role.ROLE_DRIVER;

@DisplayName("Проверка мониторинга заявок")
@TestPropertySource(properties = { "spring.cloud.kubernetes.enabled=false" })
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@EmbeddedPostgres
class RequestControllerImplTest {
    private static final String REQUEST_URI = "/request";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private CheckRepository checkRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private TransportRepository transportRepository;
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;
    @MockitoBean
    private RestTemplate restTemplate;
    @MockitoBean
    private Clock clock;
    private final static LocalDateTime LOCAL_DATE = LocalDateTime.of(2022, 11, 29, 10, 11);
    private Employee employee;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(RequestExceptionHandler.class);
    
    private static final MockMultipartFile FILE;
    
    private static final MockMultipartFile JSON_REQUEST;
    
    static {
        try {
            FILE = new MockMultipartFile(
                    "files",
                    "1111.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    RequestControllerImplTest.class.getClassLoader().getResourceAsStream("load/1111.jpg"));
            JSON_REQUEST = new MockMultipartFile(
                    "request",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    """
                    {
                    	"checks": [
                    		{
                    			"checkType": "BRAKE_SYSTEM",
                    			"status": "DECLINE"
                    		},
                    		{
                    			"checkType": "SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "STEERING",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "WHEELS_AND_TIRES",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "HORN",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "SATELLITE_NAVIGATION",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "BODY_LOCKS_FUEL_TANK_CAPS",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "DRIVER_SEAT_CUSHION_AND_BACKREST",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "WINDOW_HEATING_AND_DEFROSTER",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "TOW_HITCHES_AND_CABLES",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "SPARE_WHEEL_HOLDER",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "SEAT_BELTS",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "EXHAUST_SYSTEM",
                    			"status": "DONE"
                    		},
                    		{
                    			"checkType": "FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK",
                    			"status": "DONE"
                    		}
                    	]
                    }
                    """.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
    }
    
    @BeforeEach
    public void initMocks() {
        var fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var organization1 = createOrganization1();
        var department1 = createDepartment1(organization1, null);
        var position1 = createPosition1(organization1);
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(department1);
        positionRepository.saveAndFlush(position1);
        employee = createEmployee1(department1, position1);
        employeeRepository.saveAndFlush(employee);
    }
    
    
    @Test
    @DisplayName("Получение несуществующей заявки по идентификатору")
    void getNonExistentTest() throws Exception {
        mockMvc.perform(get(REQUEST_URI + "/" + UUID.randomUUID())
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Получение заявки по идентификатору")
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getTest() throws Exception {
        var response = mockMvc.perform(get(REQUEST_URI + "/d9d50a1e-1fc4-4458-ade6-f5297304a389")
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), ChecksTreeDto.class);
        assertFalse(actual.isCallTelemech());
        assertEquals(4, actual.getChecks().getPass().size());
        assertEquals(1, actual.getChecks().getFinished().size());
    }
    
    @Test
    @DisplayName("Создание заявки по идентификатору, уже есть активная заявка")
    void createEmptyHaveRequest() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        var createRequest = new CreateRequestDto(UUID.randomUUID());
        mockMvc.perform(post(REQUEST_URI + "/create")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .content(objectMapper.writeValueAsString(createRequest))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$").value("Найдена активная заявка"));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Найдена активная заявка", firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("Найдена активная заявка", secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 5);
    }
    
    @Test
    @DisplayName("Смена статуса заявки по идентификатору")
    void changeStatus() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        
        Mockito.reset(clock);
        var fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC).plusSeconds(30 * 60), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var response = mockMvc.perform(post(REQUEST_URI + "/" + request.getId() + "/status/" + RequestStatus.DONE)
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), RequestDto.class);
        assertEquals(RequestStatus.DONE, actual.requestStatus());
        var saved = requestRepository.findById(actual.id())
                                     .orElseThrow(() -> new RequestNotFoundException(actual.id()));
        assertEquals(RequestStatus.DONE, saved.getStatus());
        assertNotNull(saved.getChecksFinishedTime());
        assertEquals(LocalDateTime.now(fixedClock), saved.getChecksFinishedTime());
    }
    
    @Test
    @DisplayName("Получение проверки")
    void getCheck() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        var check = createCheck(request);
        request.getChecks().add(check);
        checkRepository.save(check);
        var response = mockMvc.perform(get(REQUEST_URI + "/" + request.getId() + "/" + check.getCheckType())
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), CheckDto[].class);
        assertEquals(check.getId(), actual[0].id());
        assertEquals(check.getCheckType(), actual[0].checkType());
        assertEquals(check.getAttempt(), actual[0].attempt());
        assertEquals(check.getCheckStatus(), actual[0].checkStatus());
        assertEquals(check.getCheckType().getOrdinal(), actual[0].ordinal());
    }
    
    @ParameterizedTest
    @MethodSource("checkSource")
    @Sql(value = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/transport.sql",
            "/scripts/request_with_checks.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql"
    })
    @SneakyThrows
    void vehicleNumberTest(
            CheckType checkType,
            UUID requestId,
            CheckStatus expectedCheckStatus,
            int expectedPhotoCount,
            int expectedAttempt,
            RequestBuilder request,
            List<ResultMatcher> matchers
                          ) {
        if (checkType == CheckType.VEHICLE_NUMBER) {
            var carNumber = new CarNumberDto("А777АА777", 111);
            var numbers = List.of(carNumber);
            var carPlateResponse = new CarPlateResponseDto(200, true, numbers);
            when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(ResponseEntity.ok(carPlateResponse));
        }
        if (checkType == CheckType.OIL_LEVEL) {
            var predictResponse = new PredictResponseDto(200, true, List.of("nothing"));
            when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(ResponseEntity.ok(predictResponse));
        }
        mockMvc.perform(request)
               .andExpectAll(matchers.toArray(ResultMatcher[]::new));
        
        var check = checkRepository.findByRequestIdAndCheckType(requestId, checkType)
                                   .orElseThrow(() -> new JUnitException("check not found"));
        assertThat(check.getAttempt()).isEqualTo(expectedAttempt);
        assertThat(check.getCheckStatus()).isEqualTo(expectedCheckStatus);
        assertThat(check.getPhotos()).hasSize(expectedPhotoCount);
    }
    
    private static Stream<Arguments> checkSource() {
        return Stream.of(
                Arguments.of(
                        CheckType.VEHICLE_NUMBER,
                        UUID.fromString("de30893f-9c08-4eea-a42e-db10b40b6f0e"),
                        CheckStatus.DONE,
                        1,
                        1,
                        multipart("/request/de30893f-9c08-4eea-a42e-db10b40b6f0e/VEHICLE_NUMBER/check")
                                .file(FILE)
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))),
                        List.of(
                                status().isOk(),
                                jsonPath("$.checkStatus").value("DONE")
                               )
                            ),
                Arguments.of(
                        CheckType.OIL_LEVEL,
                        UUID.fromString("1beb4fdb-0e49-68f2-c6d8-49870a2db0f3"),
                        CheckStatus.DONE,
                        1,
                        1,
                        multipart("/request/1beb4fdb-0e49-68f2-c6d8-49870a2db0f3/OIL_LEVEL/check")
                                .file(FILE)
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))),
                        List.of(
                                status().isOk(),
                                jsonPath("$.checkStatus").value("DONE")
                               )
                            ),
                Arguments.of(
                        CheckType.BODY_DAMAGE,
                        UUID.fromString("1beb4fdb-0e49-68f2-c6d8-49870a2db0f3"),
                        CheckStatus.DECLINE,
                        3,
                        1,
                        multipart("/request/1beb4fdb-0e49-68f2-c6d8-49870a2db0f3/BODY_DAMAGE/check")
                                .file(FILE)
                                .file(FILE)
                                .file(FILE)
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))),
                        List.of(
                                status().isOk(),
                                jsonPath("$.checkStatus").value("DECLINE")
                               )
                            ),
                Arguments.of(
                        CheckType.BODY_DAMAGE,
                        UUID.fromString("1beb4fdb-0e49-68f2-c6d8-49870a2db0f3"),
                        CheckStatus.DONE,
                        0,
                        1,
                        multipart("/request/1beb4fdb-0e49-68f2-c6d8-49870a2db0f3/BODY_DAMAGE/check")
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))),
                        List.of(
                                status().isOk(),
                                jsonPath("$.checkStatus").value("DONE")
                               )
                            ),
                Arguments.of(
                        CheckType.SAFETY,
                        UUID.fromString("1beb4fdb-0e49-68f2-c6d8-49870a2db0f3"),
                        CheckStatus.DECLINE,
                        0,
                        1,
                        multipart("/request/1beb4fdb-0e49-68f2-c6d8-49870a2db0f3/SAFETY/check")
                                .file(JSON_REQUEST)
                                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))),
                        List.of(
                                status().isOk(),
                                jsonPath("$.checkStatus").value("DECLINE")
                               )
                            )
                        );
    }
    
    @Test
    @DisplayName("Загрузка фото проверки без пройденной проверки номера автомобиля")
    void checkPhotoWithoutVehicleNumberCheck() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        var checkVehicleNumber = createCheck(request);
        request.getChecks().add(checkVehicleNumber);
        var check = createCheck(request);
        check.setCheckType(CheckType.HEADLAMPS_LF);
        request.getChecks().add(check);
        checkRepository.save(check);
        var file = new MockMultipartFile("file", "1111.jpg",
                                         MediaType.IMAGE_JPEG_VALUE,
                                         getClass().getClassLoader().getResourceAsStream("load/1111.jpg"));
        mockMvc.perform(multipart(REQUEST_URI + "/" + request.getId() + "/" + check.getCheckType() + "/check")
                                .file(file)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
               .andExpect(status().isPreconditionFailed());
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals("Проверка на автомобильный номер не в финальном статусе",
                     firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals("Проверка на автомобильный номер не в финальном статусе", secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 5);
    }
    
    @Test
    @DisplayName("Получение данных активной заявки пользователя")
    void getInProgress() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        var response = mockMvc.perform(get(REQUEST_URI + "/active")
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), ActiveResponse.class);
        assertEquals(request.getId(), actual.id());
    }
    
    @Test
    @DisplayName("Получение данных заявки пользователя в статусе На линии")
    @Sql(value = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/fleet_owner_organization.sql",
            "/scripts/organization_medical_license.sql",
            "/scripts/ewb_contract.sql",
            "/scripts/ewb_tariff.sql"
    })
    void getOnTheLine() throws Exception {
        var request = createRequest();
        request.setAuthor(new Employee().setId(UUID.fromString("c9e9192f-d2f3-4604-83f8-e863ce1bc192")))
               .setStatus(RequestStatus.ON_THE_LINE);
        requestRepository.save(request);
        var response = mockMvc.perform(get(REQUEST_URI + "/on-the-line")
                                               .with(jwt().jwt(builder -> builder.jti("c9e9192f-d2f3-4604-83f8-e863ce1bc192"))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8), RequestOnTheLineDto.class);
        assertEquals(request.getStatus(), actual.requestStatus());
        assertEquals(request.getId(), actual.requestId());
        assertFalse(actual.ewbPath());
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Закрытие заявки")
    void close() {
        var fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var request = createRequest();
        request.setStatus(RequestStatus.ON_THE_LINE);
        var savedRequest = requestRepository.save(request);
        mockMvc.perform(patch(REQUEST_URI + "/close/" + savedRequest.getId())
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        
        var actual = requestRepository.findById(savedRequest.getId())
                                      .orElseThrow(() -> new JUnitException("Request not found"));
        var actualHistory = requestHistoryRepository.findByRequestIdOrderByChangeTime(savedRequest.getId()).stream()
                                                    .min(Comparator.comparing(RequestHistory::getChangeTime))
                                                    .orElseThrow(() -> new JUnitException("RequestHistory not found"));
        
        assertEquals(RequestStatus.FINISHED, actual.getStatus());
        assertEquals(RequestStatus.FINISHED, actualHistory.getStatus());
        assertEquals(EMPLOYEE_1_ID, actualHistory.getInitiator().getId());
        assertEquals(LOCAL_DATE, actualHistory.getChangeTime());
    }
    
    @Test
    @DisplayName("Отмена заявки")
    void cancel() throws Exception {
        var request = createRequest();
        requestRepository.save(request);
        mockMvc.perform(patch(REQUEST_URI + "/" + request.getId() + "/cancel")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        var saved = requestRepository.findById(request.getId())
                                     .orElseThrow(() -> new RequestNotFoundException(request.getId()));
        assertEquals(RequestStatus.CANCELED, saved.getStatus());
    }
    
    @SneakyThrows
    @Test
    void getStatusHistory() {
        var request = createRequest();
        request.setStatus(RequestStatus.EXPIRED);
        requestRepository.save(request);
        var requestHistory1 = addRequestHistory(request.getId(), "В процессе проверки", RequestStatus.IN_PROGRESS);
        var requestHistory2 = addRequestHistory(request.getId(), "Проверки завершены успешно", RequestStatus.DONE);
        var requestHistory3 = addRequestHistory(request.getId(), "На линии", RequestStatus.ON_THE_LINE);
        var requestHistory4 = addRequestHistory(request.getId(),
                                                "Статус заявки изменен пользователем: " + employee.getFIO(),
                                                RequestStatus.EXPIRED);
        var histories = List.of(requestHistory4, requestHistory3, requestHistory2, requestHistory1);
        var expected = new LinkedList<RequestHistoryDto>();
        histories.forEach(requestHistory -> expected.add(0, requestHistoryElementToGetStatusDto(requestHistory)));
        expected.getLast().setType(RequestHistoryDto.TypeEnum.NOW);
        var response = mockMvc.perform(get(REQUEST_URI + "/" + request.getId() + "/status/history")
                                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                            new TypeReference<List<RequestHistoryDto>>() {
                                            });
        var actualRequest = requestRepository.findById(request.getId())
                                             .orElseThrow(() -> new EntityNotFoundException(Request.class, request.getId()));
        assertEquals(RequestStatus.EXPIRED, actualRequest.getStatus());
        assertEquals(expected, actual);
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void changeStatusForCallTelemechanic() {
        mockMvc.perform(patch(REQUEST_URI + "/d9d50a1e-1fc4-4458-ade6-f5297304a389/status")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
               .andExpect(status().isOk());
        
        var request = requestRepository.findById(UUID.fromString("d9d50a1e-1fc4-4458-ade6-f5297304a389"))
                                       .orElseThrow(() -> new JUnitException("request not found"));
        assertEquals(RequestStatus.WARNING, request.getStatus());
        assertNotNull(request.getChecksFinishedTime());
        var requestHistory = requestHistoryRepository.findByRequestIdOrderByChangeTime(UUID.fromString("d9d50a1e-1fc4-4458-ade6-f5297304a389"));
        assertEquals(1, requestHistory.size());
        assertEquals(RequestStatus.WARNING, requestHistory.get(0).getStatus());
    }
    
    private RequestHistoryDto requestHistoryElementToGetStatusDto(RequestHistory requestHistory) {
        return new RequestHistoryDto(requestHistory.getChangeTime(),
                                     requestHistory.getStatus().name(),
                                     RequestHistoryDto.TypeEnum.PAST,
                                     requestHistory.getInitiator().getFIO());
    }
    
    
    private RequestHistory addRequestHistory(UUID requestId, String comment, RequestStatus requestStatus) {
        var requestHistory = new RequestHistory()
                .setChangeTime(LOCAL_DATE)
                .setRequestId(requestId)
                .setComment(comment)
                .setStatus(requestStatus)
                .setInitiator(employee);
        requestHistoryRepository.save(requestHistory);
        return requestHistory;
    }
    
    private Request createRequest() {
        var transport = new Transport(
                UUID.randomUUID(), "А555АУ157", "audi", "Q8",
                100000, TransportStatus.IN_USE, "-", "-", 40,
                new HashSet<>(Set.of(ORGANIZATION_1)), null, null, null
        );
        transportRepository.saveAndFlush(transport);
        return Request.builder()
                      .author(employee)
                      .creationTime(LocalDateTime.now(clock))
                      .humanReadableId("OT-768")
                      .status(RequestStatus.IN_PROGRESS)
                      .transport(transport)
                      .organizationId(employee.getOrganization().getId())
                      .build();
    }
    
    private Check createCheck(Request request) {
        return Check.builder()
                    .checkType(CheckType.VEHICLE_NUMBER)
                    .checkStatus(CheckStatus.IN_PROGRESS)
                    .attempt(0)
                    .request(request)
                    .build();
    }
}