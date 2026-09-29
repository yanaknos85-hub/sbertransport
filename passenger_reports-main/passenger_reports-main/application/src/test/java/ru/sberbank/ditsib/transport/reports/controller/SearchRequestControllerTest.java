package ru.sberbank.ditsib.transport.reports.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import lombok.Data;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.reports.dao.*;
import ru.sberbank.ditsib.transport.reports.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.reports.dto.PaymentDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.WaypointDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.*;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestMapper;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.constants.PublicCompensationType.CITY_TRIP_COMPENSATION;
import static ru.sberbank.ditsib.transport.constants.PublicTransportType.CITY_BUS;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера отчетов")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class SearchRequestControllerTest extends SharedTest {
    
    @Autowired
    private Consumer<Message<RequestMessage>> requestInput;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RequestMapper requestMapper;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private ContractRepository contractRepository;
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    @Autowired
    private CarsharingTariffRepository carsharingTariffRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private LimitRepository limitRepository;
    @Autowired
    private WaypointRepository waypointRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private RequestForCargoRepository requestForCargoRepository;
    @Autowired
    private PersonalCarRepository personalCarRepository;
    @Autowired
    private SingleTaxiTripRepository singleRepository;
    @Autowired
    private CoopTaxiTripRepository coopTaxiTripRepository;
    @Autowired
    private SharedRideRepository sharedRideRepository;
    @Autowired
    private TaxiTripRepository tripRepository;
    @Autowired
    private OrderKpiRepository orderKpiRepository;
    @Autowired
    private SharedRequestKpiRepository sharedRequestKpiRepository;
    @Autowired
    private TaxiTripRegistryRepository taxiTripRegistryRepository;
    @Autowired
    private TransportCompensationRepository transportCompensationRepository;
    
    @MockitoBean
    private OrganizationService organizationService;
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @Data
    public static class Content {
        public List<TaxiResponseDTO> content = new ArrayList<>();
    }
    
    @Data
    public static class PersonalContent {
        public List<PersonalResponseDTO> content;
    }
    
    @Data
    public static class PublicContent {
        public List<PublicResponseDTO> content;
    }
    
    @Data
    public static class CarsharingContent {
        public List<CarsharingResponseDTO> content;
    }
    
    @SneakyThrows
    @BeforeEach
    public void setUp() {
        
        request1.setLimit(limitRepository.save(limit1));
        request2.setLimit(limitRepository.save(limit2));
        request3.setLimit(limitRepository.save(limit3));
        request4.setLimit(limitRepository.save(limit4));
        request5.setLimit(limitRepository.save(limit5));
        request6.setLimit(limitRepository.save(limit5));
        
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee5.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_5);
        
        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        request5.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        request6.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_6);
        
        organizationRepository.saveAndFlush(organization1);
        organizationRepository.saveAndFlush(organization2);
        organizationRepository.saveAndFlush(organization3);
        organizationRepository.saveAndFlush(organization5);
        
        departmentRepository.saveAndFlush(departmentHead);
        departmentRepository.saveAndFlush(departmentLocal);
        departmentRepository.saveAndFlush(department1);
        departmentRepository.saveAndFlush(department2);
        departmentRepository.saveAndFlush(department3);
        departmentRepository.saveAndFlush(department5);
        
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);
        
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        employeeRepository.save(testEmployee3);
        employeeRepository.save(testEmployee4);
        employeeRepository.save(testEmployee5);
        
        contractorRepository.save(contractor1);
        contractorRepository.save(contractor2);
        contractorRepository.save(carsharingContractor);
        contractRepository.save(contract1);
        contractRepository.save(contract2);
        contractRepository.save(contract3);
        taxiTariffRepository.save(taxiTariff1);
        carsharingTariffRepository.save(carsharingTariff1);
        personalTariffRepository.save(personalTariff);
        publicTariffRepository.save(publicTariff);
        
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        addressRepository.saveAndFlush(address4);
        
        tripPurposeRepository.save(tripPurpose1);
        tripPurposeRepository.save(tripPurpose3);
        tripPurposeRepository.save(tripPurpose4);
        tripPurposeRepository.save(tripPurpose5);
        
        waypointRepository.saveAll(waypoints1);
        waypointRepository.saveAll(waypoints2);
        waypointRepository.saveAll(waypoints3);
        waypointRepository.saveAll(waypoints4);
        waypointRepository.saveAll(waypoints5);
        
        sharedRideRepository.save(sharedRide1);
        orderKpiRepository.save(orderKpi1);
        
        personalCarRepository.save(employee1PersonalCar);
        
        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);
        requestRepository.save(request4);
        requestRepository.save(request5);
        requestRepository.save(request6);
        
        cityTripCompensationList.forEach(tc -> tc.setRequest(request2));
        transportCompensationRepository.saveAll(cityTripCompensationList);
        
        singleRepository.save(singleTaxiTrip1);
        coopTaxiTripRepository.save(coopTaxiTrip1);
    }
    
    @AfterEach
    public void tearDown() {
        transportCompensationRepository.deleteAll();
        tripRepository.deleteAll();
        requestForCargoRepository.deleteAll();
        requestRepository.deleteAll();
        waypointRepository.deleteAll();
        addressRepository.deleteAll();
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();
        positionRepository.deleteAll();
        tripPurposeRepository.deleteAll();
        sharedRideRepository.deleteAll();
        taxiTariffRepository.deleteAll();
        carsharingTariffRepository.deleteAll();
        personalTariffRepository.deleteAll();
        publicTariffRepository.deleteAll();
        organizationRepository.deleteAll();
        orderKpiRepository.deleteAll();
        sharedRequestKpiRepository.deleteAll();
        taxiTripRegistryRepository.deleteAll();
        contractRepository.deleteAll();
        contractorRepository.deleteAll();
    }
    
    //taxi search
    @Test
    @DisplayName("Проверка постраничного запроса с сортировкой по пассажир ФИО asc")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getPageSortPassengerAsc() throws Exception {
        RequestForTaxiReportDTO.PageSetting pageSetting = new RequestForTaxiReportDTO.PageSetting();
        pageSetting.setPage(0);
        
        RequestForTaxiReportDTO.SortSetting sortSetting = new RequestForTaxiReportDTO.SortSetting();
        sortSetting.setProperty(RequestSortOption.PASSENGER_FULL_NAME);
        sortSetting.setDirectionAsc(true);
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setOrganizationId(organization3.getId());
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setSortSetting(sortSetting);
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        
        Set requestIds = Set.of(actual.content.get(0).getId(), actual.content.get(1).getId());
        assertTrue(requestIds.contains(request3.getId()));
        assertTrue(requestIds.contains(request4.getId()));
        
        Employee passengerRequestOne = request3.getPassenger();
        TaxiResponseDTO taxiResponseDTO3 = getTaxiResponseFromContent(actual.content, request3.getId());
        EmployeeDTO passengerActualOne = taxiResponseDTO3.getPassenger();
        assertThat(passengerRequestOne.getLastName()).isEqualTo(passengerActualOne.getLastName());
        assertThat(passengerRequestOne.getFirstName()).isEqualTo(passengerActualOne.getFirstName());
        assertThat(passengerRequestOne.getPatronymic()).isEqualTo(passengerActualOne.getPatronymic());
        
        Employee passengerRequestTwo = request4.getPassenger();
        TaxiResponseDTO taxiResponseDTO4 = getTaxiResponseFromContent(actual.content, request4.getId());
        EmployeeDTO passengerActualTwo = taxiResponseDTO4.getPassenger();
        assertThat(passengerRequestTwo.getLastName()).isEqualTo(passengerActualTwo.getLastName());
        assertThat(passengerRequestTwo.getFirstName()).isEqualTo(passengerActualTwo.getFirstName());
        assertThat(passengerRequestTwo.getPatronymic()).isEqualTo(passengerActualTwo.getPatronymic());
    }
    
    @Test
    @DisplayName("Проверка постраничного запроса с сортировкой по пассажир ФИО desc")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getPageSortPassengerDesc() throws Exception {
        RequestForTaxiReportDTO.PageSetting pageSetting = new RequestForTaxiReportDTO.PageSetting();
        pageSetting.setPage(0);
        
        RequestForTaxiReportDTO.SortSetting sortSetting = new RequestForTaxiReportDTO.SortSetting();
        sortSetting.setProperty(RequestSortOption.PASSENGER_FULL_NAME);
        sortSetting.setDirectionAsc(false);
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setOrganizationId(organization3.getId());
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setSortSetting(sortSetting);
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder
                                                                                                                      .jti(USER3_ID_STR)
                                                                                                                      .claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        
        Set requestIds = Set.of(actual.content.get(0).getId(), actual.content.get(1).getId());
        assertTrue(requestIds.contains(request3.getId()));
        assertTrue(requestIds.contains(request4.getId()));
        
        Employee passengerRequestOne = request3.getPassenger();
        TaxiResponseDTO taxiResponseDTO3 = getTaxiResponseFromContent(actual.content, request3.getId());
        EmployeeDTO passengerActualOne = taxiResponseDTO3.getPassenger();
        assertThat(passengerRequestOne.getLastName()).isEqualTo(passengerActualOne.getLastName());
        assertThat(passengerRequestOne.getFirstName()).isEqualTo(passengerActualOne.getFirstName());
        assertThat(passengerRequestOne.getPatronymic()).isEqualTo(passengerActualOne.getPatronymic());
        
        Employee passengerRequestTwo = request4.getPassenger();
        TaxiResponseDTO taxiResponseDTO4 = getTaxiResponseFromContent(actual.content, request4.getId());
        EmployeeDTO passengerActualTwo = taxiResponseDTO4.getPassenger();
        assertThat(passengerRequestTwo.getLastName()).isEqualTo(passengerActualTwo.getLastName());
        assertThat(passengerRequestTwo.getFirstName()).isEqualTo(passengerActualTwo.getFirstName());
        assertThat(passengerRequestTwo.getPatronymic()).isEqualTo(passengerActualTwo.getPatronymic());
    }
    
    private TaxiResponseDTO getTaxiResponseFromContent(List<TaxiResponseDTO> content, UUID requestId) {
        return content.stream().filter(e -> e.getId().equals(requestId)).findFirst().orElse(null);
    }
    
    @Test
    @DisplayName("Поиск заявок по human ID поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByRequestHumanId() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        // Подстрока нужна для того, чтобы выполнять поиск по like условию на указанное значение
        requestSearchDTO.setRequestHumanId(request3.getHumanReadableId());
        requestSearchDTO.setOrganizationId(organization3.getId());
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getHumanReadableId()).isEqualTo(actual.content.get(0).getHumanReadableId());
    }
    
    @Test
    @DisplayName("Поиск заявок по ФИО пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByFIO() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(testEmployee3.getFIO());
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request3.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по части фамилии пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByLastNamePart() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(testEmployee4.getLastName().substring(2));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по части фамилии и имени пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByLastNameFirstNamePart() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(testEmployee4.getLastName());
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по части имени и фамилии пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByFirstNameLastNamePart() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(testEmployee4.getFirstName().substring(2).concat(" ").concat(testEmployee4.getLastName()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по части имени и отчеству пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByFirstNamePatronymicPart() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(testEmployee4.getFirstName().substring(2).concat(" ").concat(testEmployee4.getPatronymic()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по части ФИО пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByFIOPart() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(String.format("%s  %s %s",
                                                      testEmployee4.getLastName().substring(2),
                                                      testEmployee4.getFirstName(),
                                                      testEmployee4.getPatronymic()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по ФИ пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByFI() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(String.format("%s   %s",
                                                      testEmployee4.getLastName(),
                                                      testEmployee4.getFirstName()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по ИO пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByIO() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(String.format("%s %s",
                                                      testEmployee4.getFirstName(),
                                                      testEmployee4.getPatronymic()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerRequest.getLastName()).isEqualTo(passengerActual.getLastName());
        assertThat(passengerRequest.getFirstName()).isEqualTo(passengerActual.getFirstName());
        assertThat(passengerRequest.getPatronymic()).isEqualTo(passengerActual.getPatronymic());
    }
    
    @Test
    @DisplayName("Поиск заявок по ИO пассажира со спецсимволами")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByIOSpec() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeFIO(String.format(";%s!!,, %s?????",
                                                      testEmployee4.getFirstName(),
                                                      testEmployee4.getPatronymic()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(actual.content.get(0).getId()).isEqualTo(request4.getId());
        
        Employee passengerRequest = request4.getPassenger();
        EmployeeDTO passengerActual = actual.content.get(0).getPassenger();
        assertThat(passengerActual.getLastName()).isEqualTo(passengerRequest.getLastName());
        assertThat(passengerActual.getFirstName()).isEqualTo(passengerRequest.getFirstName());
        assertThat(passengerActual.getPatronymic()).isEqualTo(passengerRequest.getPatronymic());
    }
    
    @Test
    @DisplayName("Пустые пассажиры при одиночной поездке")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_NotSetPassengersToSingleRequest() throws Exception {
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setRequestHumanId(request3.getHumanReadableId());
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getId()).isEqualTo(actual.content.get(0).getId());
        
        assertThat(actual.content.get(0).getPassengers()).isEmpty();
    }
    
    @Test
    @DisplayName("Заполнение всех пассажиров при совместной поездке на такси")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_SetPassengersToCoopRequest() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setRequestHumanId(request4.getHumanReadableId());
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
        
        //assertThat(actual.content.get(0).getPassengers()).hasSize(1); // not implemented
    }
    
    @Test
    @DisplayName("Поиск заявок по актуальным датам поездки")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByActualDepartureDate() throws Exception {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()));
        
        Map<String, Object> intervalMap = new HashMap<>();
        intervalMap.put("start", now.plusMinutes(20).toInstant(ZoneOffset.UTC));
        intervalMap.put("end", now.plusHours(1).toInstant(ZoneOffset.UTC));
        
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("actualDepartureDate", intervalMap);
        requestObject.put("organizationId", organization3.getId());
        var request = objectMapper.writeValueAsString(requestObject);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .header("Authorization",
                                                                                                                      UUID.randomUUID())
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getId()).isEqualTo(actual.content.get(0).getId());
        
    }
    
    @Test
    @DisplayName("Поиск заявок по целям поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByStatusSet() throws Exception {
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        
        requestSearchDTO.setRequestStatusSet(Set.of(TripRequestStatus.TAXI_APPROVED));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(actual.content.get(0).getId()).isIn(request3.getId(), request4.getId());
        assertThat(actual.content.get(1).getId()).isIn(request3.getId(), request4.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по целям поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByTripPurposes() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setPurposeSet(Set.of(
                TripPurposeDTO.builder().id(tripPurpose3.getId()).build(),
                TripPurposeDTO.builder().id(tripPurpose4.getId()).build()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(actual.content.get(0).getPurpose().getId()).isIn(tripPurpose3.getId(), tripPurpose4.getId());
        assertThat(actual.content.get(1).getPurpose().getId()).isIn(tripPurpose3.getId(), tripPurpose4.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок проверка разбиения на страницы")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getPageRequestByTripPurposes() throws Exception {
        RequestForTaxiReportDTO.PageSetting pageSetting = new RequestForTaxiReportDTO.PageSetting();
        pageSetting.setPage(1);
        pageSetting.setSize(1);
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setPurposeSet(Set.of(
                TripPurposeDTO.builder().id(tripPurpose3.getId()).build(),
                TripPurposeDTO.builder().id(tripPurpose4.getId()).build()));
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
    }
    
    @Test
    @DisplayName("Поиск заявок по стоимости поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByCost() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        RequestReportDTO.IntegerRange integerRange = new RequestReportDTO.IntegerRange();
        integerRange.setStart(400);
        requestSearchDTO.setFactCost(integerRange);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(actual.content.get(0).getId()).isIn(request4.getId(), request3.getId());
        assertThat(actual.content.get(1).getId()).isIn(request4.getId(), request3.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по времени ожидания на точках")
    @Disabled("feature not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByWaypointWaitTime() throws Exception {
        var interval = new HashMap<String, Object>();
        interval.put("start", Duration.ofMinutes(8).toMillis());
        interval.put("end", Duration.ofMinutes(20).toMillis());
        
        var requestObject = new HashMap<String, Object>();
        requestObject.put("waypointWaitTime", interval);
        requestObject.put("organizationId", organization3.getId());
        var request = objectMapper.writeValueAsString(requestObject);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId())).content(request)
                                                                                                              .header("Authorization",
                                                                                                                      UUID.randomUUID())
                                                                                                              .contentType(
                                                                                                                      MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(actual.content.get(0).getId()).isEqualTo(request3.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по выбранному тип поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByCoopTrip() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setCoopTrip(true);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(actual.content.get(0).getId()).isEqualTo(request4.getId());
        //assertThat(actual.content.get(0).getKpiSavings()).isEqualTo(100.0); //set savings_procents in request!
    }
    
    @Test
    @DisplayName("Поиск заявок по id совместной поездки")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByMagentaSharedRequest() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setSharedRideId(MAGENTA_ID_1);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по месту возникновения затрат (Такси)")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByMvz() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setCostCenter("123456789");
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(requestRepository.findAll()).hasSize(6);
        assertThat(actual.content).hasSize(1);
        
        TaxiResponseDTO taxiResponseDTO = actual.content.get(0);
        assertThat(request3.getId()).isEqualTo(taxiResponseDTO.getId());
        assertThat(request3.getPassenger().getCostCenter()).isEqualTo(taxiResponseDTO.getCostCenter());
    }
    
    @Test
    @DisplayName("Поиск заявок по целям рейтингу")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByRatingMark() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setRatingMarkSet(Set.of(4));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(actual.content.get(0).getId()).isEqualTo(request4.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по характеру работ")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByItinerantType() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeItinerantTypeSet(Set.of(ItinerantType.FULL));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(actual.content.get(0).getId()).isEqualTo(request3.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по адресу отправления (Позитивный)")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByDepartureAddressPositive() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setDepartureAddress("москва   улица 1");
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        TaxiResponseDTO taxiResponseDTO = actual.content.get(0);
        assertThat(taxiResponseDTO.getId()).isEqualTo(request4.getId());
        List<WaypointDTO> waypoints = taxiResponseDTO.getExpected().getWaypoints();
        assertThat(waypoints).isNotNull();
        
        WaypointDTO waypointDTO = waypoints.get(0);
        assertThat(waypointDTO.getCountry()).isNotNull();
        assertThat(waypointDTO.getCity()).isNotNull();
        assertThat(waypointDTO.getStreet()).isNotNull();
        assertThat(waypointDTO.getHouse()).isNotNull();
        
    }
    
    private static Stream<Arguments> addressSource() {
        return Stream.of(
                Arguments.of("Москва   Мещерaasdasdякова 14/9 "),
                Arguments.of("Мед"),
                Arguments.of("Ме6")
                        );
    }
    
    @ParameterizedTest
    @MethodSource("addressSource")
    @DisplayName("Поиск заявок по адресу отправления (Негативный)")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByDepartureAddressNegative(String address) throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setDepartureAddress(address);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).isEmpty();
    }
    
    @Test
    @DisplayName("Поиск заявок по адресу назначения")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByDestinationAddress() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setDestinationAddress("Нижний Новгород Улица 24");
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по адресу назначения")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByDestinationAddress2() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setDestinationAddress("24 к");
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request4.getId()).isEqualTo(actual.content.get(0).getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по должности пассажира")
    @Disabled("not implemented")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByEmployeePosition() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeePositionSet(Set.of(testPosition3.getId()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .header("Authorization", UUID.randomUUID())
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getId()).isEqualTo(actual.content.get(0).getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по департаменту пассажира")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByEmployeeDepartment() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeDepartmentSet(Set.of(department3.getId()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(actual.content.get(0).getId()).isIn(request4.getId(), request3.getId());
        assertThat(actual.content.get(1).getId()).isIn(request4.getId(), request3.getId());
    }
    
    @Test
    @DisplayName("Проверка содержимого лимитов")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getRequestByEmployeeDepartmentAndCheckLimits() throws Exception {
        var requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setEmployeeDepartmentSet(Set.of(department3.getId()));
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        var response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                               .content(request)
                                               .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON))
                              .andExpect(status().isOk())
                              .andExpect(jsonPath("$.content.length()").value(2))
                              .andExpect(jsonPath("$.content[*].id",
                                                  Matchers.containsInAnyOrder(request3.getId().toString(), request4.getId().toString())))
                              .andExpect(jsonPath("$.content[*].humanReadableLimitId", Matchers.containsInAnyOrder(limit3.getHumanReadableId(),
                                                                                                                   limit4.getHumanReadableId())));
        
        assertThat(response).isNotNull();
    }
    
    @Test
    @DisplayName("Поиск заявок с пустыми условиями")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_searchRequestEmptyFilter() throws Exception {
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
    }
    
    //personal search
    @Test
    @DisplayName("Поиск заявок личного транспорта по характеру деятельности сотрудника")
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    void test_getPersonalRequestByItinerantType() throws Exception {
        RequestForPersonalReportDTO requestSearchDTO = new RequestForPersonalReportDTO();
        ItinerantType full = ItinerantType.FULL;
        requestSearchDTO.setEmployeeItinerantTypeSet(Set.of(full));
        requestSearchDTO.setOrganizationId(organization1.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/personal_report", organization1.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PersonalContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                        new TypeReference<>() {
                                                        });
        
        assertThat(actual.content).hasSize(1);
        
        PersonalResponseDTO personalResponseDTO = actual.content.get(0);
        assertThat(request1.getId()).isEqualTo(personalResponseDTO.getId());
        assertThat(full).hasToString(personalResponseDTO.getPassenger().getItinerantType());
    }
    
    @Test
    @DisplayName("Поиск заявок личного транспорта по месту возникновения затрат - Успешно")
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    void test_getPersonalRequestByMvz() throws Exception {
        RequestForPersonalReportDTO requestSearchDTO = new RequestForPersonalReportDTO();
        requestSearchDTO.setCostCenter("9900L11040");
        requestSearchDTO.setOrganizationId(organization1.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        UUID organizationId = organization1.getId();
        assertNotNull(organizationId);
        ResultActions response = mockMvc.perform(post(String.format("/%s/personal_report", organizationId))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PersonalContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                        new TypeReference<>() {
                                                        });
        
        assertThat(actual.content).hasSize(1);
        PersonalResponseDTO personalResponseDTO = actual.content.get(0);
        
        assertThat(request1.getId()).isEqualTo(personalResponseDTO.getId());
        assertThat(requestSearchDTO.getCostCenter()).isEqualTo(personalResponseDTO.getCostCenter());
    }
    
    @Test
    @DisplayName("Поиск заявок личного транспорта c {orgid} по месту возникновения затрат - Ошибка доступа")
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    void test_getPersonalRequestOrgByMvz() throws Exception {
        RequestForPersonalReportDTO requestSearchDTO = new RequestForPersonalReportDTO();
        requestSearchDTO.setCostCenter("9900L11040");
        UUID randomOrganizationId = UUID.randomUUID();
        requestSearchDTO.setOrganizationId(randomOrganizationId);
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        mockMvc.perform(post(String.format("/%s/personal_report", randomOrganizationId))
                                .content(request)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                .header("Authorization", UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = USER1_ID_STR, roles = ROLE_STR)
    @DisplayName("Получение заявки с компенсацией личного транспорта по id")
    void test_getPersonalRequestById() throws Exception {
        request1.setStatus(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name());
        request1.setOrderPaymentFormationStartDate(request1.getDesiredDate());
        requestRepository.saveAndFlush(request1);
        RequestMessage requestMessage = requestMapper.toMessage(request1);
        requestInput.accept(MessageBuilder.withPayload(requestMessage).build());
        
        RequestForPersonalReportDTO requestSearchDTO = new RequestForPersonalReportDTO();
        requestSearchDTO.setRequestHumanId(request1.getHumanReadableId());
        requestSearchDTO.setOrganizationId(organization1.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/personal_report", organization1.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PersonalContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                        new TypeReference<>() {
                                                        });
        
        assertNotNull(actual.getContent());
        List<PaymentDataDTO> paymentDataList = actual.getContent().get(0).getPaymentDataList();
        assertNotNull(paymentDataList);
        assertEquals(3, paymentDataList.size());
        
        Optional<PaymentDataDTO> mainPayment = paymentDataList.stream().
                                                              filter(payment -> PaymentTypeCode.CODE_4661.equals(payment.getPaymentTypeCode())).
                                                              findFirst();
        assertEquals(2400_00, mainPayment.get().getPaymentPrice());
        
        Optional<PaymentDataDTO> optionalPayment = paymentDataList.stream().
                                                                  filter(payment -> PaymentTypeCode.CODE_4665.equals(payment.getPaymentTypeCode())).
                                                                  findFirst();
        assertEquals(500_00, optionalPayment.get().getPaymentPrice());
    }
    
    //public search
    @Test
    @DisplayName("Поиск заявок по месту возникновения затрат (общественный)")
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    void test_getPublicRequestByMvz() throws Exception {
        RequestForPublicReportDTO requestSearchDTO = new RequestForPublicReportDTO();
        requestSearchDTO.setCostCenter("9900L11040");
        requestSearchDTO.setOrganizationId(organization2.getId());
        requestSearchDTO.setPublicTransportType(Set.of(CITY_BUS));
        requestSearchDTO.setCompensationType(Set.of(CITY_TRIP_COMPENSATION));
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/public_report", organization2.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER2_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PublicContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                      new TypeReference<>() {
                                                      });
        
        assertThat(actual.content).hasSize(1);
        PublicResponseDTO publicResponseDTO = actual.content.get(0);
        
        assertThat(request2.getId()).isEqualTo(publicResponseDTO.getId());
        assertThat(requestSearchDTO.getCostCenter()).isEqualTo(publicResponseDTO.getCostCenter());
    }
    
    @Test
    @DisplayName("Фильтр заявок по дате создания (общественный)")
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    void test_getPublicRequestByCreationDate() throws Exception {
        RequestForPublicReportDTO requestSearchDTO = new RequestForPublicReportDTO();
        requestSearchDTO.setPublicTransportType(Set.of(CITY_BUS));
        requestSearchDTO.setCompensationType(Set.of(CITY_TRIP_COMPENSATION));
        requestSearchDTO.setCreationDate(RequestReportDTO.DateRange.builder()
                                                                   .start(request2.getCreationTime().minusDays(1))
                                                                   .end(request2.getCreationTime().plusDays(1))
                                                                   .build());
        requestSearchDTO.setOrganizationId(organization2.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/public_report", organization2.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER2_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PublicContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                      new TypeReference<>() {
                                                      });
        
        assertThat(actual.content).hasSize(1);
        PublicResponseDTO publicResponseDTO = actual.content.get(0);
        
        assertThat(request2.getId()).isEqualTo(publicResponseDTO.getId());
    }
    
    
    @Test
    @DisplayName("Поиск заявок по типу компенсации")
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    void test_getPublicRequestByCompensation() throws Exception {
        publicTariffRepository.saveAndFlush(publicTariff);
        RequestForPublicReportDTO requestSearchDTO = new RequestForPublicReportDTO();
        PublicCompensationType cityTripCompensation = CITY_TRIP_COMPENSATION;
        requestSearchDTO.setCompensationType(Set.of(cityTripCompensation));
        requestSearchDTO.setPublicTransportType(Set.of(CITY_BUS));
        requestSearchDTO.setOrganizationId(organization2.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/public_report", organization2.getId())).content(request)
                                                                                                                .with(jwt().jwt(builder -> builder.jti(USER2_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                                .contentType(
                                                                                                                        MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PublicContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                      new TypeReference<>() {
                                                      });
        
        assertThat(actual.content).hasSize(1);
        PublicResponseDTO publicResponseDTO = actual.content.get(0);
        assertNotNull(publicResponseDTO.getTransportCompensation());
        TransportCompensationDTO payment = publicResponseDTO.getTransportCompensation().stream().
                                                            filter(tr -> PaymentTypeCode.CODE_4666.equals(tr.getPaymentTypeCode())).
                                                            findFirst().orElse(null);
        assertNotNull(payment);
        assertThat(request2.getId()).isEqualTo(publicResponseDTO.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по желаемой дате поездки")
    @WithMockUser(username = USER2_ID_STR, roles = ROLE_STR)
    void test_getPublicRequestByDesiredDate() throws Exception {
        LocalDateTime now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).toLocalDate().atStartOfDay();
        Map<String, Object> interval = new HashMap<>();
        interval.put("end", now.plusDays(1).toInstant(ZoneOffset.UTC));
        
        Map<String, Object> requestObject = new HashMap<>();
        requestObject.put("desiredDate", interval);
        requestObject.put("organizationId", organization2.getId());
        String request = objectMapper.writeValueAsString(requestObject);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/public_report", organization2.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER2_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PublicContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                      new TypeReference<>() {
                                                      });
        
        assertThat(actual.content).hasSize(1);
        PublicResponseDTO publicResponseDTO = actual.content.get(0);
        
        assertThat(request2.getId()).isEqualTo(publicResponseDTO.getId());
    }

    @Test
    @DisplayName("Поиск заявок по желаемой дате поездки")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    void test_getCarsharingRequestByDesiredDate() throws Exception {
        var now = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).toLocalDate().atStartOfDay();
        var interval = new HashMap<>();
        interval.put("start", now.toInstant(ZoneOffset.UTC));
        interval.put("end", now.plusDays(1).toInstant(ZoneOffset.UTC));

        var requestObject = new HashMap<>();
        requestObject.put("desiredDate", interval);
        requestObject.put("organizationId", organization5.getId());
        var request = objectMapper.writeValueAsString(requestObject);
        mockMvc.perform(post(String.format("/%s/carsharing_report", organization5.getId()))
                        .content(request)
                        .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(request5.getId().toString()))
                .andExpect(jsonPath("$.content[0].humanReadableId").value(request5.getHumanReadableId()))
                .andExpect(jsonPath("$.content[0].organization").value(organization5.getOfficialName()))
                .andExpect(jsonPath("$.content[0].contractor").value(carsharingContractor.getName()))
                .andExpect(jsonPath("$.content[0].fio").value(testEmployee5.getFIO()))
                .andExpect(jsonPath("$.content[0].personnelNumber").value(testEmployee5.getPersonnelNumber()))
                .andExpect(jsonPath("$.content[0].purpose.id").value(tripPurpose5.getId().toString()))
                .andExpect(jsonPath("$.content[0].purpose.purpose").value(tripPurpose5.getPurpose()))
                .andExpect(jsonPath("$.content[0].status").value(request5.getStatus()))
                .andExpect(jsonPath("$.content[0].coopTrip").value(false))
                .andExpect(jsonPath("$.content[0].expectedTime").value(10))
                .andExpect(jsonPath("$.content[0].expectedDistance").value(1.0))
                .andExpect(jsonPath("$.content[0].expectedCost").value(0.01))
                .andExpect(jsonPath("$.content[0].department.id").value(department5.getId().toString()))
                .andExpect(jsonPath("$.content[0].department.departmentName").value(department5.getDepartmentName()))
                .andExpect(jsonPath("$.content[0].tariff.id").value(carsharingTariff1.getId().toString()))
                .andExpect(jsonPath("$.content[0].tariff.humanReadableId").value(carsharingTariff1.getHumanReadableId()))
                .andExpect(jsonPath("$.content[0].tariff.serviceType").value(carsharingTariff1.getServiceType().name()))
                .andExpect(jsonPath("$.content[0].tariff.active").value(carsharingTariff1.isActive()))
                .andExpect(jsonPath("$.content[0].passengerCount").value(1))
                .andExpect(jsonPath("$.content[0].joinedPassengers").value(""))
                .andExpect(jsonPath("$.content[0].savings").value(false))
                .andExpect(jsonPath("$.content[0].deadlineViolation").value("Нет"))
                .andExpect(jsonPath("$.content[0].contractNumber").value("test"))

                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20))
                .andExpect(jsonPath("$.pageable.sort[0].direction").value("DESC"))
                .andExpect(jsonPath("$.pageable.sort[0].property").value("creationTime"))
                .andExpect(jsonPath("$.pageable.sort[0].ignoreCase").value(false))
                .andExpect(jsonPath("$.pageable.sort[0].nullHandling").value("NATIVE"))
                .andExpect(jsonPath("$.pageable.sort[0].descending").value(true))
                .andExpect(jsonPath("$.pageable.sort[0].ascending").value(false))
                .andExpect(jsonPath("$.pageable.offset").value(0))
                .andExpect(jsonPath("$.pageable.unpaged").value(false))
                .andExpect(jsonPath("$.pageable.paged").value(true))

                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.size").value(20))

                .andExpect(jsonPath("$.sort[0].direction").value("DESC"))
                .andExpect(jsonPath("$.sort[0].property").value("creationTime"))
                .andExpect(jsonPath("$.sort[0].ignoreCase").value(false))
                .andExpect(jsonPath("$.sort[0].nullHandling").value("NATIVE"))
                .andExpect(jsonPath("$.sort[0].descending").value(true))
                .andExpect(jsonPath("$.sort[0].ascending").value(false))

                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }
    
    @Test
    @DisplayName("Поиск заявок по целям поездки")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    void test_getCarsharingByTripPurposes() throws Exception {
        RequestForCarsharingReportDTO requestSearchDTO = new RequestForCarsharingReportDTO();
        requestSearchDTO.setPurposeSet(Set.of(TripPurposeDTO.builder().id(tripPurpose5.getId()).build()));
        requestSearchDTO.setOrganizationId(organization5.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/carsharing_report", organization5.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        CarsharingContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                          new TypeReference<>() {
                                                          });
        
        assertThat(actual.content).hasSize(1);
        CarsharingResponseDTO carsharingResponseDTO = actual.content.get(0);
        assertThat(carsharingResponseDTO.getPurpose().getId()).isEqualTo(tripPurpose5.getId());
    }
    
    @Test
    @DisplayName("Поиск заявок по перевозчику")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    void test_getCarsharingByContractors() throws Exception {
        RequestForCarsharingReportDTO requestSearchDTO = new RequestForCarsharingReportDTO();
        requestSearchDTO.setContractorSet(Set.of(carsharingContractor.getId()));
        requestSearchDTO.setOrganizationId(organization5.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/carsharing_report", organization5.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER5_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        CarsharingContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                          new TypeReference<>() {
                                                          });
        
        assertThat(actual.content).hasSize(1);
        CarsharingResponseDTO carsharingResponseDTO = actual.content.get(0);
        assertThat(carsharingResponseDTO.getPurpose().getId()).isEqualTo(tripPurpose5.getId());
        assertThat(carsharingResponseDTO.getContractor()).isEqualTo(carsharingContractor.getName());
    }
    
    @Test
    @DisplayName("Проверка постраничного запроса такси с сортировкой по ИД лимита asc")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getPageSortLimitIdAsc() throws Exception {
        RequestForTaxiReportDTO.PageSetting pageSetting = new RequestForTaxiReportDTO.PageSetting();
        pageSetting.setPage(0);
        
        RequestForTaxiReportDTO.SortSetting sortSetting = new RequestForTaxiReportDTO.SortSetting();
        sortSetting.setProperty(RequestSortOption.LIMIT_ID);
        sortSetting.setDirectionAsc(true);
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setSortSetting(sortSetting);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        Limit limit3 = Limit.builder()
                            .id(UUID.randomUUID())
                            .humanReadableId(HUMAN_READABLE_LIMIT_ID_4)
                            .build();
        
        limitRepository.saveAndFlush(limit3);
        
        request3.setLimit(limit3);
        requestRepository.saveAndFlush(request3);
        
        Limit limit4 = Limit.builder()
                            .id(UUID.randomUUID())
                            .humanReadableId(HUMAN_READABLE_LIMIT_ID_3)
                            .build();
        
        limitRepository.saveAndFlush(limit4);
        
        request4.setLimit(limit4);
        requestRepository.saveAndFlush(request4);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        Set hridset = Set.of(actual.content.get(0).getHumanReadableLimitId(), actual.content.get(1).getHumanReadableLimitId());
        assertTrue(hridset.contains(request3.getLimit().getHumanReadableId()));
        assertTrue(hridset.contains(request4.getLimit().getHumanReadableId()));
    }
    
    @Test
    @DisplayName("Проверка постраничного запроса такси с сортировкой по HumanReadableId asc")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getPageHumanReadableIdAsc() throws Exception {
        RequestForTaxiReportDTO.PageSetting pageSetting = new RequestForTaxiReportDTO.PageSetting();
        pageSetting.setPage(0);
        
        RequestForTaxiReportDTO.SortSetting sortSetting = new RequestForTaxiReportDTO.SortSetting();
        sortSetting.setProperty(RequestSortOption.REQUEST_HUMAN_ID);
        sortSetting.setDirectionAsc(true);
        
        RequestForTaxiReportDTO requestSearchDTO = new RequestForTaxiReportDTO();
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setSortSetting(sortSetting);
        requestSearchDTO.setOrganizationId(organization3.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request3.getHumanReadableId()).isEqualTo(actual.content.get(0).getHumanReadableId());
        assertThat(request4.getHumanReadableId()).isEqualTo(actual.content.get(1).getHumanReadableId());
    }
    
    @Test
    @DisplayName("Проверка постраничного запроса групповому трансферу с сортировкой по HumanReadableId asc")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    void test_getPageHumanReadableIdAsc_groupTransfer() throws Exception {
        RequestForGroupTransferReportDTO.PageSetting pageSetting = new RequestForGroupTransferReportDTO.PageSetting();
        pageSetting.setPage(0);
        
        RequestForGroupTransferReportDTO.SortSetting sortSetting = new RequestForGroupTransferReportDTO.SortSetting();
        sortSetting.setProperty(RequestSortOption.REQUEST_HUMAN_ID);
        sortSetting.setDirectionAsc(true);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        requestSearchDTO.setPageSetting(pageSetting);
        requestSearchDTO.setSortSetting(sortSetting);
        requestSearchDTO.setOrganizationId(organization5.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/group_transfer_report", organization5.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request6.getHumanReadableId()).isEqualTo(actual.content.get(0).getHumanReadableId());
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка поиск заявок по наименованию подразделений - 1 уровень")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6 })
    void test_getPageHumanReadableIdDepartamentName(int level) throws Exception {
        
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        switch (level) {
            case 1:
                requestSearchDTO.setDepartment1(List.of("level 1"));
                break;
            case 2:
                requestSearchDTO.setDepartment2(List.of("level 2"));
                break;
            case 3:
                requestSearchDTO.setDepartment3(List.of("level 3"));
                break;
            case 4:
                requestSearchDTO.setDepartment4(List.of("level 4"));
                break;
            case 5:
                requestSearchDTO.setDepartment5(List.of("level 5"));
                break;
            case 6:
                requestSearchDTO.setDepartment6(List.of("level 6"));
                break;
        }
        requestSearchDTO.setOrganizationId(organization5.getId());
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/group_transfer_report", organization5.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER5_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request6.getHumanReadableId()).isEqualTo(actual.content.get(0).getHumanReadableId());
    }
    
    @Test
    @DisplayName("Проверка запроса заявок по группе исполнителей - такси")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getByExecorGroupIdAndWithoutOrganizationTaxi() throws Exception {
        
        request1.setTransportType("TAXI");
        requestRepository.save(request1);
        request2.setTransportType("TAXI");
        requestRepository.save(request2);
        request3.setTransportType("TAXI");
        requestRepository.save(request3);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
        }});
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/taxi_report")
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request1.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(1).getExecutorGroupId());
        
        response = mockMvc.perform(post(String.format("/%s/taxi_report", organization3.getId()))
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
            add(EXECUTOR_GROUP_ID_2);
        }});
        
        request = objectMapper.writeValueAsString(requestSearchDTO);
        
        response = mockMvc.perform(post("/taxi_report")
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(3);
    }
    
    @Test
    @DisplayName("Проверка запроса заявок по группе исполнителей - личный")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getByExecorGroupIdAndWithoutOrganizationPersonal() throws Exception {
        
        request1.setTransportType("PERSONAL");
        requestRepository.save(request1);
        request2.setTransportType("PERSONAL");
        requestRepository.save(request2);
        request3.setTransportType("PERSONAL");
        requestRepository.save(request3);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
        }});
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/personal_report")
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PersonalContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                        new TypeReference<>() {
                                                        });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request1.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(1).getExecutorGroupId());
        
        response = mockMvc.perform(post(String.format("/%s/personal_report", organization3.getId()))
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
            add(EXECUTOR_GROUP_ID_2);
        }});
        
        request = objectMapper.writeValueAsString(requestSearchDTO);
        
        response = mockMvc.perform(post("/personal_report")
                                           .content(request)
                                           .header("Authorization", UUID.randomUUID())
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(3);
    }
    
    @Test
    @DisplayName("Проверка запроса заявок по группе исполнителей - общественный")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getByExecorGroupIdAndWithoutOrganizationPublic() throws Exception {
        
        request1.setTransportType("PUBLIC");
        requestRepository.save(request1);
        request2.setTransportType("PUBLIC");
        requestRepository.save(request2);
        request3.setTransportType("PUBLIC");
        requestRepository.save(request3);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
        }});
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/public_report")
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        PublicContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                      new TypeReference<>() {
                                                      });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request1.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(1).getExecutorGroupId());
        
        response = mockMvc.perform(post(String.format("/%s/public_report", organization3.getId()))
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
            add(EXECUTOR_GROUP_ID_2);
        }});
        
        request = objectMapper.writeValueAsString(requestSearchDTO);
        
        response = mockMvc.perform(post("/public_report")
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(3);
    }
    
    @Test
    @DisplayName("Проверка запроса заявок по группе исполнителей - каршеринг")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    void test_getByExecorGroupIdAndWithoutOrganizationCarsharing() throws Exception {
        
        request1.setTransportType("CARSHARING");
        requestRepository.save(request1);
        request2.setTransportType("CARSHARING");
        requestRepository.save(request2);
        request3.setTransportType("CARSHARING");
        requestRepository.save(request3);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
        }});
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/carsharing_report")
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        CarsharingContent actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                          new TypeReference<>() {
                                                          });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request1.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(1).getExecutorGroupId());
        
        response = mockMvc.perform(post(String.format("/%s/carsharing_report", organization3.getId()))
                                           .content(request)
                                           .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
            add(EXECUTOR_GROUP_ID_2);
        }});
        
        request = objectMapper.writeValueAsString(requestSearchDTO);
        
        response = mockMvc.perform(post("/carsharing_report")
                                           .content(request)
                                           .header("Authorization", UUID.randomUUID())
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(3);
    }
    
    @Test
    @DisplayName("Проверка запроса заявок по группе исполнителей - трансфер")
    @WithMockUser(username = USER5_ID_STR, roles = ROLE_STR)
    void test_getByExecorGroupIdAndWithoutOrganizationTransfer() throws Exception {
        
        request1.setTransportType("GROUP_TRANSFER");
        requestRepository.save(request1);
        request2.setTransportType("GROUP_TRANSFER");
        requestRepository.save(request2);
        request3.setTransportType("GROUP_TRANSFER");
        requestRepository.save(request3);
        
        RequestForGroupTransferReportDTO requestSearchDTO = new RequestForGroupTransferReportDTO();
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
        }});
        
        var request = objectMapper.writeValueAsString(requestSearchDTO);
        
        ResultActions response = mockMvc.perform(post("/group_transfer_report")
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER5_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        
        Content actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                                new TypeReference<>() {
                                                });
        
        assertThat(actual.content).hasSize(2);
        assertThat(request1.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(1).getExecutorGroupId());
        
        response = mockMvc.perform(post(String.format("/%s/group_transfer_report", organization3.getId()))
                                           .content(request)
                                           .header("Authorization", UUID.randomUUID())
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(1);
        assertThat(request3.getExecutorGroupId()).isEqualTo(actual.content.get(0).getExecutorGroupId());
        
        requestSearchDTO.setExecutorGroupIds(new ArrayList<>() {{
            add(EXECUTOR_GROUP_ID_1);
            add(EXECUTOR_GROUP_ID_2);
        }});
        
        request = objectMapper.writeValueAsString(requestSearchDTO);
        
        response = mockMvc.perform(post("/group_transfer_report")
                                           .content(request)
                                           .header("Authorization", UUID.randomUUID())
                                           .contentType(MediaType.APPLICATION_JSON))
                          .andExpect(status().isOk());
        
        actual = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(Charsets.UTF_8),
                                        new TypeReference<>() {
                                        });
        
        assertThat(actual.content).hasSize(3);
    }
}