package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;

import java.time.format.DateTimeFormatter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.dto.FileData;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleResponse;
import ru.sber.transport.telemechanic.enumerate.*;
import ru.sber.transport.telemechanic.messaging.sender.message.OdometerHistoryValueMessage;
import ru.sber.transport.telemechanic.service.FileService;
import ru.sber.transport.telemechanic.service.SignatureVerifier;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.*;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;
import static ru.sber.transport.telemechanic.enumerate.Role.*;

@DisplayName("Проверка контроллера ЭПЛ")
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedPostgres
@EnableWireMock({
        @ConfigureWireMock(name = "ewb-korus-server", property = "korus.url")
})
class EwbControllerImplTest {
    public static final LocalDate currentDate = LocalDate.of(2023, 2, 10);
    public final Clock fixedClock = Clock.fixed(currentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                                ZoneId.of(ZoneOffset.UTC.getId()));
    
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private Clock clock;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EwbRepository ewbRepository;
    @Autowired
    private EwbTitleRepository ewbTitleRepository;
    @Autowired
    private EwbHistoryRepository ewbHistoryRepository;
    @Autowired
    private MedicRequestHistoryRepository medicRequestHistoryRepository;
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;
    @MockitoBean
    private FileService fileService;
    @InjectWireMock("ewb-korus-server")
    private WireMockServer ewbKorusWiremockServer;
    @MockitoBean
    private OutputBridge odometerValueOutput;
    @MockitoBean
    private SignatureVerifier signatureVerifier;
    @Captor
    private ArgumentCaptor<OdometerHistoryValueMessage> odometerHistoryValueMessageArgumentCaptor;
    
    private static final String FIRST_TITLE_REQUEST_2 = """
                                                        {
                                                            "ewbUuid": "%s",
                                                            "startDate": "%s",
                                                            "finishDate": "%s",
                                                            "transportationType": "СН",
                                                            "communicationType": "Г",
                                                            "tariffDepartmentId": "20d4a338-e121-4a0b-9d80-a7b6b035484f",
                                                            "transportId": "9b1d882c-f623-446b-af1e-ae9ff1966a0e",
                                                            "driverId": "167a0b4c-8324-44ba-9619-c0cd583fb1ca"
                                                        }
                                                        """;
    
    private static final String SECOND_TITLE_REQUEST =
            """
            {
            	"id": "95c2bd4f-3a79-4b30-8d7e-9308e751b40a",
            	"ewbId": "%s",
            	"ewbUuid": "%s",
            	"medic": {
            		"id": "95c2bd4f-3a79-4b30-8d7e-9308e751b40a",
            		"organizationName": "ЦА",
            		"position": "Не_ожидает_обновления",
            		"fullName": "Макаров Максим Дмитриевич",
            		"medicalLicenseId": "a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7",
            		"series": "SERIES-2",
            		"number": "1234",
            		"issueDate": "2024-07-31",
            		"expiryDate": "2025-07-31",
            		"decisionTime": 1676008494000
            	},
            	"driver": {
            		"id": "95c2bd4f-3a79-4b30-8d7e-9308e751b40a",
            		"lastName": "Макаров",
            		"firstName": "Максим",
            		"patronymic": "Дмитриевич",
            		"tin": 123456789098
            	},
            	"drivingLicense": {
            		"id": "c08ebefc-fd60-4fab-98be-333bc3bf1145",
            		"number": 12345,
            		"series": 12345678,
            		"issueDate": "2024-01-01"
            	},
            	"request": {
            	    "systPressure": 150,
            	    "dyastPressure": 70,
            	    "pulse": 120,
            	    "temperature": 39.6,
            	    "bloodAlcohol": 0.45,
            	    "comment": "done"
            	}
            }
            """;
    
    @BeforeEach
    void initMocks() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        AuthorizeUtils.authorize(manager, Role.ROLE_ADMIN_DATA_MASTER.name());
        var organization1 = createOrganization1();
        var department1 = createDepartment1(organization1, null);
        var position1 = createPosition1(organization1);
        var employee1 = createEmployee1(department1, position1);
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(department1);
        positionRepository.saveAndFlush(position1);
        employeeRepository.saveAndFlush(employee1);
    }
    
    @Test
    void authSuccess() {
        assertAuthRequest(status().isOk());
    }
    
    @Test
    void authUnauthorized() {
        ewbKorusWiremockServer.stubFor(WireMock.post(urlEqualTo("/v1/login"))
                                               .willReturn(aResponse()
                                                                   .withStatus(HttpStatus.UNAUTHORIZED.value())
                                                                   .withHeader("Content-Type", "application/json")
                                                                   .withBody("""
                                                                                 {
                                                                                     "login": "mock_login",
                                                                                     "password": "mock_password"
                                                                                 }
                                                                             """)));
        
        assertAuthRequest(status().isUnauthorized());
    }
    
    @Test
    void getUUID() {
        assertGetUUIDRequest(status().isOk());
    }
    
    @Test
    void getUUIDUnauthorized() {
        ewbKorusWiremockServer.stubFor(WireMock.post(urlEqualTo("/v1/client/gis/uuid/external"))
                                               .withHeader("Authorization", equalTo("some_correct_token"))
                                               .willReturn(aResponse()
                                                                   .withStatus(HttpStatus.UNAUTHORIZED.value())));
        
        assertGetUUIDRequest(status().isUnauthorized());
    }
    
    @Test
    void uuidIsNotAvailable() {
        ewbKorusWiremockServer.stubFor(WireMock.post(urlEqualTo("/v1/client/gis/uuid/external"))
                                               .withHeader("Authorization", equalTo("some_correct_token"))
                                               .willReturn(aResponse()
                                                                   .withStatus(HttpStatus.NO_CONTENT.value())));
        assertGetUUIDRequest(status().isConflict());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/ewb_title_integration_test.sql",
    })
    void generateFirstTitle() {
        var newUuid = UUID.fromString("C6CE9C3F-8FC4-46B3-AD52-7866BBD2F6EA");
        var titleForm = FIRST_TITLE_REQUEST_2.formatted(newUuid, LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1));
        
        assertThat(ewbRepository.findByEwbUuid(newUuid)).isEmpty();
        
        mockMvc.perform(post("/ewb/form-title/1")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(titleForm))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }
    
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/ewb_title_integration_test.sql",
    })
    @Test
    @SneakyThrows
    void ewbTileShouldBeSent() {
        var titleBytes =
                Base64.getEncoder().encode(
                        this.getClass().getClassLoader().getResource("ewb/titles/first/title_one_line.xml").openStream().readAllBytes()
                                          );
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        var content = """
                      {
                        "content": "%s",
                        "fileName": "file",
                        "firstTitleForm": %s,
                        "humanReadableId": "PL-0000-00000001",
                        "creationTime": "2024-11-14T18:40:00",
                        "ewbUuid": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                        "titleType": "FIRST",
                        "signature": "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"
                      }
                      """.formatted(new String(titleBytes).replaceAll("\"", "\\\\\""),
                                    FIRST_TITLE_REQUEST_2.formatted(UUID.randomUUID(), LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1)));
        
        mockMvc.perform(post("/ewb/title/send")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content))
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findAll().stream()
                               .filter(el -> el.getHumanReadableId().equals("PL-0000-00000001"))
                               .findAny()
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertNotNull(ewb);
        assertNotNull(ewb.getCreationTime());
        assertEquals(LocalDateTime.of(2024, 11, 14, 18, 40, 0), ewb.getCreationTime());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/ewb_title_integration_test.sql"
    })
    void generateSecondTitle() {
        var ewbId = UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f");
        var titleForm = SECOND_TITLE_REQUEST.formatted(ewbId, ewbId);
        doReturn(new FileData("content", "signature".getBytes())).when(fileService).get(anyString());
        mockMvc.perform(post("/ewb/form-title/2")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(titleForm))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        var ewb = ewbRepository.findById(ewbId)
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertNotNull(ewb);
        assertEquals(UUID.fromString("40b80a64-a191-46c6-bbbb-16a60e772f7f"), ewb.getMedic().getId());
        assertEquals(150, ewb.getMedicRequest().getSystPressure());
        assertEquals(70, ewb.getMedicRequest().getDyastPressure());
        assertEquals(120, ewb.getMedicRequest().getPulse());
        assertEquals(BigDecimal.valueOf(39.6), ewb.getMedicRequest().getTemperature());
        assertEquals(BigDecimal.valueOf(0.45), ewb.getMedicRequest().getBloodAlcohol());
        assertEquals("done", ewb.getMedicRequest().getComment());
        assertEquals(UUID.fromString("d8686e9a-a50c-4c6f-a581-e01ef501e983"), ewb.getOrganizationMedicalLicenseId());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void searchSelfOrganization() {
        var request = new EwbSearchSelfOrganizationRequestDto("A77", "A77",
                                                              Set.of(EwbStatus.ON_THE_LINE, EwbStatus.IN_GARAGE),
                                                              new DateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now()),
                                                              new DateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now()),
                                                              new PageSettingDto(0, 20));
        mockMvc.perform(
                       post("/ewb/search/self-organization")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());
        
        request = new EwbSearchSelfOrganizationRequestDto(null, null, null,
                                                          null, null, null);
        
        mockMvc.perform(
                       post("/ewb/search/self-organization")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void searchAllOrganizations() {
        var request = new EwbSearchAllOrganizationsRequestDto("A77", "A77", UUID.randomUUID(),
                                                              Set.of(EwbStatus.ON_THE_LINE, EwbStatus.IN_GARAGE),
                                                              new DateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now()),
                                                              new DateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now()),
                                                              new PageSettingDto(0, 20));
        mockMvc.perform(
                       post("/ewb/search/all-organizations")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());
        
        request = new EwbSearchAllOrganizationsRequestDto(null, null, null,
                                                          null, null, null, null);
        
        mockMvc.perform(
                       post("/ewb/search/all-organizations")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void searchAllOrganizationsByContractorIdsAndAutoparkIds() {
        var request = new EwbSearchDto(null, null, null,
                                       null,
                                       null,
                                       List.of(UUID.fromString("50683ed0-4afc-4a13-b5c7-5064220b9517")),
                                       List.of(UUID.fromString("06d789cf-fac8-46ae-889b-3e15e111c757")),
                                       new PageSettingDto(0, 20));
        mockMvc.perform(
                       post("/ewb/search")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.content", hasSize(5)))
               .andExpect(jsonPath("$.content[0].id", in(
                       List.of("140e4734-4909-4bf3-b98f-f24c90d8005f", "d87dfe9a-5915-4d37-855f-a1598d4819c3",
                               "660184ec-a2be-4340-b482-cb57082567a8", "a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7"))));
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getEwb() {
        var ewb = ewbRepository.findById(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                .orElseThrow(() -> new JUnitException("Ewb not found"));
        mockMvc.perform(
                       get("/ewb/140e4734-4909-4bf3-b98f-f24c90d8005f")
                               .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("140e4734-4909-4bf3-b98f-f24c90d8005f"))
               .andExpect(jsonPath("$.medic").doesNotExist())
               .andExpect(jsonPath("$.telemechOut").exists())
               .andExpect(jsonPath("$.telemechIn").doesNotExist())
               .andExpect(jsonPath("$.transport.odometerOut").value(10000))
               .andExpect(jsonPath("$.transport.fuelLitreageOut").value(30))
               .andExpect(jsonPath("$.transport.fuelTankVolume").value(40))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getTelemechDecisionOut().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.transportationType").value("OWN_ACCOUNT_TRANSPORTATION"))
               .andExpect(jsonPath("$.transportationSubtype").value("REGULAR_PASSENGER_TRANSPORTATION"))
               .andExpect(jsonPath("$.communicationType").value("URBAN"));
        
        ewb = ewbRepository.findById(UUID.fromString("d87dfe9a-5915-4d37-855f-a1598d4819c3"))
                               .orElseThrow(() -> new JUnitException("Ewb not found"));
        mockMvc.perform(
                       get("/ewb/d87dfe9a-5915-4d37-855f-a1598d4819c3")
                               .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("d87dfe9a-5915-4d37-855f-a1598d4819c3"))
               .andExpect(jsonPath("$.medic").exists())
               .andExpect(jsonPath("$.telemechOut").exists())
               .andExpect(jsonPath("$.telemechIn").exists())
               .andExpect(jsonPath("$.transport.odometerOut").value(1200))
               .andExpect(jsonPath("$.transport.fuelLitreageOut").doesNotExist())
               .andExpect(jsonPath("$.transport.fuelTankVolume").value(40))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getTelemechDecisionOut().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getTelemechDecisionIn().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getMedicDecisionTime().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.transportationType").value("COMMERCIAL_TRANSPORTATION"))
               .andExpect(jsonPath("$.transportationSubtype").value("PASSENGER_TAXI_TRANSPORTATION"))
               .andExpect(jsonPath("$.communicationType").value("SUBURBAN"));
        
        ewb = ewbRepository.findById(UUID.fromString("d87dfe9a-5915-4d37-855f-a1598d4819c3"))
                           .orElseThrow(() -> new JUnitException("Ewb not found"));
        mockMvc.perform(
                       get("/ewb/660184ec-a2be-4340-b482-cb57082567a8")
                               .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value("660184ec-a2be-4340-b482-cb57082567a8"))
               .andExpect(jsonPath("$.medic").exists())
               .andExpect(jsonPath("$.telemechOut").exists())
               .andExpect(jsonPath("$.telemechIn").exists())
               .andExpect(jsonPath("$.transport.odometerOut").value(1200))
               .andExpect(jsonPath("$.transport.fuelLitreageOut").doesNotExist())
               .andExpect(jsonPath("$.transport.fuelTankVolume").value(40))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getTelemechDecisionOut().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getTelemechDecisionIn().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.telemechOut.decisionTime").value(
                       ewb.getMedicDecisionTime().plusHours(3).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
               .andExpect(jsonPath("$.transportationType").value("COMMERCIAL_TRANSPORTATION"))
               .andExpect(jsonPath("$.transportationSubtype").value("ON_DEMAND_PASSENGER_TRANSPORTATION"))
               .andExpect(jsonPath("$.communicationType").value("INTERCITY"));
    }
    
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @Test
    @SneakyThrows
    void addOdometerValue() {
        doReturn(CompletableFuture.completedFuture(Void.class)).when(odometerValueOutput).send(odometerHistoryValueMessageArgumentCaptor.capture());
        mockMvc.perform(post("/ewb/odometer-out")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                             {
                                                 "id": "a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7",
                                                 "value": 1020
                                             }
                                         """)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertThat(ewb)
                .extracting(Ewb::getOdometerOut, Ewb::getOdometerIn)
                .containsExactly(1020, null);
        
        var odometerCheck = ewb.getRequest().getChecks().stream()
                               .filter(check -> check.getCheckType().equals(CheckType.ODOMETER))
                               .findAny()
                               .orElseThrow(() -> new JUnitException("odometer check not exists"));
        assertThat(odometerCheck.getCheckStatus()).isEqualTo(CheckStatus.DONE);
        
    }
    
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @Test
    @SneakyThrows
    void closeEwb() {
        doReturn(CompletableFuture.completedFuture(Void.class)).when(odometerValueOutput).send(odometerHistoryValueMessageArgumentCaptor.capture());
        mockMvc.perform(post("/ewb/close")
                                .with(jwt().jwt(builder -> builder.jti("6deae338-2402-4a04-a85f-e4f242eb357e"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                             {
                                                 "id": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                                                 "value": 10200,
                                                 "fuelLitreage": 25
                                             }
                                         """)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        var request = ewb.getRequest();
        assertThat(ewb)
                .extracting(Ewb::getOdometerOut,
                            Ewb::getOdometerIn,
                            Ewb::getFuelLitreageOut,
                            Ewb::getFuelLitreageIn,
                            Ewb::getStatus)
                .containsExactly(10000, 10200, 30, 25, EwbStatus.IN_GARAGE);
        assertThat(ewb.getTransport().getFuelLitreage()).isEqualTo(25);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.IN_GARAGE);
    }
    
    @SneakyThrows
    void assertAuthRequest(ResultMatcher status) {
        mockMvc.perform(
                       post("/ewb/auth")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status);
    }
    
    @SneakyThrows
    private void assertGetUUIDRequest(ResultMatcher status) {
        mockMvc.perform(
                       post("/ewb/uuid?token=token")
                               .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name()))))
               .andExpect(status);
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/ewb_title_integration_test.sql"
    })
    @DisplayName("Формирование третьего титула ЭПЛ")
    void shouldGenerateThirdTitleTest() {
        doReturn(new FileData("content", "signature".getBytes())).when(fileService).get(anyString());
        var response = mockMvc.perform(post("/ewb/form-title/telemech-out/" + EwbTitleType.THIRD)
                                               .with(jwt().jwt(builder -> builder.jti("6deae338-2402-4a04-a85f-e4f242eb357e"))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .content("""
                                                            {
                                                             	"requestId": "d9d51a1e-1fc4-4458-ade6-f5297304a389",
                                                             	"decisionTime": "2023-02-10T00:00:00"
                                                             }
                                                        """)
                                      )
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        var result = objectMapper.readValue(response, TelemechOutTitleResponse.class);
        assertThat(result).isNotNull();
        assertThat(result.creationTime()).isNotNull();
        var ewb = ewbRepository.findByRequestId(UUID.fromString("d9d51a1e-1fc4-4458-ade6-f5297304a389"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertEquals(LocalDateTime.of(2023, 2, 10, 0, 0, 0), ewb.getTelemechDecisionOut());
        assertEquals(UUID.fromString("6deae338-2402-4a04-a85f-e4f242eb357e"), ewb.getTelemechOut().getId());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @DisplayName("Отправка и сохранение третьего титула ЭПЛ")
    void sendAndSaveThirdTitleTest() {
        var titleBytes =
                Base64.getEncoder().encode(
                        this.getClass().getClassLoader().getResource("ewb/titles/first/title_one_line.xml").openStream().readAllBytes()
                                          );
        var creationTime = "2024-11-14T18:40:00";
        var content = """
                      {
                        "ewbId": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                        "file": "%s",
                        "fileName": "file",
                        "creationTime": "%s",
                        "signature": "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"
                      }
                      """.formatted(new String(titleBytes).replaceAll("\"", "\\\\\""), creationTime);
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        mockMvc.perform(post("/ewb/title/send/telemech-out/" + EwbTitleType.THIRD)
                                .with(jwt().jwt(builder -> builder.jti("6deae338-2402-4a04-a85f-e4f242eb357e"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                       )
               .andExpect(status().isOk());
        
        var ewbTitle = ewbTitleRepository.findByEwbIdAndType(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"), EwbTitleType.THIRD)
                                         .orElseThrow(() -> new JUnitException("ewb title not found"));
        
        assertEquals(EwbTitleType.THIRD, ewbTitle.getType());
        assertEquals(LocalDateTime.parse(creationTime), ewbTitle.getCreatedAt());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql",
            "/scripts/ewb_title_integration_test.sql"
    })
    @DisplayName("Формирование пятого титула ЭПЛ")
    void shouldGenerateFifthTitleTest() {
        doReturn(new FileData("content", "signature".getBytes())).when(fileService).get(anyString());
        mockMvc.perform(post("/ewb/form-title/5")
                                .with(jwt().jwt(builder -> builder.jti("6deae338-2402-4a04-a85f-e4f242eb357e"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                             {
                                              	"id": "d87dfe9a-5915-4d37-855f-a1598d4819c3",
                                              	"decisionTime": "2024-07-31T00:00:00"
                                              }
                                         """)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("d87dfe9a-5915-4d37-855f-a1598d4819c3"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertEquals(LocalDateTime.of(2024, 7, 31, 0, 0, 0), ewb.getTelemechDecisionIn());
        assertEquals(UUID.fromString("6deae338-2402-4a04-a85f-e4f242eb357e"), ewb.getTelemechIn().getId());
    }
    
    @Test
    @SneakyThrows
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @DisplayName("Отправка и сохранение пятого титула ЭПЛ")
    void sendAndSaveFifthTitleTest() {
        var titleBytes =
                Base64.getEncoder().encode(
                        this.getClass().getClassLoader().getResource("ewb/titles/first/title_one_line.xml").openStream().readAllBytes()
                                          );
        var content = """
                      {
                        "ewbId": "140e4734-4909-4bf3-b98f-f24c90d8005f",
                        "file": "%s",
                        "fileName": "file",
                        "signature": "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"
                      }
                      """.formatted(new String(titleBytes).replaceAll("\"", "\\\\\""));
        doNothing().when(signatureVerifier).verify(any(), anyString(), anyString());
        mockMvc.perform(post("/ewb/title/send/5")
                                .with(jwt().jwt(builder -> builder.jti("6deae338-2402-4a04-a85f-e4f242eb357e"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertEquals(EwbStatus.EWB_CLOSED, ewb.getStatus());
        assertEquals(RequestStatus.FINISHED, ewb.getRequest().getStatus());
        var ewbTitle = ewbTitleRepository.findByEwbIdAndType(UUID.fromString("140e4734-4909-4bf3-b98f-f24c90d8005f"), EwbTitleType.FIFTH)
                                         .orElseThrow(() -> new JUnitException("ewb title not found"));
        assertEquals(EwbTitleType.FIFTH, ewbTitle.getType());
        assertEquals(LocalDateTime.of(2024, 7, 31, 10, 11, 39), ewbTitle.getCreatedAt());
    }
    
    @Test
    @DisplayName("Получение реестра для всех организаций")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @SneakyThrows
    void getRegistryForAllOrganizations() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var ewbs = ewbRepository.findAll();
        mockMvc.perform(post("/ewb/report/all-organizations")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                            "fieldSet": [
                                            	"EWB_HUMAN_READABLE_ID",
                                            	"EWB_STATUS",
                                            	"EWB_CREATION_TIME",
                                            	"EWB_TELEMECH_DECISION_OUT_TIME",
                                            	"EWB_TELEMECH_DECISION_IN_TIME",
                                            	"DRIVER_FULL_NAME",
                                            	"DRIVER_ORGANIZATION_NAME",
                                            	"DRIVER_DEPARTMENT_NAME",
                                            	"TRANSPORT_STATE_NUMBER"
                                            ],
                                         	"humanReadableId": "EWB_ID"
                                         }
                                         """)
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(ewbs.size()));
    }
    
    @Test
    @DisplayName("Получение реестра для определенной организации")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @SneakyThrows
    void getRegistryForSelfOrganization() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ENGINEER_CORP_CLIENT.name());
        var ewbs = ewbRepository.findAll();
        mockMvc.perform(post("/ewb/report/self-organization")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                            "fieldSet": [
                                            	"EWB_HUMAN_READABLE_ID",
                                            	"EWB_STATUS",
                                            	"EWB_CREATION_TIME",
                                            	"EWB_TELEMECH_DECISION_OUT_TIME",
                                            	"EWB_TELEMECH_DECISION_IN_TIME",
                                            	"DRIVER_FULL_NAME",
                                            	"DRIVER_ORGANIZATION_NAME",
                                            	"DRIVER_DEPARTMENT_NAME",
                                            	"TRANSPORT_STATE_NUMBER"
                                            ]
                                         }
                                         """)
                       )
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalElements").value(ewbs.size()));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Получение заявки ЭПЛ")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getEwbRequest() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        
        var newCurrentDate = LocalDate.of(2024, 7, 31);
        var newFixedClock = Clock.fixed(newCurrentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                        ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(newFixedClock.instant()).when(clock).instant();
        doReturn(newFixedClock.getZone()).when(clock).getZone();
        
        var response = mockMvc.perform(get("/ewb/request")
                                               .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        
        var actual = objectMapper.readValue(response, GetEwbRequestDto.class);
        var expected = ewbRepository.findAll().stream()
                                    .filter(ewb -> ewb.getDriver().getId().equals(UUID.fromString("167a0b4c-8324-44ba-9619-c0cd583fb1ca")))
                                    .filter(ewb -> ewb.getStartDate().equals(LocalDate.now(clock)))
                                    .filter(ewb -> Set.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS,
                                                          MEDIC_DECLINED, TELEMECH_DECLINED, KORUS_DECLINED)
                                                      .contains(ewb.getStatus()))
                                    .max(Comparator.comparing(Ewb::getCreationTime))
                                    .orElseThrow(() -> new JUnitException("ewb not found"));
        
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getId(), actual.id());
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Получение заявки ЭПЛ с разными статусами проверок")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getEwbRequestWithChecks() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        
        var newCurrentDate = LocalDate.now();
        var newFixedClock = Clock.fixed(newCurrentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                        ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(newFixedClock.instant()).when(clock).instant();
        doReturn(newFixedClock.getZone()).when(clock).getZone();
        
        var response = mockMvc.perform(get("/ewb/request")
                                               .with(jwt().jwt(builder -> builder.jti("019ff061-c0dd-708a-ac10-787b06956003"))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
                              .andExpect(status().isOk())
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        
        var actual = objectMapper.readValue(response, GetEwbRequestDto.class);
        var checks = actual.telemechanic().checks();
        checks.getFinished().forEach(check -> {
            switch (check.getCheckStatus()) {
                case CheckStatus.DONE -> assertNull(check.getComment());
                case CheckStatus.DECLINE -> assertEquals("нечеткое фото", check.getComment());
            }
        });
        checks.getPass().forEach(check -> {
            if (check.getCheckStatus() == CheckStatus.IN_PROGRESS) {
                assertNull(check.getComment());
            }
        });
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Получение заявки ЭПЛ")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void getEwbDetailed() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        mockMvc.perform(get("/ewb/detailed")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name()))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.humanReadableId").value("EWB_ID1"))
               .andExpect(jsonPath("$.status").value(ON_THE_LINE.getRusName()));
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Отмена ЭПЛ")
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    void cancelEwb() {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ENGINEER_CORP_CLIENT.name());
        
        var newCurrentDate = LocalDate.of(2024, 7, 31);
        var newFixedClock = Clock.fixed(newCurrentDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                        ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(newFixedClock.instant()).when(clock).instant();
        doReturn(newFixedClock.getZone()).when(clock).getZone();
        
        mockMvc.perform(patch("/ewb/a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7/cancel")
                                .with(jwt().jwt(builder -> builder.jti("95c2bd4f-3a79-4b30-8d7e-9308e751b40a"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ENGINEER_CORP_CLIENT.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                         {
                                         "comment": "Комментарий отмены ЭПЛ"
                                         }
                                         """))
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        
        assertThat(ewb.getStatus()).isEqualTo(EWB_CANCELLED);
        assertThat(ewb.getRequest().getStatus()).isEqualTo(RequestStatus.EXPIRED);
        assertThat(ewb.getMedicRequest().getStatus()).isEqualTo(TelemedicineStatus.EXPIRED);
        
        assertThat(requestHistoryRepository.count()).isEqualTo(1);
        var ewbh = ewbHistoryRepository.findAll();
        assertThat(ewbh).hasSize(1);
        assertThat(ewbh.getFirst().getOldStatus()).isEqualTo(TELEMECH_IN_PROGRESS);
        assertThat(ewbh.getFirst().getStatus()).isEqualTo(EWB_CANCELLED);
        assertThat(ewbh.getFirst().getComment()).isEqualTo("Комментарий отмены ЭПЛ");
        assertThat(ewbh.getFirst().getEwbId()).isEqualTo(ewb.getId());
        var medicRequestHistory = medicRequestHistoryRepository.findAll();
        assertThat(medicRequestHistory).hasSize(1);
        assertThat(medicRequestHistory.getFirst().getOldStatus()).isEqualTo(TelemedicineStatus.DECLINED);
        assertThat(medicRequestHistory.getFirst().getStatus()).isEqualTo(TelemedicineStatus.EXPIRED);
        var requestHistory = requestHistoryRepository.findAll();
        assertThat(requestHistory).hasSize(1);
        assertThat(requestHistory.getFirst().getOldStatus()).isEqualTo(RequestStatus.IN_PROGRESS);
        assertThat(requestHistory.getFirst().getStatus()).isEqualTo(RequestStatus.EXPIRED);
    }
    
    static Stream<Arguments> addLitreageValue() {
        return Stream.of(
                Arguments.of(40),
                Arguments.of(0)
                        );
    }
    
    @Sql(scripts = {
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/ewb_integration_test.sql"
    })
    @SneakyThrows
    @MethodSource
    @ParameterizedTest
    void addLitreageValue(int value) {
        Mockito.<AuthorizationManager<?>>reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_DRIVER.name());
        mockMvc.perform(post("/ewb/litreage-out")
                                .with(jwt().jwt(builder -> builder.jti("40b80a64-a191-46c6-bbbb-16a60e772f7f"))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DRIVER.name())))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                             {
                                                 "id": "a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7",
                                                 "value": %d
                                             }
                                         """.formatted(value))
                       )
               .andExpect(status().isOk());
        
        var ewb = ewbRepository.findById(UUID.fromString("a4f3e1c8-fd4b-4088-9c2d-9d36490e08a7"))
                               .orElseThrow(() -> new JUnitException("ewb not found"));
        assertThat(ewb)
                .extracting(Ewb::getFuelLitreageOut, Ewb::getFuelLitreageIn)
                .containsExactly(value, null);
        
        var litreageCheck = ewb.getRequest().getChecks().stream()
                               .filter(check -> check.getCheckType().equals(CheckType.LITREAGE))
                               .findAny()
                               .orElseThrow(() -> new JUnitException("litreage check not exists"));
        assertThat(litreageCheck.getCheckStatus()).isEqualTo(CheckStatus.DONE);
    }
}
