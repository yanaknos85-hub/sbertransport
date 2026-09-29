package ru.sberbank.transport.oto.cargo.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.SharedTestData;
import ru.sberbank.transport.oto.cargo.database.dao.*;
import ru.sberbank.transport.oto.cargo.database.model.*;
import ru.sberbank.transport.oto.cargo.dto.ContractorDTO;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.enums.CriticalExpireDateEnum;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.transport.oto.cargo.SharedTestData.*;


@AutoConfigureMockMvc
@DisplayName("Проверка контроллера отчетов по грузам")
@EmbeddedPostgres()
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
class OtoRequestsControllerCargoByCriticalExpireDateTest {

    private static final LocalDateTime LOCAL_DATE = LocalDateTime.of(2023, 4, 13, 5, 34, 56);

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

    private final UUID organizationId = UUID.fromString(SharedTestData.ORGANIZATION_ID);

    private final UUID organizationGroupId1 = UUID.fromString(SharedTestData.ORGANIZATION_GROUP_ID);

    private Organization organization;

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

        buildEngineerEnv();

        int index = 0;
        List<RequestDateTestCase> cases = List.of(
                RequestDateTestCase.of(++index, 10, 1,
                        LOCAL_DATE.toLocalDate().minusDays(4), LOCAL_DATE.minusDays(1).toLocalDate()),
                RequestDateTestCase.of(++index, 5, 5,
                        LOCAL_DATE.toLocalDate(), LOCAL_DATE.plusDays(3).toLocalDate()),
                RequestDateTestCase.of(++index, 0, 20,
                        LOCAL_DATE.plusDays(10).toLocalDate(), LOCAL_DATE.plusDays(16).toLocalDate()),
                RequestDateTestCase.of(++index, 5, 4,
                        LOCAL_DATE.toLocalDate(), LOCAL_DATE.plusDays(2).toLocalDate()),
                RequestDateTestCase.of(++index, 4, 5,
                        LOCAL_DATE.plusDays(1).toLocalDate(), LOCAL_DATE.plusDays(3).toLocalDate()),
                RequestDateTestCase.of(++index, 1, 1,
                        LOCAL_DATE.plusDays(0).toLocalDate(), LOCAL_DATE.plusDays(1).toLocalDate()),
                RequestDateTestCase.of(++index, 0, 10,
                        LOCAL_DATE.toLocalDate().plusDays(5), LOCAL_DATE.plusDays(8).toLocalDate())
        );

        cases.forEach(this::testRequestDates);
    }

    private record RequestDateTestCase(
            int testCaseNum,
            int daysBeforeNow,
            int daysAfterNow,
            LocalDate expectedCdLess50PctDate,
            LocalDate expectedCdLess20PctDate
    ) {
        static RequestDateTestCase of(int testCaseNum, int daysBeforeCreation, int daysAfterDesired,
                                      LocalDate expectedCdLess50PctDate, LocalDate expectedCdLess20PctDate) {
            return new RequestDateTestCase(testCaseNum, daysBeforeCreation, daysAfterDesired,
                    expectedCdLess50PctDate, expectedCdLess20PctDate);
        }
    }

    static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(CriticalExpireDateEnum.LESS_THAN_20, 1,
                        List.of("HRI001")),
                Arguments.of(CriticalExpireDateEnum.BETWEEN_20_AND_50, 3,
                        List.of("HRI002", "HRI004", "HRI006")),
                Arguments.of(CriticalExpireDateEnum.MORE_THAN_50, 3,
                        List.of("HRI003", "HRI005", "HRI007")),
                Arguments.of(null, 7,
                        List.of("HRI001", "HRI002", "HRI003", "HRI004", "HRI005", "HRI006", "HRI007"))

        );
    }

    @ParameterizedTest
    @MethodSource("testData")
    @DisplayName("Получение всех. Фильтр по остатку времени отправления и отправки груза")
    void test_add_statusFilter_one(CriticalExpireDateEnum filterByPercents,
                                   int expectedCount,
                                   List<String> expectedRequests) throws Exception {
        assertThat(requestRepository.findAll()).hasSize(7);
        try (var mockedLocalDateTime = mockStatic(LocalDateTime.class, Mockito.CALLS_REAL_METHODS);
             var mockedLocaldate = mockStatic(java.time.LocalDate.class, Mockito.CALLS_REAL_METHODS)) {
            when(LocalDateTime.now(ZoneOffset.UTC)).thenReturn(LOCAL_DATE);
            when(LocalDateTime.now(ZoneId.of("UTC"))).thenReturn(LOCAL_DATE);
            when(LocalDate.now()).thenReturn(LOCAL_DATE.toLocalDate());
            when(LocalDate.now(ZoneOffset.UTC)).thenReturn(LOCAL_DATE.toLocalDate());

            var mvcResult = mockMvc.perform(post(CARGO_POST_URL_TEMPLATE)
                            .header(SharedTestData.AUTHORIZATION_HEADER_NAME, SharedTestData.AUTHORIZATION_HEADER_VALUE)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .with(jwt().jwt(
                                                    builder -> builder.jti(USER_ENGINEER_ID)
                                            )
                                            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_DATA_MASTER"))
                            )
                            .content(String.format("""
                                    {"organizationId": "%s",
                                    "criticalExpireDate": %s}
                                    """, ORGANIZATION_ID, filterByPercents != null ? "\""+filterByPercents.name()+"\"" : null)))
                    .andExpect(status().isOk())
                    .andReturn();
            Map<String, Object> content = objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                    new TypeReference<>() {
                    });
            List<OtoEngineerCargoRequestDetailDTO> actualList = objectMapper.readValue(new ObjectMapper().writeValueAsBytes(content.get("content")), new TypeReference<>() {
            });
            actualList.stream()
                    .map(dto -> "id: %s; cd desiredDate: %s, control date: %s".formatted(dto.getHumanReadableId(), dto.getDesiredDate(), dto.getControlDate()))
                    .forEach(System.out::println);
            assertThat(actualList).hasSize(expectedCount);
            if (expectedRequests != null) {
                assertThat(actualList.stream()
                        .map(OtoEngineerCargoRequestDetailDTO::getHumanReadableId)
                        .toList())
                        .containsExactlyInAnyOrderElementsOf(expectedRequests);
            }
        }
    }

    private void testRequestDates(RequestDateTestCase testCase) {
        saveRequestIntegrations(testCase.testCaseNum(), testCase.daysBeforeNow(), testCase.daysAfterNow());
    }

    private Request createCargoRequest(
            UUID requestId,
            String humanReadableId,
            TripRequestStatus status,
            TransportTypeEnum transportType,
            List<Waypoint> waypoints,
            LocalDateTime creationTime,
            LocalDateTime desiredDate,
            LocalDateTime controlDate,
            Organization organization
    ) {
        return Request.builder()
                .id(requestId)
                .humanReadableId(humanReadableId)
                .status(status.name())
                .transportType(transportType.name())
                .waypoints(waypoints)
                .creationTime(creationTime)
                .desiredDate(desiredDate)
                .controlDate(controlDate)
                .organization(organization)
                .template(false)
                .build();
    }


    private void buildEngineerEnv() {

        var organizationGroup =
                organizationGroupRepository.saveAndFlush(OrganizationGroup.builder().id(organizationGroupId1).name("organizationGroup1").build());

        organization = organizationRepository
                .saveAndFlush(Organization.builder().id(organizationId).officialName("organization 1").organizationGroup(organizationGroup).build());

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

    private void saveRequestIntegrations(int i, int daysBeforeNow, int daysAfterNow) {

        TransportTypeEnum transportType = TransportTypeEnum.DEDICATED;
        var orgId = organization.getId();

        positionRepository.save(Position.builder()
                .id(UUID.randomUUID())
                .name(String.format("Position%03d", i))
                .build());

        departmentRepository.save(Department.builder()
                .id(UUID.randomUUID())
                .departmentName(String.format("Department%03d", i))
                .organizationId(orgId)
                .build());

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
                LOCAL_DATE.minusDays(daysBeforeNow + 3),
                LOCAL_DATE.minusDays(daysBeforeNow),
                LOCAL_DATE.plusDays(daysAfterNow),
                organization);

        request.setContractor(contractor);
        request.setTripId(routelist.getId());

        var savedRequest = requestRepository.saveAndFlush(request);
        savedRequest = requestRepository.findById(savedRequest.getId()).get();

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
