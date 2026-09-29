package ru.sberbank.transport.oto.cargo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.enums.RequestTypeEnum;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.SharedTestData;
import ru.sberbank.transport.oto.cargo.database.dao.*;
import ru.sberbank.transport.oto.cargo.database.model.*;
import ru.sberbank.transport.oto.cargo.dto.ContractorDTO;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_cargo_oto")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера отчетов по грузам")
@EmbeddedPostgres()
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
class OtoRequestsControllerCargoTest extends SharedTestData {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @Autowired
    private OrganizationGroupRepository organizationGroupRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private final UUID organizationId = UUID.fromString(ORGANIZATION_ID);
    private final UUID organizationId2 = UUID.fromString(ORGANIZATION2_ID);
    private final UUID organizationId3 = UUID.fromString(ORGANIZATION3_ID);

    private final UUID organizationGroupId1 = UUID.fromString(ORGANIZATION_GROUP_ID);
    private final UUID organizationGroupId2 = UUID.fromString(ORGANIZATION_GROUP2_ID);

    @AfterEach
    void tearDown() {
        requestRepository.deleteAll();
        contractorRepository.deleteAll();
        employeeRepository.deleteAll();
        positionRepository.deleteAll();
        departmentRepository.deleteAll();
        routeRepository.deleteAll();
    }

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(roleCheckService);
        var organizationId = UUID.fromString(ORGANIZATION_ID);
        buildEngineerEnv();
        var transportTypes = new ArrayList<>(List.of(TransportTypeEnum.DEDICATED, TransportTypeEnum.COURIER, TransportTypeEnum.INTERREGIONAL,
                TransportTypeEnum.DOMESTIC_COURIER));
        var orgs = organizationRepository.findAll();
        for (var o = 0; o < orgs.size(); o++) {
            for (var i = o * 10; i < (o + 1) * 10; i++) {
                Collections.shuffle(transportTypes);
                generateAndSave(i, orgs.get(o), transportTypes.getFirst());
            }
        }
        generateAndSave(31, orgs.stream().filter(org -> org.getId().equals(organizationId)).findFirst()
                .orElseThrow(() -> new JUnitException("organization not found")), TransportTypeEnum.PUBLIC);
    }

    @Test
    @DisplayName("Получение всех для инженера орг1 без ролей")
    void test_add1() {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending()).stream()
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s ==
                                TransportTypeEnum.valueOf(request.getTransportType())))
                .filter(request -> request.getOrganization().getId().equals(organizationId))
                .toList();
        checkOtoCargoRequest(expectedList,
                10,
                String.format("""
                        {"organizationId": "%s"}
                        """, ORGANIZATION_ID),
                "ROLE_GUEST",
                USER_ENGINEER_ID);
    }

    @Test
    @DisplayName("Получение инженером орг1 заявок из орг2")
    @SneakyThrows
    void test_add1_2() {
        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s"}
                                """, ORGANIZATION3_ID))
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                        )
                )
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("Получение всех для инженера орг1 с ролью ограниченного доступа")
    void test_add2() {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending()).stream()
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s ==
                                TransportTypeEnum.valueOf(request.getTransportType())))
                .filter(request -> request.getOrganization().getId().equals(organizationId))
                .toList();

        checkOtoCargoRequest(expectedList,
                10,
                String.format("""
                        {"organizationId": "%s"}
                        """, ORGANIZATION_ID),
                "ROLE_DISPATCHER_SUPPORT_SERVICE",
                USER_ENGINEER_ID);
    }

    @Test
    @DisplayName("Получение всех для инженера орг1 с ролью ограниченного доступа")
    void test_add2_2() {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending()).stream()
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s ==
                                TransportTypeEnum.valueOf(request.getTransportType())))
                .filter(request -> request.getOrganization().getId().equals(organizationId2))
                .toList();

        checkOtoCargoRequest(expectedList,
                10,
                String.format("""
                        {"organizationId": "%s"}
                        """, ORGANIZATION2_ID),
                "ROLE_DISPATCHER_SUPPORT_SERVICE",
                USER_ENGINEER_ID);
    }

    @Test
    @DisplayName("Получение всех для администратора для поизвольной организации")
    void test_add3() {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending()).stream()
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s ==
                                TransportTypeEnum.valueOf(request.getTransportType())))
                .filter(request -> request.getOrganization().getId().equals(organizationId3))
                .toList();

        checkOtoCargoRequest(expectedList,
                10,
                String.format("""
                        {"organizationId": "%s"}
                        """, ORGANIZATION3_ID),
                "ROLE_ADMIN_DATA_MASTER",
                USER_ENGINEER_ID);
    }

    @Test
    @DisplayName("Ошибка поиска при запросе ИКК произвольной организации")
    @SneakyThrows
    void test_add5() {
        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s"}
                                """, ORGANIZATION3_ID))
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                        )
                )
                .andExpect(status().isForbidden());
    }

    private static Stream<Arguments> provideTestAddFilterData() {
        return Stream.of(
                Arguments.of("HRI01",
                        String.format("""
                                {"organizationId": "%s",
                                "senderAddress": "AddressString001"}
                                """, ORGANIZATION2_ID),
                        10),
                Arguments.of("HRI01",
                        String.format("""
                                {"organizationId": "%s",
                                "senderAddress": "AddressString001",
                                "sortField": "sender",
                                "sortDirection": "DESC"}
                                """, ORGANIZATION2_ID),
                        10),
                Arguments.of("HRI00",
                        String.format("""
                                {"organizationId": "%s",
                                "id": "HRI00"}
                                """, ORGANIZATION_ID),
                        10),
                Arguments.of("HRI01",
                        String.format("""
                                {"organizationId": "%s",
                                "recipientAddress": "AddressString10"}
                                """, ORGANIZATION2_ID),
                        10),
                Arguments.of("HRI010",
                        String.format("""
                                {"organizationId": "%s",
                                "recipientName": "LastName010 FirstName010 Patronymic010"}
                                """, ORGANIZATION_ID),
                        0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestAddFilterData")
    @DisplayName("Различные фильтры")
    void test_add_Filter(String humanReadableId, String requestBody, int expected) {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending()).stream()
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s ==
                                TransportTypeEnum.valueOf(request.getTransportType())))
                .filter(request -> request.getHumanReadableId().startsWith(humanReadableId))
                .toList();

        checkOtoCargoRequest(expectedList,
                expected,
                requestBody,
                "ROLE_DISPATCHER_SUPPORT_SERVICE",
                USER_ENGINEER_ID);
    }

    @Disabled("Нестабильный тест, работает через раз")
    @ParameterizedTest
    @ValueSource(strings = {"DEDICATED", "COURIER", "INTERREGIONAL", "DOMESTIC_COURIER"})
    @DisplayName("Фильтры по типу транспорта")
    @WithMockUser(username = USER_ENGINEER_ID, roles = "GUEST")
    void test_add_FilterTransportType(String transportType) throws Exception {
        int expected = (int) requestRepository.findAll().stream()
                .filter(r -> r.getTransportType().equals(transportType))
                .filter(r -> r.getOrganization().getId().equals(organizationId))
                .count();

        ResultActions perform = mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(String.format("""
                        {"organizationId": "%s",
                        "transportType": "%s"}
                        """, ORGANIZATION_ID, transportType))
                .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE));
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(expected))
                .andExpect(jsonPath("$.content[*].contractor").isNotEmpty());

    }

    @Test
    @DisplayName("Получение всех. Фильтр по статусам. 1 статус")
    @SneakyThrows
    void test_add_statusFilter_one() {
        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "statuses": ["%s"]}
                                """, ORGANIZATION_ID, TripRequestStatus.CARGO_APPROVED))
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Получение всех. Фильтр по типу заявок. 1 статус")
    @SneakyThrows
    void test_add_requestTypeFilter() {

        var requests = requestRepository.findAll();// [0][1] REGULAR

        requests.get(2).setTemplate(false);// SINGLE
        requests.get(3).setTemplate(false);// SINGLE

        requests.get(4).setTemplate(false);
        requests.get(4).setRequestType(RequestTypeEnum.SINGLE.name()); // SINGLE

        requests.get(5).setRequestType(RequestTypeEnum.REGULAR.name());// REGULAR
        requests.get(6).setRequestType(RequestTypeEnum.REGULAR.name());// REGULAR

        requests.get(7).setTemplate(false);
        requests.get(7).setRequestType(RequestTypeEnum.RELOCATION.name());// RELOCATION
        requests.get(8).setTemplate(false);
        requests.get(8).setRequestType(RequestTypeEnum.RELOCATION.name());// RELOCATION
        requests.get(9).setTemplate(false);
        requests.get(9).setRequestType(RequestTypeEnum.RELOCATION.name());// RELOCATION

        requestRepository.saveAllAndFlush(requests);

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "requestTypes": ["%s"]}
                                """, ORGANIZATION_ID, RequestTypeEnum.SINGLE.name()))
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3));

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "requestTypes": ["%s"]}
                                """, ORGANIZATION_ID, RequestTypeEnum.REGULAR.name()))
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4));

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "requestTypes": ["%s","%s"]}
                                """, ORGANIZATION_ID, RequestTypeEnum.SINGLE.name(), RequestTypeEnum.REGULAR.name()))
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(7));

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "requestTypes": ["%s"]}
                                """, ORGANIZATION_ID, RequestTypeEnum.RELOCATION.name()))
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].requestType").value("RELOCATION"));

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s"}
                                """, ORGANIZATION_ID))
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER_ENGINEER_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10));

    }

    private static Stream<Arguments> provideTestSearchData() {
        return Stream.of(
                Arguments.of(10, 20, """
                        {"creationTimeFrom": "%s","creationTimeTo":"%s", "organizationId": "%s"}
                        """),
                Arguments.of(110, 120, """
                        {"desiredTimeFrom": "%s","desiredTimeTo": "%s", "organizationId": "%s"}
                        """),
                Arguments.of(20, 30, """
                        {"approvalTimeFrom": "%s","approvalTimeTo": "%s", "organizationId": "%s"}
                        """),
                Arguments.of(30, 40, """
                        {"transferTimeFrom": "%s", "transferTimeTo": "%s", "organizationId": "%s"}
                        """),
                Arguments.of(40, 50, """
                        {"shipmentTimeFrom": "%s","shipmentTimeTo": "%s", "organizationId": "%s"}
                        """)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestSearchData")
    @DisplayName("Фильтр по дате создания")
    void test_search(int fromPlusDays, int toPlusDays, String requestBody) {
        var from = ZonedDateTime.now().plusDays(fromPlusDays).format(DateTimeFormatter.ISO_DATE_TIME)
                .replace("[Europe/Moscow]", "");
        var to = ZonedDateTime.now().plusDays(toPlusDays).format(DateTimeFormatter.ISO_DATE_TIME)
                .replace("[Europe/Moscow]", "");

        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending())
                .stream().
                filter(request -> request.getHumanReadableId().
                        startsWith("HRI01"))
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION)
                        .stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(
                                request.getTransportType())))
                .toList();

        checkOtoCargoRequest(expectedList,
                10,
                String.format(requestBody, from, to, ORGANIZATION2_ID),
                "ROLE_DISPATCHER_SUPPORT_SERVICE",
                USER_ENGINEER_ID);
    }

    @DisplayName("Фильтр по Human Readable ID заявки")
    @Test
    void test_search_byId() {
        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending())
                .stream().filter(request -> request.getHumanReadableId().
                        startsWith("HRI00"))
                .filter(request -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION)
                        .stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(
                                request.getTransportType())))
                .toList();

        checkOtoCargoRequest(expectedList,
                10,
                String.format("""
                        {"organizationId": "%s",
                        "pageSize": 100,
                        "id": "00"}
                        """, ORGANIZATION_ID),
                "ROLE_GUEST",
                USER1_ID);
    }

    @DisplayName("Фильтр по департаменту инициатора(автора) заявки")
    @Test
    @SneakyThrows
    void test_search_byDepartment() {
        var department = Department.builder()
                .id(UUID.randomUUID())
                .departmentName(String.format("Department%03d", 100))
                .organizationId(UUID.fromString(ORGANIZATION_ID))
                .build();
        department = departmentRepository.saveAndFlush(department);

        var position = positionRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new JUnitException("Position not found"));

        var departmentId = department.getId();

        var author = createPassenger(UUID.randomUUID(),
                "HRS" + 100,
                String.format("LastName%03d", 100),
                String.format("FirstName%03d", 100),
                String.format("Patronymic%03d", 100),
                String.format("Phone%03d", 100),
                position,
                department);
        author = employeeRepository.save(author);

        var request = requestRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new JUnitException("Request not found"));
        request.setAuthor(author);
        request.setId(UUID.randomUUID());
        request.setHumanReadableId("HRI100");
        requestRepository.saveAndFlush(request);

        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending())
                .stream()
                .filter(r -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(r.getTransportType())))
                .filter(r -> Objects.equals(r.getAuthor().getDepartment().getId(), departmentId))
                .toList();

        checkOtoCargoRequest(expectedList,
                1,
                String.format("""
                        {"organizationId": "%s",
                        "pageSize": 100,
                        "authorDepartment": "%s"}
                        """, ORGANIZATION_ID, departmentId),
                "GUEST",
                USER1_ID);
    }

    @DisplayName("Фильтр по отправителю")
    @Test
    @SneakyThrows
    void test_search_bySender() {
        var department = Department.builder()
                .id(UUID.randomUUID())
                .departmentName(String.format("Department%03d", 100))
                .organizationId(organizationId)
                .build();
        department = departmentRepository.saveAndFlush(department);

        var position = positionRepository.findAll().stream().findFirst().orElseThrow();

        var author = createPassenger(UUID.randomUUID(),
                "HRS" + 100,
                String.format("LastName%03d", 100),
                String.format("FirstName%03d", 100),
                String.format("Patronymic%03d", 100),
                String.format("Phone%03d", 100),
                position,
                department);
        author = employeeRepository.save(author);

        var request = requestRepository.findAll().stream()
                .filter(r -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(r.getTransportType())))
                .filter(r -> r.getOrganization().getId().equals(organizationId))
                .findFirst().orElseThrow();
        request.setAuthor(author);
        request.setId(UUID.randomUUID());
        request.setHumanReadableId("HRI100");
        request.setSenderName("sender");
        request.setSenderPhone("+900000");
        request.setRecipientName("recipient");
        request.setRecipientPhone("+787654356");
        requestRepository.saveAndFlush(request);

        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending())
                .stream().filter(r -> "sender".equals(r.getSenderName()))
                .toList();

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {"organizationId": "%s",
                                "pageSize": 100,
                                "senderName": "sender"}
                                """, ORGANIZATION_ID))
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER1_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                        )
                )
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(expectedList.size()))
                .andReturn();


    }

    @DisplayName("Фильтр по получателю")
    @Test
    @SneakyThrows
    void test_search_byRecipient() {
        var department = Department.builder()
                .id(UUID.randomUUID())
                .departmentName(String.format("Department%03d", 100))
                .organizationId(UUID.fromString(ORGANIZATION_ID))
                .build();
        department = departmentRepository.saveAndFlush(department);

        var position = positionRepository.findAll().stream().findFirst().orElseThrow();

        var author = createPassenger(UUID.randomUUID(),
                "HRS" + 100,
                String.format("LastName%03d", 100),
                String.format("FirstName%03d", 100),
                String.format("Patronymic%03d", 100),
                String.format("Phone%03d", 100),
                position,
                department);
        author = employeeRepository.save(author);

        var request = requestRepository.findAll().stream()
                .filter(r -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(r.getTransportType())))
                .filter(r -> r.getOrganization().getId().equals(organizationId))
                .findFirst().orElseThrow();
        request.setAuthor(author);
        request.setId(UUID.randomUUID());
        request.setHumanReadableId("HRI100");
        request.setSenderName("sender");
        request.setSenderPhone("+900000");
        request.setRecipientName("recipient");
        request.setRecipientPhone("+787654356");
        requestRepository.saveAndFlush(request);

        var expectedList = requestRepository.findAll(Sort.by(Request_.CREATION_TIME).descending())
                .stream()
                .filter(r -> TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                        .anyMatch(s -> s == TransportTypeEnum.valueOf(r.getTransportType())))
                .filter(r -> "sender".equals(r.getSenderName()))
                .toList();

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(String.format("""
                                {
                                "organizationId": "%s",
                                "recipientName": "recipient",
                                "pageSize": 100
                                }
                                """, ORGANIZATION_ID))
                        .with(jwt().jwt(
                                                builder -> builder.jti(USER1_ID)
                                        )
                                        .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                        )
                )
                .andExpect(status().isOk()).andExpect(jsonPath("$.content.length()").value(expectedList.size()))
                .andReturn();
    }

    /**
     * Источник данных для параметризованного теста фильтрации по группам исполнителей.
     * Покрывает три тестируемые ветви {@code OtoEngineerServiceImpl.addExecuterGroupPredicate}:
     * <ul>
     *     <li>«Все группы» — {@code executorGroupIds} пуст, {@code emptyExecutorGroup=false} — фильтр не применяется;</li>
     *     <li>«Без групп» — {@code emptyExecutorGroup=true} — фильтр {@code executorGroupId IS NULL};</li>
     *     <li>«Конкретные группы» — {@code executorGroupIds} непуст — фильтр {@code executorGroupId IN (...)}.</li>
     * </ul>
     * Четвёртая ветвь спеки («emptyExecutorGroup=true имеет приоритет») блокируется на уровне
     * валидации {@code ValidationServiceImpl.validateEmptyExecutorGroups} и проверяется отдельным тестом
     * {@code test_add_ExecutorGroupFilter_BothSet}.
     */
    private static Stream<Arguments> provideTestAddExecutorGroupFilterData() {
        return Stream.of(
                Arguments.of("all groups — фильтр не применяется",
                        String.format("""
                                {"organizationId": "%s", "pageSize": 100}
                                """, ORGANIZATION_ID),
                        10),
                Arguments.of("empty groups — только заявки без группы",
                        String.format("""
                                {"organizationId": "%s", "pageSize": 100, "emptyExecutorGroup": true}
                                """, ORGANIZATION_ID),
                        4),
                Arguments.of("specific group — фильтр по одной группе",
                        String.format("""
                                {"executorGroupIds": ["%s"], "pageSize": 100}
                                """, EXECUTOR_GROUP_ID_1),
                        10),
                Arguments.of("specific groups — фильтр по двум группам",
                        String.format("""
                                {"executorGroupIds": ["%s", "%s"], "pageSize": 100}
                                """, EXECUTOR_GROUP_ID_1, EXECUTOR_GROUP_ID_2),
                        20)
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestAddExecutorGroupFilterData")
    @DisplayName("Фильтр по группам исполнителей")
    @SneakyThrows
    void test_add_ExecutorGroupFilter(String scenario, String requestBody, int expectedSize) {
        var role = "all groups — фильтр не применяется".equals(scenario)
                || "empty groups — только заявки без группы".equals(scenario)
                ? "ROLE_GUEST" : "ROLE_ADMIN_DATA_MASTER";

        var mvcResult = mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody)
                        .with(jwt().jwt(jwtBuilder -> jwtBuilder.jti(USER_ENGINEER_ID))
                                .authorities(new SimpleGrantedAuthority(role)))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(expectedSize))
                .andReturn();

        Map<String, Object> content = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8), new TypeReference<>() {});
        List<OtoEngineerCargoRequestDetailDTO> actualList = objectMapper.readValue(
                new ObjectMapper().writeValueAsBytes(content.get("content")), new TypeReference<>() {});
        assertThat(actualList).hasSize(expectedSize);
    }

    @Test
    @DisplayName("BadRequest при одновременной передаче emptyExecutorGroup=true и executorGroupIds")
    @SneakyThrows
    void test_add_ExecutorGroupFilter_BothSet() {
        var requestBody = String.format("""
                {"organizationId": "%s", "pageSize": 100,
                 "emptyExecutorGroup": true, "executorGroupIds": ["%s"]}
                """, ORGANIZATION_ID, EXECUTOR_GROUP_ID_1);

        mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody)
                        .with(jwt().jwt(jwtBuilder -> jwtBuilder.jti(USER_ENGINEER_ID))
                                .authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
                )
                .andExpect(status().isBadRequest());
    }

    /**
     * Проверяем работу контроллера, отправляя запрос с заданным фильтром, и ожидаем в ответ определенный результат
     *
     * @param expectedList список заявок {@link List<Request>}, преобразованную информацию которых мы ожидаем получить после ответа контроллера
     * @param expectedSize ожидаемый размер объектов в ответе
     */
    @SneakyThrows
    private void checkOtoCargoRequest(List<Request> expectedList, int expectedSize, String requestBody, String roleName, String userId) {
        var mvcResult = mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                        .header(AUTHORIZATION_HEADER_NAME, AUTHORIZATION_HEADER_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody)
                        .with(jwt().jwt(
                                                jwtBuilder -> jwtBuilder.jti(userId)
                                        )
                                        .authorities(new SimpleGrantedAuthority(roleName))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(expectedSize))
                .andReturn();

        Map<String, Object> content = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });
        byte[] bytes = new ObjectMapper().writeValueAsBytes(content.get("content"));
        List<OtoEngineerCargoRequestDetailDTO> actualList = objectMapper.readValue(bytes, new TypeReference<>() {
        });
        assertThat(actualList).hasSize(expectedSize);
        checkOtoDto(expectedList, actualList, expectedSize);
    }

    /**
     * @param expectedList список заявок {@link List<Request>}
     * @param actualList   список заявок {@link List<OtoEngineerCargoRequestDetailDTO>}
     * @param size         размер списка для итерирования
     */
    private void checkOtoDto(List<Request> expectedList, List<OtoEngineerCargoRequestDetailDTO> actualList, int size) {
        for (var i = 0; i < size; i++) {
            var expected = expectedList.get(i);
            var actual = actualList.stream()
                    .filter(dto -> Objects.equals(dto.getHumanReadableId(), expected.getHumanReadableId()))
                    .findAny()
                    .orElseThrow(() -> new JUnitException(
                            "OtoEngineerCargoRequestDetailDTO not found by HumanReadableId " + expected.getHumanReadableId()));
            assertThat(actual.getHumanReadableId()).isEqualTo(expected.getHumanReadableId());
            assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
            assertThat(actual.getCargoTransportType()).isEqualTo(expected.getTransportType());
            assertThat(actual.getSenderAddress()).isEqualTo(expected.getWaypoints().getFirst().getAddress().getAddressString());
            assertThat(actual.getRecipientAddress()).isEqualTo(expected.getWaypoints().get(1).getAddress().getAddressString());
            assertThat(actual.getExpected().waypointsCount()).isEqualTo(expected.getWaypoints().size());
            assertThat(actual.getSender()).isEqualTo(expected.getSender().getFIO());
            assertThat(actual.getRecipient()).isEqualTo(expected.getRecipient().getFIO());
            assertThat(actual.getTemplate()).hasToString(expected.getTemplate() ? "ДА" : "НЕТ");
            assertThat(convertTime(actual.getCreationTime())).isEqualTo(expected.getCreationTime());
            assertThat(convertTime(actual.getDesiredDate())).isEqualTo(expected.getDesiredDate());
            assertThat(convertTime(actual.getTransferTime())).isEqualTo(expected.getTransferTime());
            assertThat(convertTime(actual.getShipmentTime())).isEqualTo(expected.getShipmentTime());
            assertThat(convertTime(actual.getControlDate())).isEqualTo(expected.getControlDate());
        }
    }

    private LocalDateTime convertTime(ZonedDateTime timeToConvert) {
        return Optional.ofNullable(timeToConvert)
                .map(time -> time.toInstant().atZone(ZoneOffset.UTC))
                .map(ZonedDateTime::toLocalDateTime).orElse(null);
    }

    private Request createCargoRequest(
            UUID requestId,
            String humanReadableId,
            TripRequestStatus status,
            TransportTypeEnum transportType,
            List<Waypoint> waypoints,
            LocalDateTime creationTime,
            LocalDateTime desiredDate,
            LocalDateTime approvalDate,
            LocalDateTime transferTime,
            LocalDateTime shipmentTime,
            Employee sender,
            Employee recipient,
            Organization organization
    ) {
        return Request.builder()
                .id(requestId)
                .author(sender)
                .humanReadableId(humanReadableId)
                .status(status.name())
                .transportType(transportType.name())
                .waypoints(waypoints)
                .creationTime(creationTime)
                .desiredDate(desiredDate)
                .approvalDate(approvalDate)
                .transferTime(transferTime)
                .shipmentTime(shipmentTime)
                .sender(sender)
                .recipient(recipient)
                .organization(organization)
                .template(true)
                .build();
    }


    private void buildEngineerEnv() {

        var organizationGroup =
                organizationGroupRepository.saveAndFlush(OrganizationGroup.builder().id(organizationGroupId1).name("organizationGroup1").build());
        var organizationGroup2 =
                organizationGroupRepository.saveAndFlush(OrganizationGroup.builder().id(organizationGroupId2).name("organizationGroup2").build());

        var organization = organizationRepository
                .saveAndFlush(Organization.builder().id(organizationId).officialName("organization 1").organizationGroup(organizationGroup).build());
        organizationRepository
                .saveAndFlush(Organization.builder().id(organizationId2).officialName("organization 2").organizationGroup(organizationGroup).build());
        organizationRepository
                .saveAndFlush(
                        Organization.builder().id(organizationId3).officialName("organization 3").organizationGroup(organizationGroup2).build());

        var department = departmentRepository.saveAndFlush(Department.builder()
                .id(organizationId)
                .organizationId(organization.getId())
                .departmentName("Department name")
                .build());
        employeeRepository.saveAndFlush(Employee.builder()
                .id(UUID.fromString(USER_ENGINEER_ID))
                .userId(UUID.fromString(USER_ENGINEER_ID))
                .department(department)
                .firstName("FirstName")
                .lastName("lastName")
                .humanReadableId("EMPLOYEE")
                .personnelNumber("123")
                .build());
    }

    private void generateAndSave(int i, Organization organization, TransportTypeEnum transportType) {
        var organizationId = organization.getId();
        var position = createPosition(String.format("Position%03d", i));
        position = positionRepository.save(position);

        var department = Department.builder()
                .id(UUID.randomUUID())
                .departmentName(String.format("Department%03d", i))
                .organizationId(organizationId)
                .build();
        department = departmentRepository.save(department);

        var sender = createPassenger(UUID.fromString(USER1_ID),
                "HRS" + i,
                String.format("LastName%03d", i),
                String.format("FirstName%03d", i),
                String.format("Patronymic%03d", i),
                String.format("Phone%03d", i),
                position,
                department);
        sender = employeeRepository.save(sender);

        var recipient = createPassenger(UUID.fromString(USER2_ID), "HRR" + i,
                String.format("LastName%03d", i),
                String.format("FirstName%03d", i),
                String.format("Patronymic%03d", i),
                String.format("Phone%03d", i),
                position,
                department);
        recipient = employeeRepository.save(recipient);

        var statusArray = TripRequestStatus.CARGO_STATUSES.toArray(TripRequestStatus[]::new);

        var contractor = contractorRepository.saveAndFlush(Contractor.builder().id(UUID.randomUUID()).name("new " + i).active(true).build());
        var routelist = routeRepository.saveAndFlush(Routelist.builder()
                .id(UUID.randomUUID())
                .status(TripRequestStatus.CARGO_AWAITING_DATA)
                .humanReadableId(String.format("HRI%03d", i))
                .contractor(ContractorDTO.builder()
                        .id(contractor.getId())
                        .name(contractor.getName())
                        .build())
                .build());

        var request = createCargoRequest(UUID.randomUUID(),
                String.format("HRI%03d", i),
                statusArray[i % statusArray.length],
                transportType,
                new ArrayList<>(),
                LocalDateTime.now().plusDays(i),
                LocalDateTime.now().plusDays(100 + i),
                LocalDateTime.now().plusDays(10 + i),
                LocalDateTime.now().plusDays(20 + i),
                LocalDateTime.now().plusDays(30 + i),
                sender,
                recipient,
                organization);

        request.setContractor(contractor);
        request.setTripId(routelist.getId());
        request.setExecutorGroupId(switch (i % 3) {
            case 0 -> null;
            case 1 -> UUID.fromString(EXECUTOR_GROUP_ID_1);
            default -> UUID.fromString(EXECUTOR_GROUP_ID_2);
        });
        request.setExecutorGroupName(switch (i % 3) {
            case 0 -> null;
            case 1 -> "ExecutorGroup1";
            default -> "ExecutorGroup2";
        });

        var savedRequest = requestRepository.saveAndFlush(request);

        for (var w = 0; w < 2; w++) {
            var address = Address.builder()
                    .id(UUID.randomUUID())
                    .region(String.format("Region%d%03d", w, i))
                    .city(String.format("City%d%03d", w, i))
                    .street(String.format("Street%d%03d", w, i))
                    .house(String.format("House%d%03d", w, i))
                    .addressString(String.format("AddressString%d%03d", w, i))
                    .build();

            address = addressRepository.save(address);

            var waypoint = Waypoint.builder()
                    .id(UUID.randomUUID())
                    .address(address)
                    .orderingIndex(i)
                    .request(savedRequest)
                    .build();
            Waypoint savedWaypoint = waypointRepository.saveAndFlush(waypoint);
            savedRequest.getWaypoints().add(savedWaypoint);
            if (w == 0) {
                savedRequest.setStartWaypoint(savedWaypoint);
            } else {
                savedRequest.setEndWaypoint(savedWaypoint);
            }
        }
        requestRepository.saveAndFlush(savedRequest);
    }
}
