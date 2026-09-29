package ru.sber.transport.telemechanic.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.telemechanic.database.dao.*;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.RegistryDto;
import ru.sber.transport.telemechanic.dto.ReportSearchDto;
import ru.sber.transport.telemechanic.enumerate.RequestSortOption;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.telemechanic.TestData.*;
import static ru.sber.transport.telemechanic.enumerate.Role.*;

@DisplayName("Проверка сервиса реестра")
@AutoConfigureMockMvc
@SpringBootTest
@EmbeddedPostgres
@Transactional
class ReportControllerTest {
    private static final String REPORT_URI = "/report";
    private Request request1;
    private Request request2;
    private Request request3;
    private Request request4;
    private Request request5;
    private Request request6;
    private Request request7;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @Autowired
    private RequestRepository requestRepository;
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
    @MockitoBean
    private Clock clock;
    private final static LocalDateTime LOCAL_DATE = LocalDateTime.of(2022, 11, 29, 10, 11);
    
    @BeforeEach
    void setup() {
        var fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        Mockito.<AuthorizationManager<?>>reset(manager);
        var organization1 = organizationRepository.save(createOrganization1());
        var organization2 = organizationRepository.save(createOrganization2());
        var organization3 = organizationRepository.save(createOrganization3());
        var department1 = departmentRepository.save(createDepartment1(organization1, null));
        var department2 = departmentRepository.save(createDepartment2(organization2, null));
        var department3 = departmentRepository.save(Department.builder()
                                                              .id(UUID.randomUUID())
                                                              .humanReadableId("DT-0001-00000003")
                                                              .organization(organization3)
                                                              .departmentName("departmentName3")
                                                              .build());
        var position1 = positionRepository.save(createPosition1(organization1));
        var position2 = positionRepository.save(createPosition2(organization2));
        var position3 = positionRepository.save(createPosition3(organization3));
        var employee1 = employeeRepository.save(createEmployee1(department1, position1));
        var employee2 = employeeRepository.save(createEmployee2(department2, position2));
        var employee3 = employeeRepository.save(createEmployee3(department3, position3));
        var transport1 = transportRepository.save(new Transport(UUID.randomUUID(),
                                                                "А010ЕК50",
                                                                "ВАЗ",
                                                                "2101",
                                                                100000,
                                                                TransportStatus.IN_USE,
                                                                "-",
                                                                "-",
                                                                40,
                                                                new HashSet<>(Set.of(organization1)),
                                                                null, null, null));
        var transport2 = transportRepository.save(new Transport(UUID.randomUUID(),
                                                                "А199ЕК799",
                                                                "ВАЗ",
                                                                "2101",
                                                                100000,
                                                                TransportStatus.IN_USE,
                                                                "-",
                                                                "-",
                                                                40,
                                                                new HashSet<>(Set.of(organization1)),
                                                                null, null, null));
        var transport3 = transportRepository.save(new Transport(UUID.randomUUID(),
                                                                "А010ЕК51",
                                                                "ВАЗ",
                                                                "2101",
                                                                100000,
                                                                TransportStatus.IN_USE,
                                                                "-",
                                                                "-",
                                                                40,
                                                                new HashSet<>(Set.of(organization1)),
                                                                null, null, null));
        request1 = createRequest(employee1, transport1, ORGANIZATION_1_ID);
        request1.setStatus(RequestStatus.CANCELED);
        request1.setHumanReadableId("WA-0001-00000001");
        request1 = requestRepository.save(request1);
        
        request2 = createRequest(employee1, transport1, ORGANIZATION_1_ID);
        request2.setStatus(RequestStatus.EXPIRED);
        request2.setHumanReadableId("WA-0001-00000002");
        request2 = requestRepository.save(request2);
        
        request3 = createRequest(employee1, transport1, ORGANIZATION_1_ID);
        request3.setStatus(RequestStatus.DECLINED);
        request3.setHumanReadableId("WA-0001-00000003");
        request3.setInspector(employee1);
        request3.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(9));
        request3 = requestRepository.save(request3);
        
        request4 = createRequest(employee1, transport1, ORGANIZATION_1_ID);
        request4.setHumanReadableId("WA-0001-00000004");
        request4 = requestRepository.save(request4);
        
        request5 = createRequest(employee2, transport2, ORGANIZATION_2_ID);
        request5.setStatus(RequestStatus.FINISHED);
        request5.setInspector(employee2);
        request5.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(12));
        request5.setHumanReadableId("WA-0002-00000001");
        request5 = requestRepository.save(request5);
        
        request6 = createRequest(employee2, transport2, ORGANIZATION_2_ID);
        request6.setStatus(RequestStatus.ON_THE_LINE);
        request6.setInspector(employee2);
        request6.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(12));
        request6.setHumanReadableId("WA-0002-00000002");
        request6 = requestRepository.save(request6);
        
        request7 = createRequest(employee3, transport3, ORGANIZATION_3_ID);
        request7.setStatus(RequestStatus.DONE);
        request7.setInspector(employee3);
        request7.setInspectionTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(12));
        request7.setHumanReadableId("WA-0003-00000003");
        request7.setCreationTime(request7.getCreationTime().minusYears(5));
        request7 = requestRepository.save(request7);
    }
    
    @Test
    @DisplayName("Получение реестра")
    void getReportTest() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder().build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertEquals(7, actualList.size());
        checkEquality(request7, actualList.get(0));
        checkEquality(request6, actualList.get(1));
        checkEquality(request5, actualList.get(2));
        checkEquality(request4, actualList.get(3));
        checkEquality(request3, actualList.get(4));
        checkEquality(request2, actualList.get(5));
        checkEquality(request1, actualList.get(6));
    }
    
    @Test
    @DisplayName("Получение реестра без роли, дающей доступ ко всем организациям")
    void getReportTestNotHaveAllOrganizationsRole() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder().build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertEquals(4, actualList.size());
        checkEquality(request4, actualList.get(0));
        checkEquality(request3, actualList.get(1));
        checkEquality(request2, actualList.get(2));
        checkEquality(request1, actualList.get(3));
    }
    
    @Test
    @DisplayName("Получение реестра с фильтром по id")
    void getReportWithFilterTest() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var searchRequest = ReportSearchDto.builder()
                                           .humanReadableId(request2.getHumanReadableId())
                                           .build();
        mockMvc.perform(post(REPORT_URI)
                                .content(objectMapper.writeValueAsString(searchRequest))
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(1))
               .andExpect(jsonPath("$.content[0].id").value(request2.getId().toString()));
    }
    
    @Test
    @DisplayName("Получение реестра с фильтром по StartCreationDate - EndCreationDate")
    void getReportWithPeriodCreationDateFilterTest() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var period = new ReportSearchDto.DateRange();
        period.setStart(LocalDateTime.now().minusYears(1));
        period.setEnd(LocalDateTime.now().plusYears(1));
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder().period(period).build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertEquals(6, actualList.size());
        checkEquality(request6, actualList.get(0));
        checkEquality(request5, actualList.get(1));
        checkEquality(request4, actualList.get(2));
        checkEquality(request3, actualList.get(3));
        checkEquality(request2, actualList.get(4));
        checkEquality(request1, actualList.get(5));
    }
    
    @Test
    void getReportWithDepartmentIdsFilterTest() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var ids = Set.of(DEPARTMENT_1_ID, DEPARTMENT_2_ID, UUID.randomUUID());
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder()
                                                                                                     .organizationId(ORGANIZATION_1_ID)
                                                                                                     .departmentIds(ids)
                                                                                                     .build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertEquals(4, actualList.size());
        checkEquality(request1, actualList.get(3));
        checkEquality(request2, actualList.get(2));
        checkEquality(request3, actualList.get(1));
        checkEquality(request4, actualList.get(0));
    }
    
    @Test
    void getReportWithNoDepartmentIds() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var ids = Set.of(UUID.randomUUID());
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder()
                                                                                                     .organizationId(ORGANIZATION_1_ID)
                                                                                                     .departmentIds(ids)
                                                                                                     .build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertTrue(actualList.isEmpty());
    }
    
    @Test
    void getReportWithEmptyDepartmentIds() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var ids = new HashSet<UUID>();
        var result = mockMvc.perform(post(REPORT_URI)
                                             .content(objectMapper.writeValueAsString(ReportSearchDto.builder()
                                                                                                     .departmentIds(ids)
                                                                                                     .build()))
                                             .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk())
                            .andReturn();
        var content = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                             new TypeReference<Map<String, Object>>() {
                                             });
        byte[] bytes = objectMapper.writeValueAsBytes(content.get("content"));
        var actualList = objectMapper.readValue(bytes, new TypeReference<List<RegistryDto>>() {
        });
        assertEquals(7, actualList.size());
        checkEquality(request7, actualList.get(0));
        checkEquality(request6, actualList.get(1));
        checkEquality(request5, actualList.get(2));
        checkEquality(request4, actualList.get(3));
        checkEquality(request3, actualList.get(4));
        checkEquality(request2, actualList.get(5));
        checkEquality(request1, actualList.get(6));
    }
    
    @Test
    @DisplayName("Получение реестра, проверка сортировок")
    void repostSortSettingTest() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_DATA_MASTER.name());
        var pageSetting = new ReportSearchDto.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(10);
        var sortSetting1 = new ReportSearchDto.SortSetting();
        sortSetting1.setDirectionAsc(true);
        var searchDto1 = ReportSearchDto.builder()
                                        .sortSetting(sortSetting1)
                                        .pageSetting(pageSetting)
                                        .build();
        mockMvc.perform(post(REPORT_URI)
                                .content(objectMapper.writeValueAsString(searchDto1))
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(7))
               .andExpect(jsonPath("$.content[0].id").value(request1.getId().toString()))
               .andExpect(jsonPath("$.content[1].id").value(request2.getId().toString()))
               .andExpect(jsonPath("$.content[2].id").value(request3.getId().toString()))
               .andExpect(jsonPath("$.content[3].id").value(request4.getId().toString()))
               .andExpect(jsonPath("$.content[4].id").value(request5.getId().toString()))
               .andExpect(jsonPath("$.content[5].id").value(request6.getId().toString()))
               .andExpect(jsonPath("$.content[6].id").value(request7.getId().toString()));
        
        var sortSetting2 = new ReportSearchDto.SortSetting();
        sortSetting2.setDirectionAsc(true);
        sortSetting2.setProperty(RequestSortOption.ORGANIZATION_OFFICIAL_NAME);
        var searchDto2 = ReportSearchDto.builder()
                                        .sortSetting(sortSetting2)
                                        .pageSetting(pageSetting)
                                        .build();
        mockMvc.perform(post(REPORT_URI)
                                .content(objectMapper.writeValueAsString(searchDto2))
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_DATA_MASTER.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(7))
               .andExpect(jsonPath("$.content[0].id").value(request4.getId().toString()))
               .andExpect(jsonPath("$.content[1].id").value(request3.getId().toString()))
               .andExpect(jsonPath("$.content[2].id").value(request2.getId().toString()))
               .andExpect(jsonPath("$.content[3].id").value(request1.getId().toString()))
               .andExpect(jsonPath("$.content[4].id").value(request6.getId().toString()))
               .andExpect(jsonPath("$.content[5].id").value(request5.getId().toString()))
               .andExpect(jsonPath("$.content[6].id").value(request7.getId().toString()));
        
    }
    
    @Test
    @DisplayName("Получение реестра, проверка сортировок с ролью ROLE_DISPATCHER_SUPPORT_SERVICE")
    void repostSortSettingTestSupport() throws Exception {
        AuthorizeUtils.authorize(manager, ROLE_DISPATCHER_SUPPORT_SERVICE.name());
        var pageSetting = new ReportSearchDto.PageSetting();
        pageSetting.setPage(0);
        pageSetting.setSize(10);
        var sortSetting = new ReportSearchDto.SortSetting();
        sortSetting.setDirectionAsc(false);
        sortSetting.setProperty(RequestSortOption.HUMAN_READABLE_ID);
        var searchDto = ReportSearchDto.builder()
                                       .sortSetting(sortSetting)
                                       .pageSetting(pageSetting)
                                       .build();
        mockMvc.perform(post(REPORT_URI)
                                .content(objectMapper.writeValueAsString(searchDto))
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_DISPATCHER_SUPPORT_SERVICE.name())))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalPages").value(1))
               .andExpect(jsonPath("$.totalElements").value(7))
               .andExpect(jsonPath("$.content[0].id").value(request7.getId().toString()))
               .andExpect(jsonPath("$.content[1].id").value(request6.getId().toString()))
               .andExpect(jsonPath("$.content[2].id").value(request5.getId().toString()))
               .andExpect(jsonPath("$.content[3].id").value(request4.getId().toString()))
               .andExpect(jsonPath("$.content[4].id").value(request3.getId().toString()))
               .andExpect(jsonPath("$.content[5].id").value(request2.getId().toString()))
               .andExpect(jsonPath("$.content[6].id").value(request1.getId().toString()));
    }
    
    private void checkEquality(Request expected, RegistryDto actual) {
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getHumanReadableId(), actual.humanReadableId());
        assertEquals(expected.getAuthor().getOrganization().getOfficialName(), actual.officialName());
        checkLocalDateTimeCell(expected.getCreationTime(), actual.creationTime());
        assertEquals(expected.getAuthor().getFIO(), actual.fullName());
        assertEquals(expected.getTransport().getStateNumber(), actual.stateNumber());
        assertEquals(expected.getTransport().getBrand(), actual.brand());
        assertEquals(expected.getTransport().getModel(), actual.model());
        checkLocalDateTimeCell(expected.getInspectionTime(), actual.inspectionTime());
        assertEquals(Objects.isNull(expected.getInspector()) ? "" : expected.getInspector().getFIO(), actual.inspectorFullName());
        assertEquals(Objects.isNull(expected.getInspector()) ? null : expected.getInspector().getPersonnelNumber(),
                     actual.inspectorPersonnelNumber());
        checkInspectionMark(expected.getStatus(), actual.inspectionMark());
    }
    
    private static void checkInspectionMark(RequestStatus status, String inspectionMark) {
        if (status.equals(RequestStatus.ON_THE_LINE) || status.equals(RequestStatus.FINISHED)) {
            assertEquals("Пройден", inspectionMark);
        } else if (status.equals(RequestStatus.DECLINED)) {
            assertEquals("Не пройден", inspectionMark);
        } else {
            assertNull(inspectionMark);
        }
    }
    
    private static void checkLocalDateTimeCell(LocalDateTime actual, LocalDateTime expected) {
        if (Objects.isNull(actual)) {
            assertThat(expected).isNull();
        } else {
            assertThat(actual.truncatedTo(ChronoUnit.SECONDS)).isEqualTo(expected.truncatedTo(ChronoUnit.SECONDS));
        }
    }
}