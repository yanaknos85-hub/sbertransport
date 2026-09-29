package ru.sberbank.ditsib.transport.request.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcDiscoveryClientAutoConfiguration;
import org.instancio.Instancio;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.magenta.model.MagentaSharedRequestResponseDTO;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.RestTemplateConfig;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveTripRequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TripRatingMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.UpdateTripRequestMessage;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.grpc.CorporateDocumentValidationGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.DurationRequestCheckGrpcClient;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForTaxiServiceImpl;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;
import ru.sberbank.ditsib.transport.request.service.taxiprice.CityMobilPriceServiceImpl;
import ru.sberbank.ditsib.transport.request.service.taxiprice.UberPriceServiceImpl;
import ru.sberbank.ditsib.transport.request.service.taxiprice.YandexPriceServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SuppressWarnings({ "OptionalGetWithoutIsPresent" })
@Transactional
@Slf4j
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера заявок. Временная зона")
@SpringBootTest(classes = RequestApplication.class, properties = { "default-timezone=GMT+10" })
@MockitoBean(types = { JwtDecoder.class, GrpcDiscoveryClientAutoConfiguration.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(RestTemplateConfig.class)
class RequestControllerTimeZoneTest extends SharedTest {
    
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();
    
    @MockitoBean
    private SrmService srmService;

    @MockitoBean
    private EasupGrpcService easupGrpcService;

    @MockitoBean
    private RequestChecksGrpcService requestChecksGrpcService;
    
    @MockitoBean
    private DurationRequestCheckGrpcClient durationRequestCheckGrpcClient;
    
    @Autowired
    private EntityDTOMapper mapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private RequestForPublicRepository publicRepository;
    
    @Autowired
    private WaypointRepository waypointRepository;
    
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    
    @Autowired
    private PositionRepository positionRepository;
    
    @Autowired
    private UpdateRequestRepository updateRequestRepository;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private RequestControllerService requestControllerService;
    
    @MockitoBean("frequentlyAddressOutput")
    private OutputBridge frequentlyAddressOutput;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private TaxiTripRepository taxiTripRepository;
    
    @MockitoBean
    private RestTemplate restTemplate;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    
    @MockitoBean
    private CityMobilPriceServiceImpl cityMobilPriceServiceImpl;
    
    @MockitoBean
    private YandexPriceServiceImpl yandexPriceServiceImpl;
    
    @MockitoBean
    private UberPriceServiceImpl uberPriceServiceImpl;
    
    @MockitoBean(name = "requestOutput")
    private OutputBridge requestOutput;

    @MockitoBean(name = "updateRequestOutput")
    private OutputBridge updateRequestOutput;
    
    @MockitoBean("requestRatingOutput")
    private OutputBridge requestRatingOutput;

    @MockitoBean
    private CorporateDocumentValidationGrpcClient corporateDocumentValidationGrpcClient;
    
    @Autowired
    private DelegateRepository delegateRepository;
    
    @Autowired
    @Qualifier("approveRequestInput")
    private Consumer<Message<ApproveTripRequestMessage>> approveRequestInput;
    
    @Autowired
    private CheckinSettingsService checkinSettingsService;
    
    @MockitoBean
    private PersonalCarDataResolver personalCarDataResolver;
    
    private final UUID regionId = UUID.randomUUID();
    
    @Autowired
    private RequestForTaxiRepository taxiRequestRepository;
    
    @Autowired
    private DepLimitRepository depLimitRepository;
    
    @Autowired
    private TransportCompensationRepository transportCompensationRepository;
    
    @Autowired
    private ContractorRepository contractorRepository;
    
    @Autowired
    private TaxiTariffRepository taxiTariffRepository;
    
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    
    @MockitoSpyBean
    private ReservationService reservationService;
    
    @Mock
    private Clock clock;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    private Function<OldReserve.LimitReservationRequest, OldReserve.LimitReservationResponse> limitReservationFunction;
    
    @BeforeEach
    void setup() throws IOException {
        AuthorizeUtils.authorize(roleCheckService);
        final var grpc = new LimitReservationServiceGrpc.LimitReservationServiceImplBase() {
            
            @Override
            public void limitReservation(
                    OldReserve.LimitReservationRequest request, StreamObserver<OldReserve.LimitReservationResponse> responseObserver
                                        ) {
                    try {
                        if (limitReservationFunction != null) {
                            responseObserver.onNext(limitReservationFunction.apply(request));
                        }
                        responseObserver.onCompleted();
                    } catch (Exception e) {
                        responseObserver.onError(e);
                    }
            }
        };
        final var channel = grpcCleanupExtension.addService(grpc);
        ((ReservationServiceImpl) reservationService).setStub(LimitReservationServiceGrpc.newBlockingStub(channel));
    }
    
    private final Clock fixedClock =
            Clock.fixed(LocalDateTime.now(ZoneId.of("UTC")).plusMinutes(20).toInstant(ZoneOffset.UTC),
                        ZoneId.of("UTC"));
    
    @BeforeEach
    public void setUp() {
        testEmployee1.setUserId(userId);
        testEmployee2.setUserId(userId2);
        testEmployee3.setUserId(userId3);
        testEmployee4.setUserId(userId4);
        testEmployee6.setUserId(userId6);
        testEmployee7.setUserId(userId7);
        testEmployee8.setUserId(userId8);
        
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        testEmployee6.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_6);
        testEmployee7.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_7);
        testEmployee8.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_8);

        var contractor = Contractor.builder()
                .id(CONTRACTOR_ID_1)
                .name("name")
                .contractorName("name")
                .contractorRusName("rusname")
                .integrationEmail("aaa@bbb.ru")
                .build();
        contractorRepository.save(contractor);

        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request1.setContractorId(contractor.getId());
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request2.setContractorId(contractor.getId());
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request3.setContractorId(contractor.getId());
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        publicRequest.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_5);
        request6.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_6);
        request7.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_7);
        request8.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_8);
        
        tripPurposeRepository.save(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        tripPurposeRepository.save(TripPurpose.builder().id(purposeId2).purpose(PURPOSE_DESCRIPTION_2).build());
        
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(department1);
        positionRepository.save(testPosition1);
        positionRepository.save(testPosition2);
        positionRepository.save(testPosition3);
        employeeRepository.saveAndFlush(testEmployee1);
        testEmployee2.setSupervisorId(testEmployee1.getId());
        employeeRepository.save(testEmployee2);
        testEmployee3.setSupervisorId(testEmployee2.getId());
        employeeRepository.saveAndFlush(testEmployee3);
        department1.setDepartmentHead(testEmployee2.getId());
        departmentRepository.saveAndFlush(department1);
        employeeRepository.save(testEmployee4);
        employeeRepository.save(testEmployee6);
        employeeRepository.save(testEmployee7);
        employeeRepository.save(testEmployee8);
        
        var taxiTariff = TaxiTariff.builder()
                                          .id(TARIFF_ID_1)
                                          .contractorId(contractor.getId())
                                          .workGroup("Work group")
                                          .regionId(UUID.randomUUID())
                                          .humanReadableId("TT-123-23")
                                          .triggerTime(TRIGGER_TIME)
                                          .departmentId(department1.getId())
                                          .taxiClass(TaxiClass.ECONOMY)
                                          .rideCostPerKm(1)
                                          .rideCostPerMin(2)
                                          .waitCostPerMin(3)
                                          .serviceType(TransportServiceType.EMPLOYEE_TRANSPORTATION)
                                          .transportType(TransportTypeEnum.TAXI)
                                          .build();
        taxiTariffRepository.save(taxiTariff);
        
        personalTariffRepository.saveAndFlush(
                PersonalTariff.builder()
                              .id(TARIFF_ID_2)
                              .humanReadableId("PT-123-23")
                              .departmentId(department1.getId())
                              .transportType(TransportTypeEnum.CARSHARING)
                              .rideCostPerKm(1)
                              .rideCostPerMin(2)
                              .build());
        
        var regionDto = RegionDto.builder().code("1").id(UUID.randomUUID()).name("moscow").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class)))
                .thenReturn(deletedAnswer());
        doNothing().when(reservationService).cancel(any());
        when(regionDataResolver.getRegionBranch(any(WaypointDTO.class))).thenReturn(List.of(
                RegionDto.builder()
                         .id(UUID.randomUUID())
                         .parentId(UUID.randomUUID())
                         .timeZone("+3")
                         .name("name")
                         .code("123")
                         .build()));
    }
    
    @Test
    @DisplayName("Получение несуществующей заявки")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    void test_getNonExistent() throws Exception {
        mockMvc.perform(
                       get("/" + UUID.randomUUID())
                               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("Обновление заявки (с одинаковыми точками поездки идущими подряд)")
    @Order(6)
    void test_editRequest_sameAddressOneByOne() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        request1.setOutcomeTariffId(request1.getTariffId());
        request1 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request1), testEmployee1);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        
        var response = mockMvc.perform(
                                      get("/" + request1.getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        RequestDTO expected = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                     RequestDTO.class);
        Duration updatedWaitTime = Duration.ofMillis(15 * 60 * 1000);
        expected.getExpected().setCost(1111d);
        var wp = expected.getExpected().getWaypoints().get(1);
        var waitedWP = WaypointDTO.builder()
                                  .waitTime(updatedWaitTime)
                                  .absenceReason(wp.getAbsenceReason())
                                  .active(wp.isActive())
                                  .building(wp.getBuilding())
                                  .checkinAutomatic(wp.isCheckinAutomatic())
                                  .checkinManual(wp.isCheckinManual())
                                  .checkinOnlyManual(wp.isCheckinOnlyManual())
                                  .city(wp.getCity())
                                  .country(wp.getCountry())
                                  .existInVspGosbTbRegistry(wp.isExistInVspGosbTbRegistry())
                                  .house(wp.getHouse())
                                  .latitude(wp.getLatitude())
                                  .street(wp.getStreet())
                                  .structure(wp.getStructure())
                                  .longitude(wp.getLongitude())
                                  .region(wp.getRegion())
                                  .build();
        expected.getExpected().getWaypoints().set(1, waitedWP);
        expected.getExpected().getWaypoints().set(0, WaypointDTO.builder()
                                                                .latitude(address3.getLatitude())
                                                                .longitude(address3.getLongitude()).build());
        expected.getExpected().getWaypoints().set(1, WaypointDTO.builder()
                                                                .latitude(address3.getLatitude())
                                                                .longitude(address3.getLongitude()).build());
        var request = objectMapper.writeValueAsString(expected);
        
        mockMvc.perform(put("/" + request1.getId())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("The address by position 1 duplicates the previous one"));
    }
    
    @Test
    @DisplayName("Добавление заявки (с одинаковыми точками поездки идущими подряд)")
    @Order(7)
    void test_addRequest_sameAddressOneByOne() throws Exception {
        request1.setAuthor(testEmployee1);
        request1.setPassenger(testEmployee2);
        request1.setPurpose(TripPurpose.builder().id(purposeId1).build());
        
        Waypoint startPoint = request1.getWaypoints().getFirst();
        Address from = startPoint.getAddress();
        Address addressTo = deepCopy(from);
        
        Waypoint waypointTo = new Waypoint();
        waypointTo.setAddress(addressTo);
        
        request1.getWaypoints().clear();
        request1.getWaypoints().add(startPoint);
        request1.getWaypoints().add(waypointTo);
        request1.getWaypoints().getFirst().setWaitTime(Duration.ofSeconds(111));
        request1.setTimeZone("GMT+3");
        
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        var requestDTO = mapper.requestForTaxiToDTO(request1);
        var request = objectMapper.writeValueAsString(requestDTO);
        
        mockMvc.perform(post("/")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(request))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.message").value("The address by position 1 duplicates the previous one"));
        
    }

    @Test
    @DisplayName("Получение заявки для общественного адреса по идентификатору")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    @Order(11)
    void test_getPublicRequest() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.getTransportCompensation().forEach(elt -> elt.setRequest(publicRequest));
        publicRepository.save(publicRequest);
        
        var response = mockMvc.perform(get("/" + publicRequest.getId())
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        
        Map<String, Object> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                            new TypeReference<>() {
                                                            });
        
        assertEquals(publicRequest.getId().toString(), actual.get("id"));
        
        Map<String, Object> expected = (Map<String, Object>) actual.get("expected");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) expected.get("waypoints");
        
        assertEquals(address1.getLatitude(), waypoints.getFirst().get("latitude"));
        assertEquals(address1.getLongitude(), waypoints.getFirst().get("longitude"));
        assertEquals(address2.getLatitude(), waypoints.get(1).get("latitude"));
        assertEquals(address2.getLongitude(), waypoints.get(1).get("longitude"));
        assertEquals(address3.getLatitude(), waypoints.get(2).get("latitude"));
        assertEquals(address3.getLongitude(), waypoints.get(2).get("longitude"));
        
        Map<String, Object> author = (Map<String, Object>) actual.get("author");
        assertEquals(publicRequest.getAuthor().getId().toString(), author.get("id"));
        assertEquals(publicRequest.getAuthor().getDepartment().getId().toString(), author.get("departmentId"));
        Map<String, Object> passenger = (Map<String, Object>) actual.get("passenger");
        assertEquals(publicRequest.getPassenger().getId().toString(), passenger.get("id"));
        assertEquals(publicRequest.getPassenger().getDepartment().getId().toString(), passenger.get("departmentId"));
        
        assertNull(passenger.get("approvedBy"));
        List<Map<String, Object>> transportCompensation = (List<Map<String, Object>>) actual.get("transportCompensation");
        Map<String, Object> compensationType = (Map<String, Object>) transportCompensation.getFirst().get("compensationType");
        assertEquals(PublicCompensationType.CITY_TRIP_COMPENSATION.name(), compensationType.get("name"));
    }
    
    @Test
    @DisplayName("Получение заявки для личного транспорта по идентификатору")
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    @Order(12)
    void test_getPersonalRequest() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        //sharedRequestRepository.save(magentaSharedRequest);
        requestRepository.save(request4);
        
        var response = mockMvc.perform(get("/" + request4.getId())
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        
        Map<String, Object> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                            new TypeReference<>() {
                                                            });
        
        assertEquals(request4.getId().toString(), actual.get("id"));
        
        Map<String, Object> expected = (Map<String, Object>) actual.get("expected");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) expected.get("waypoints");
        
        assertEquals(address1.getLatitude(), waypoints.getFirst().get("latitude"));
        assertEquals(address1.getLongitude(), waypoints.getFirst().get("longitude"));
        assertEquals(address2.getLatitude(), waypoints.get(1).get("latitude"));
        assertEquals(address2.getLongitude(), waypoints.get(1).get("longitude"));
        assertEquals(address3.getLatitude(), waypoints.get(2).get("latitude"));
        assertEquals(address3.getLongitude(), waypoints.get(2).get("longitude"));
        
        Map<String, Object> author = (Map<String, Object>) actual.get("author");
        assertEquals(request4.getAuthor().getId().toString(), author.get("id"));
        assertEquals(request4.getAuthor().getDepartment().getId().toString(), author.get("departmentId"));
        Map<String, Object> passenger = (Map<String, Object>) actual.get("passenger");
        assertEquals(request4.getPassenger().getId().toString(), passenger.get("id"));
        assertEquals(request4.getPassenger().getDepartment().getId().toString(), passenger.get("departmentId"));
        
        assertNull(passenger.get("approvedBy"));
    }
    
    @Test
    @DisplayName("Получение своей незавершенной заявки")
    void test_getSelfSearch() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        //sharedRequestRepository.save(magentaSharedRequest);
        requestRepository.save(request4);
        
        boolean terminal = false;
        ResultActions response = mockMvc.perform(get("/self/non_terminal?size=5&page=0&sort=humanReadableId,desc")
                                                         .with(jwt().jwt(builder -> builder.jti(USER4_ID).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andExpect(status().isOk());
        
        Map<String, Object> actual =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        
        List<Object> contentList = (List<Object>) actual.get("content");
        assertNotNull(contentList);
        assertEquals(1, contentList.size());
        Map<String, Object> content = (Map<String, Object>) contentList.getFirst();
        
        assertEquals(request4.getId().toString(), content.get("id"));
        assertEquals(request4.getStatus().name(), content.get("status"));
        assertEquals(request4.getStatus().isTerminal(), terminal);
        
        Map<String, Object> expected = (Map<String, Object>) content.get("expected");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) expected.get("waypoints");
        
        assertEquals(address1.getLatitude(), waypoints.getFirst().get("latitude"));
        assertEquals(address1.getLongitude(), waypoints.getFirst().get("longitude"));
        assertEquals(address2.getLatitude(), waypoints.get(1).get("latitude"));
        assertEquals(address2.getLongitude(), waypoints.get(1).get("longitude"));
        assertEquals(address3.getLatitude(), waypoints.get(2).get("latitude"));
        assertEquals(address3.getLongitude(), waypoints.get(2).get("longitude"));
        
        Map<String, Object> author = (Map<String, Object>) content.get("author");
        assertEquals(request4.getAuthor().getId().toString(), author.get("id"));
        assertEquals(request4.getAuthor().getDepartment().getId().toString(), author.get("departmentId"));
        Map<String, Object> passenger = (Map<String, Object>) content.get("passenger");
        assertEquals(request4.getPassenger().getId().toString(), passenger.get("id"));
        assertEquals(request4.getPassenger().getDepartment().getId().toString(), passenger.get("departmentId"));
        
        assertNull(passenger.get("approvedBy"));
    }
    
    @Test
    @DisplayName("Получение своей незавершенной заявки")
    void test_getRequestByPersonalAndNonTerminalStatus() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        //sharedRequestRepository.save(magentaSharedRequest);
        requestRepository.save(request4);
        
        boolean terminal = false;
        
        String body = "{   \"pageSetting\": {\"page\": 0, \"size\": 7}," +
                      "    \"sortSetting\": {\"directionAsc\": \"false\"}," +
                      "    \"transportTypeEnum\": \"PERSONAL\"" +
                      "}";
        ResultActions response = mockMvc.perform(post("/self/non_terminal")
                                                         .with(jwt().jwt(builder -> builder.jti(USER4_ID).claim("roles", "ROLE_USER")))
                                                         .content(body)
                                                         .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andExpect(status().isOk());
        
        Map<String, Object> actual =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        
        List<Object> contentList = (List<Object>) actual.get("content");
        assertNotNull(contentList);
        assertEquals(1, contentList.size());
        Map<String, Object> content = (Map<String, Object>) contentList.getFirst();
        
        assertEquals(request4.getId().toString(), content.get("id"));
        assertEquals(request4.getStatus().name(), content.get("status"));
        assertEquals(request4.getStatus().isTerminal(), terminal);
        
        Map<String, Object> expected = (Map<String, Object>) content.get("expected");
        List<Map<String, Object>> waypoints = (List<Map<String, Object>>) expected.get("waypoints");
        
        assertEquals(address1.getLatitude(), waypoints.getFirst().get("latitude"));
        assertEquals(address1.getLongitude(), waypoints.getFirst().get("longitude"));
        assertEquals(address2.getLatitude(), waypoints.get(1).get("latitude"));
        assertEquals(address2.getLongitude(), waypoints.get(1).get("longitude"));
        assertEquals(address3.getLatitude(), waypoints.get(2).get("latitude"));
        assertEquals(address3.getLongitude(), waypoints.get(2).get("longitude"));
        
        Map<String, Object> author = (Map<String, Object>) content.get("author");
        assertEquals(request4.getAuthor().getId().toString(), author.get("id"));
        assertEquals(request4.getAuthor().getDepartment().getId().toString(), author.get("departmentId"));
        Map<String, Object> passenger = (Map<String, Object>) content.get("passenger");
        assertEquals(request4.getPassenger().getId().toString(), passenger.get("id"));
        assertEquals(request4.getPassenger().getDepartment().getId().toString(), passenger.get("departmentId"));
        
        assertNull(passenger.get("approvedBy"));
    }
    
    @Test
    @DisplayName("Получение своей завершенной заявки")
    @WithMockUser(username = USER4_ID, roles = "GUEST")
    void test_getRequestByPersonalAndTerminalStatus() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        requestRepository.save(request4);
        
        ResultActions response = mockMvc.perform(get("/self/terminal?size=5&page=0&sort=humanReadableId,desc")
                                                         .with(jwt().jwt(builder -> builder.jti(USER4_ID).claim("roles", "ROLE_USER")))
                                                         .contentType(MediaType.APPLICATION_JSON_VALUE))
                                        .andExpect(status().isOk());
        
        Map<String, Object> actual =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       new TypeReference<>() {
                                       });
        
        List<Object> contentList = (List<Object>) actual.get("content");
        assertEquals(0, contentList.size());
    }
    
    @Test
    @DisplayName("Получение заявки по идентификатору и типу транспорта")
    @Order(14)
    @Transactional
    void test_getRequestWithTransportType() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        departmentRepository.save(department1);
        request1.setAuthor(employeeRepository.findById(testEmployee1.getId()).get());
        request1.setPassenger(employeeRepository.findById(testEmployee2.getId()).get());
        request1.setPurpose(TripPurpose.builder().id(purposeId1).build());
        request1 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request1), testEmployee1);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        GetRequestDTO expected = requestControllerService.get(request1.getId());
        
        var response = mockMvc.perform(
                                      get("/TAXI/" + request1.getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        RequestDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                   RequestDTO.class);
        
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getTimeZone(), request1.getTimeZone());
        assertEquals(expected.getTimeZone(), actual.getTimeZone());
        assertEquals(address1.getLatitude(), actual.getExpected().getWaypoints().getFirst().getLatitude());
        assertEquals(address1.getLongitude(), actual.getExpected().getWaypoints().getFirst().getLongitude());
        assertEquals(address2.getLatitude(), actual.getExpected().getWaypoints().get(1).getLatitude());
        assertEquals(address2.getLongitude(), actual.getExpected().getWaypoints().get(1).getLongitude());
        assertEquals(address3.getLatitude(), actual.getExpected().getWaypoints().get(2).getLatitude());
        assertEquals(address3.getLongitude(), actual.getExpected().getWaypoints().get(2).getLongitude());
        assertEquals(expected.getApprovalState(), actual.getApprovalState());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getAuthor().id(), testEmployee1.getId());
        assertEquals(expected.getPassenger().id(), actual.getPassenger().id());
        assertEquals(expected.getAuthor().organizationId(), testEmployee1.getDepartment().getOrganization().getId());
        assertEquals(expected.getAuthor().departmentId(), testEmployee1.getDepartment().getId());
        assertEquals(expected.getAuthor().organizationId(), testEmployee1.getDepartment().getOrganization().getId());
        assertEquals(expected.getPassenger().organizationId(), actual.getPassenger().organizationId());
        assertEquals(expected.getAuthor().organizationId(), testEmployee1.getDepartment().getOrganization().getId());
        assertEquals(expected.getPassenger().departmentId(), actual.getPassenger().departmentId());
        assertEquals(expected.getAuthor().organizationId(), testEmployee1.getDepartment().getOrganization().getId());
    }
    
    @Test
    @DisplayName("Получение всех заявок")
    @Order(15)
    void test_getAllRequests() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request2.setTaxiTrip(taxiTrip2);
        requestRepository.save(request2);
        assertEquals(2, requestRepository.findAll().size());
        
        var response = mockMvc.perform(get("/")
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        assertEquals(2, actual.size());
        assertNotNull(actual.stream().filter(elt -> elt.getId().equals(request1.getId())).findFirst().orElse(null));
        assertNotNull(actual.stream().filter(elt -> elt.getId().equals(request2.getId())).findFirst().orElse(null));
    }
    
    @Test
    @DisplayName("Поиск одобренных заявок")
    @Order(16)
    void test_getApprovedRequests() throws Exception {
        when(srmService.postNewSharedRide(any(SrmRequestDTO.class), eq(null))).thenReturn(Instancio.create(SrmSharedRideDTO.class));
        
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(testEmployee3.getId());
        delegateRecordDTO.setDelegateId(testEmployee1.getId());
        delegateRecordDTO.setTransportType(TransportTypeEnum.SCOOTER);
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);
        
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        
        request1.setDesiredDate(LocalDateTime.now(clock));
        request2.setDesiredDate(LocalDateTime.now(clock));
        request3.setDesiredDate(LocalDateTime.now(clock));
        
        request1 = (RequestForTaxi) requestService.add(null, true, mapper.requestForTaxiToDTO(request1), testEmployee1);
        request2 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request2), testEmployee2);
        request3 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request3), testEmployee3);
        
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        request1.setApprovalState(ApprovalState.APPROVED);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request2.setTaxiTrip(taxiTrip2);
        requestRepository.save(request2);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        request3.setTaxiTrip(taxiTrip3);
        requestRepository.save(request3);
        
        var response = mockMvc.perform(
                                      get("/search?approvedFlag=true")
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andExpect(jsonPath("$.length()").value(1)).andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        
        assertEquals(1,
                     actual.stream()
                           .filter(elt -> elt.getStatus().ordinal() >= TripRequestStatus.TAXI_APPROVED.ordinal())
                           .count());
        taxiRequestRepository.flush();
        
    }
    
    @Test
    @DisplayName("Поиск неодобренных заявок")
    @Order(17)
    void test_getNotApprovedRequests() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        
        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(testEmployee3.getId());
        delegateRecordDTO.setDelegateId(testEmployee1.getId());
        delegateRecordDTO.setTransportType(request2.getTransportType());
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);
        
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        
        request1 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request1), testEmployee1);
        request2 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request2), testEmployee2);
        request3 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request3), testEmployee3);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request2.setTaxiTrip(taxiTrip2);
        requestRepository.save(request2);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        request3.setTaxiTrip(taxiTrip3);
        requestRepository.save(request3);
        
        var response = mockMvc.perform(
                                      get("/search?approvedFlag=false")
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk())
                              .andExpect(jsonPath("$.length()").value(3))
                              .andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        
        assertEquals(3,
                     actual.stream()
                           .filter(elt -> elt.getStatus().ordinal() < TripRequestStatus.TAXI_APPROVED.ordinal())
                           .count());
    }
    
    @Test
    @DisplayName("Поиск неодобренных заявок  назначенных на одобряющего")
    @Order(19)
    void test_getNotApprovedRequestsByApprovedById() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        request1.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(1));
        request2.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(1));
        request3.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusHours(1));
        
        request1 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request1), testEmployee1);
        request2 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request2), testEmployee2);
        request3 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request3), testEmployee3);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request2.setTaxiTrip(taxiTrip2);
        requestRepository.save(request2);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        request3.setTaxiTrip(taxiTrip3);
        requestRepository.save(request3);
        
        var delegate = new Delegate();
        delegate.setStartDate(LocalDate.now(ZoneId.of(ZoneOffset.UTC.getId())).minusDays(2));
        delegate.setEndDate(LocalDate.now(ZoneId.of(ZoneOffset.UTC.getId())).plusDays(2));
        delegate.setSupervisorId(testEmployee1.getId());
        delegate.setTransportType(request2.getTransportType());
        delegate.setDelegateId(testEmployee2.getId());
        delegate.setId(UUID.randomUUID());
        delegateRepository.save(delegate);
        
        var response = mockMvc.perform(
                                      get("/search?approvedFlag=false")
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
//                get("/search?approvedFlag=false&approvedById=" + testEmployee2.getId())// Не понимаю, почему если
//                заявка еще не рассматривалась для согласоваия,  она должна содеожать поле approvedBy
.contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        List<RequestDTO> actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                         new TypeReference<>() {
                                                         });
        assertEquals(3, actual.size());
        
    }
    
    @Test
    @DisplayName("Оценка заявки")
    @Order(24)
    void test_rateRequest() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        //employee2 self approved
        request3.setPassenger(testEmployee2);
        
        request3 = (RequestForTaxi) requestService.add(null, false, mapper.requestForTaxiToDTO(request3), testEmployee3);
        request3.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        taxiTripRepository.saveAndFlush(taxiTrip3);
        request3.setTaxiTrip(taxiTrip3);
        requestRepository.save(request3);
        
        RequestRatingDTO rating = new RequestRatingDTO();
        rating.setRating(3);
        rating.setRatingComment("Nice comment");
        String drawback1 = "drawback11";
        String drawback2 = "drawback12";
        String drawback3 = "drawback13";
        rating.getDrawbacks().addAll(Arrays.asList(drawback1, drawback2, drawback3));
        String advantage1 = "advantage11";
        String advantage2 = "advantage12";
        String advantage3 = "advantage13";
        rating.getAdvantages().addAll(Arrays.asList(advantage1, advantage2, advantage3));
        
        
        var response = mockMvc.perform(
                                      post("/rate/" + request3.getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER2_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(objectMapper.writeValueAsBytes(rating)))
                              .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(), GetRequestDTO.class);
        
        assertEquals(rating.getRating(), actual.getRequestRating().getRating());
        assertEquals(rating.getAdvantages().size(), actual.getRequestRating().getAdvantages().size());
        rating.getAdvantages().forEach(elt -> assertThat(actual.getRequestRating().getAdvantages().contains(elt)));
        assertEquals(rating.getDrawbacks().size(), actual.getRequestRating().getDrawbacks().size());
        rating.getDrawbacks().forEach(elt -> assertThat(actual.getRequestRating().getDrawbacks().contains(elt)));
        
        assertEquals(rating.getRatingComment(), actual.getRequestRating().getRatingComment());
        
        final var tripRatingMessageCaptor = ArgumentCaptor.forClass(TripRatingMessage.class);
        verify(requestRatingOutput).send(tripRatingMessageCaptor.capture());
        var message = tripRatingMessageCaptor.getValue();
        
        assertThat(message.getRequestId()).isEqualTo(request3.getId());
        assertThat(message.getRating()).isEqualTo(rating.getRating());
        assertThat(message.getRatingComment()).isEqualTo(rating.getRatingComment());
        assertThat(message.getAdvantages()).isEqualTo(rating.getAdvantages());
        assertThat(message.getDrawbacks()).isEqualTo(rating.getDrawbacks());
    }
    
    @Test
    @DisplayName("Отмена заявки в на ЛТ")
    @WithMockUser(username = USER4_ID, roles = "GUEST")
    void test_cancelRequest_success() throws Exception {
        request4.setStatus(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS);
        
        CancelDTO cancelDTO = new CancelDTO();
        cancelDTO.setReason("Reason");
        cancelDTO.setCode(234);
        var requestContent = objectMapper.writeValueAsString(cancelDTO);
        
        address1 = addressRepository.save(address1);
        address2 = addressRepository.save(address2);
        address3 = addressRepository.save(address3);
        request4.setPassenger(employeeRepository.save(request4.getPassenger()));
        request4.setAuthor(employeeRepository.save(request4.getAuthor()));
        request4 = requestRepository.save(request4);
        
        mockMvc.perform(put("/cancel/" + request4.getId())
                                .with(jwt().jwt(builder -> builder.jti(USER4_ID).claim("roles", "ROLE_USER")))
                                .content(requestContent)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        Request result = requestService.get(request4.getId()).get();
        assertEquals(TripRequestStatus.PERSONAL_CANCELLED, result.getStatus());
    }

    @Test
    @DisplayName("Поиск заявок (с делегированием)")
    @Order(31)
    void test_getRequestByApprovedById_delegate() throws Exception {
        employeeRepository.saveAndFlush(testEmployee1);
        
        var delegateRecordDTO = new Delegate();
        delegateRecordDTO.setId(UUID.randomUUID());
        delegateRecordDTO.setSupervisorId(testEmployee3.getId());
        delegateRecordDTO.setDelegateId(testEmployee1.getId());
        delegateRecordDTO.setTransportType(request2.getTransportType());
        delegateRecordDTO.setStartDate(LocalDate.now());
        delegateRecordDTO.setEndDate(LocalDate.now().plusMonths(3));
        delegateRepository.save(delegateRecordDTO);
        
        
        request2.setApprovedBy(testEmployee3); //одобрить должен Employee3
        
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip2);
        request2.setTaxiTrip(taxiTrip2);
        requestRepository.save(request2);
        
        var response = mockMvc.perform(
                                      get("/search?approvedById=" + testEmployee1.getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        var actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                            new TypeReference<List<RequestDTO>>() {
                                            });
        
        assertEquals(1, actual.size());
        assertEquals(request2.getId(), actual.getFirst().getId());
        assertEquals(request2.getAuthor().getId(), testEmployee2.getId());
    }
    
    @Test
    @DisplayName("Добавление заявки на личный транспорт")
    @Order(33)
    void test_addPersonalRequest() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        request4.setAuthor(testEmployee1);
        request4.setPassengerCount(2);
        request4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).plusMinutes(5));
        request4.setTariffId(TARIFF_ID_2);
        request4.setOutcomeTariffId(TARIFF_ID_2);
        request4.setCoopTrip(false);
        request4.setPassenger(testEmployee2);
        request4.setPurpose(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        request4.setTransportType(TransportTypeEnum.PERSONAL);
        request4.setOccupiedPlacesCount(2);
        request4.setTimeZone("GMT+03:00");
        request4.getPersonalCar().setId(UUID.randomUUID());
        when(personalCarDataResolver.getPersonalCar(any(), any(), any(), any(), any()))
                .thenReturn(request4.getPersonalCar());
        request4.setId(null);
        NewRequestDTO requestDTO = mapper.requestForPersonalToDTO(request4);
        assertThat(requestDTO.getTransportType()).isEqualTo(TransportTypeEnum.PERSONAL);
        assertThat(requestDTO.getOccupiedPlacesCount()).isEqualTo(2);
        var request = objectMapper.writeValueAsString(requestDTO);
        
        var result = mockMvc.perform(post("/")
                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .content(request))
                            .andExpect(status().isOk());
        
        GetRequestDTO responseDTO =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       GetRequestDTO.class);
        RequestForPersonal actualRequest = (RequestForPersonal) requestRepository.findById(responseDTO.getId()).get();
        
        assertThat(actualRequest.getAuthor().getId()).isEqualTo(testEmployee1.getId());
        assertThat(actualRequest.getPassengerCount()).isEqualTo(2);
        assertThat(actualRequest.getTariffId()).isEqualTo(TARIFF_ID_2);
        assertFalse(actualRequest.isCoopTrip());
        assertThat(actualRequest.getPassenger().getId()).isEqualTo(testEmployee2.getId());
        assertThat(actualRequest.getPurpose().getId()).isEqualTo(purposeId1);
        assertThat(actualRequest.getTransportType()).isEqualTo(TransportTypeEnum.PERSONAL);
        assertThat(actualRequest.getOccupiedPlacesCount()).isEqualTo(2);
        assertThat(actualRequest.getPersonalCar().getId()).isEqualTo(request4.getPersonalCar().getId());
        assertThat(actualRequest.getTimeZone()).isEqualTo(request4.getTimeZone());
    }
    
    @Test
    @Transactional
    @DisplayName("Изменение заявки на личный транспорт в статусе TRIP_IN_PROGRESS")
    @Order(33)
    void test_editPersonal_tripInProgress() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        Employee author = employeeRepository.save(testEmployee1);
        request4.setAuthor(author);
        Employee passenger = employeeRepository.save(testEmployee2);
        request4.setPassengerCount(2);
        request4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).plusMinutes(5));
        request4.setTariffId(TARIFF_ID_2);
        request4.setOutcomeTariffId(TARIFF_ID_2);
        request4.setCoopTrip(false);
        request4.setPassenger(passenger);
        request4.setPurpose(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        request4.setTransportType(TransportTypeEnum.PERSONAL);
        request4.setOccupiedPlacesCount(2);
        request4.setPersonalCarId(UUID.randomUUID());
        request4.setTimeZone("GMT+03");
        request4.setId(null);
        request4.setStatus(TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS);
        NewRequestDTO requestDTO = mapper.requestForPersonalToDTO(request4);
        assertThat(requestDTO.getTransportType()).isEqualTo(TransportTypeEnum.PERSONAL);
        assertThat(requestDTO.getOccupiedPlacesCount()).isEqualTo(2);
        var saved = requestControllerService.add(requestDTO, author, "token", Collections.emptyList());
        WaypointDTO waypointDTO = requestDTO.getExpected().getWaypoints().get(1);
        saved.getExpected().getWaypoints().add(waypointDTO);
        
        mockMvc.perform(put("/" + saved.getId())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(saved)))
               .andExpect(status().isOk()).andReturn();
    }
    
    @Test
    @DisplayName("Изменение статуса оплаты заявок")
    @Order(34)
    void test_paymentStateChange() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.save(address3);
        //sharedRequestRepository.save(magentaSharedRequest);
        request4.setPassenger(employeeRepository.save(request4.getPassenger()));
        request4.setAuthor(employeeRepository.save(request4.getAuthor()));
        requestRepository.save(request4);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        var compensations = new ArrayList<>(publicRequest.getTransportCompensation());
        publicRequest.getTransportCompensation().clear();
        publicRequest.setPassenger(employeeRepository.save(publicRequest.getPassenger()));
        publicRequest.setAuthor(employeeRepository.save(publicRequest.getAuthor()));
        var savedPR = requestRepository.save(publicRequest);
        compensations.stream().peek(comp -> comp.setRequest(savedPR)).forEach(transportCompensationRepository::save);
        var content = new ArrayList<PaymentStateRequestDTO>();
        content.add(new PaymentStateRequestDTO(request4.getId(), true));
        content.add(new PaymentStateRequestDTO(publicRequest.getId(), false));
        var mvcResult = mockMvc.perform(put("/paymentStates")
                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .content(objectMapper.writeValueAsString(content)))
                               .andExpect(status().isOk()).andReturn();
        
        var response = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                           PaymentStateResponseDTO[].class);
        var requestsMap = new HashMap<UUID, PaymentStateResponseDTO>();
        for (PaymentStateResponseDTO paymentStateResponseDTO : response) {
            requestsMap.put(paymentStateResponseDTO.getRequestId(),
                            paymentStateResponseDTO);
        }
        assertEquals(2, response.length);
        assertEquals(TripRequestStatus.PERSONAL_PAYMENT_DONE.name(),
                     requestsMap.get(request4.getId()).getStatus());
        assertEquals(TripRequestStatus.PUBLIC_PAYMENT_NOT_DONE.name(),
                     requestsMap.get(publicRequest.getId()).getStatus());
    }
    
    @Test
    @DisplayName("Изменение согласованной поездки")
    @Order(35)
    void test_changeApprovedRequest() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        requestRepository.save(request1);
        
        var expectedDataDTO = ExpectedDataDTO.builder()
                                             .cost(10.0)
                                             .distance(20.0)
                                             .time(Duration.ofHours(1))
                                             .waypoints(Arrays.asList(
                                                     createWaypointDTO("w1", 50),
                                                     createWaypointDTO("w2", 60)))
                                             .segments(List.of(createSegmentDTO(40)))
                                             .build();
        assertThat(updateRequestRepository.count()).isZero();
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        
        var exception = mockMvc.perform(
                                       put("/update/approved/" + request1.getId())
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .content(objectMapper.writeValueAsString(expectedDataDTO))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                               .andExpect(status().isConflict())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(IllegalStateResponseException.class);
        
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        requestRepository.save(request1);
        mockMvc.perform(
                       put("/update/approved/" + request1.getId())
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                               .content(objectMapper.writeValueAsString(expectedDataDTO))
                               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        assertThat(updateRequestRepository.count()).isEqualTo(1);
        final UpdateRequest updateRequest = updateRequestRepository.findAll().getFirst();
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        assertThat(updateRequest.getRequest().getId()).isEqualTo(request1.getId());
        assertThat(updateRequest.getExpected().getCost()).isEqualTo(expectedDataDTO.getCost());
        assertThat(updateRequest.getExpected().getDistance()).isEqualTo(expectedDataDTO.getDistance());
        assertThat(updateRequest.getExpected().getTime()).isEqualTo(expectedDataDTO.getTime());
        assertThat(updateRequest.getSegmentsJSON()).hasSize(1);
        assertThat(updateRequest.getWaypoints()).hasSize(2);
        assertThat(updateRequest.getRequest().getTimeZone()).isEqualTo(request1.getTimeZone());
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        
        final var messageCaptor = ArgumentCaptor.forClass(UpdateTripRequestMessage.class);
        verify(updateRequestOutput).send(messageCaptor.capture());
        final var message = messageCaptor.getValue();
        assertThat(message.getId()).isEqualTo(updateRequest.getId());
        assertThat(message.getRequest().getId()).isEqualTo(updateRequest.getRequest().getId());
    }
    
    @Test
    @DisplayName("Получение поездки, смерженной с изменениями из заявки на согласование")
    void test_getRequestWithUpdate() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.saveAndFlush(address3);
        requestRepository.save(request1);
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        requestRepository.save(request1);
        
        var expectedDataDTO = ExpectedDataDTO.builder()
                                             .cost(10.0)
                                             .distance(20.0)
                                             .time(Duration.ofHours(1))
                                             .waypoints(Arrays.asList(
                                                     createWaypointDTO("w1", 50),
                                                     createWaypointDTO("w2", 60)))
                                             .segments(List.of(createSegmentDTO(40)))
                                             .build();
        assertThat(updateRequestRepository.count()).isZero();
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        
        request1.setStatus(TripRequestStatus.TAXI_APPROVED);
        requestRepository.save(request1);
        mockMvc.perform(
                       put("/update/approved/" + request1.getId())
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                               .content(objectMapper.writeValueAsString(expectedDataDTO))
                               .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        assertThat(updateRequestRepository.count()).isEqualTo(1);
        UpdateRequest updateRequest = updateRequestRepository.findAll().getFirst();
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        assertThat(updateRequest.getRequest().getId()).isEqualTo(request1.getId());
        assertThat(updateRequest.getRequest().getTimeZone()).isEqualTo(request1.getTimeZone());
        assertThat(updateRequest.getExpected().getCost()).isEqualTo(expectedDataDTO.getCost());
        assertThat(updateRequest.getExpected().getDistance()).isEqualTo(expectedDataDTO.getDistance());
        assertThat(updateRequest.getExpected().getTime()).isEqualTo(expectedDataDTO.getTime());
        assertThat(updateRequest.getSegmentsJSON()).hasSize(1);
        assertThat(updateRequest.getWaypoints()).hasSize(2);
        
        final MvcResult mvcResult = mockMvc.perform(
                                                   get("/with-update/" + request1.getId())
                                                           .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                                           .content(objectMapper.writeValueAsString(expectedDataDTO))
                                                           .contentType(MediaType.APPLICATION_JSON_VALUE))
                                           .andExpect(status().isOk()).andReturn();
        GetRequestDTO responseDTO =
                objectMapper.readValue(mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       GetRequestDTO.class);
        assertThat(updateRequestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findById(request1.getId()).get().getWaypoints()).hasSize(4);
        assertThat(responseDTO.getId()).isEqualTo(request1.getId());
        assertThat(responseDTO.getTimeZone()).isEqualTo(request1.getTimeZone());
        assertThat(responseDTO.getExpected().getCost()).isEqualTo(expectedDataDTO.getCost());
        assertThat(responseDTO.getExpected().getDistance()).isEqualTo(expectedDataDTO.getDistance());
        assertThat(responseDTO.getExpected().getTime()).isEqualTo(expectedDataDTO.getTime());
        assertThat(responseDTO.getExpected().getSegments()).hasSize(1);
        assertThat(responseDTO.getExpected().getWaypoints()).hasSize(2);
        
    }
    
    @Test
    @Order(99)
    @DisplayName("Попытка редактировать несогласованную заявку методом для согласованной - исключение")
    void test_editCreatedRequestByMethodForApprovedRequest_exception() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestDTO response = createSingleTaxiRequest();
        Optional<Request> byId = requestRepository.findById(response.getId());
        assertThat(byId).isPresent();
        //попытаться отредактировать не тем методом
        var taxiRequest = byId.get();
        String uri = "/update/approved/" + taxiRequest.getId();
        
        ExpectedDataDTO expectedDTO = ExpectedDataDTO.builder().cost(1000.0).distance(100.0).time(Duration.ofHours(1))
                                                     .segments(routeSegments3)
                                                     .waypoints(mapper.waypointListToDTOList(waypoints3)).build();
        String request = objectMapper.writeValueAsString(expectedDTO);
        
        Exception ex = mockMvc.perform(put(uri)
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                               .content(request))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
        
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
    }
    
    @Test
    @Order(100)
    @DisplayName("Попытка редактировать согласованную заявку методом для несогласованной - исключение")
    @Transactional
    void test_editApprovedRequestByMethodForCreatedRequest_exception() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        departmentRepository.save(department1);
        RequestDTO response = createSingleTaxiRequest();
        Optional<Request> byId = requestRepository.findById(response.getId());
        assertThat(byId).isPresent();
        //согласовать (статусы в БД)
        var taxiRequest = byId.get();
        taxiRequest.setStatus(TripRequestStatus.TAXI_APPROVED);
        taxiRequest = requestRepository.save(taxiRequest);
        //попытаться отредактировать не тем методом
        RequestDTO requestDTO = mapper.requestToDTO(taxiRequest);
        requestDTO.setTariffId(UUID.randomUUID());
        String request = objectMapper.writeValueAsString(requestDTO);
        String uri = "/" + taxiRequest.getId().toString();
        
        Exception ex = mockMvc.perform(put(uri)
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                               .content(request))
                              .andExpect(status().is4xxClientError()).andReturn().getResolvedException();
        
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
    }
    
    @Test
    @Order(101)
    @DisplayName("Получение подходящих совместных поездок с повторяющимися координатами")
    void test_getSuitableSharedRidesSameCoords() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        Employee author = new Employee(testEmployee1.getId());
        
        request1.setAuthor(author);
        
        Employee passenger = new Employee(testEmployee1.getId());
        request1.getWaypoints().set(0, request1.getWaypoints().get(1));
        request1.getWaypoints().remove(2);
        request1.getWaypoints().remove(2);
        request1.setPassengerCount(2);
        request1.setDesiredDate(
                LocalDateTime.now(ZoneId.of("UTC")).plusYears(10).plusHours(3).plusMinutes(5).plusMonths(1));
        request1.setTimeZone("GMT+3");
        request1.setTariffId(TARIFF_ID_1);
        request1.setCoopTrip(true);
        request1.setPassenger(passenger);
        request1.setPurpose(TripPurpose.builder().id(purposeId1).build());
        request1.getWaypoints().getFirst().setWaitTime(Duration.ofSeconds(111));
        assertTrue(employeeRepository.findById(testEmployee2.getId()).isPresent());
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        
        request1.setPassenger(testEmployee2);// Change passenger
        //Получение списка подходящих
        var result = mockMvc.perform(post("/shared/suitable")
                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE)
                                             .content(objectMapper.writeValueAsString(
                                                     mapper.requestForTaxiToDTO(request1))))
                            .andExpect(status().isOk());
        
        
        MagentaSharedRequestResponseDTO[] dto =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                       MagentaSharedRequestResponseDTO[].class);
        assertEquals(0, dto.length);
    }
    
    @Test
    @Order(102)
    @DisplayName("Завершение поездки от лица ВЛП с заполненной причиной отсутсвия")
    void test_completeRideWithAbsenceReasonForDepLimitOwner() throws Exception {
        addressRepository.save(address1);
        addressRepository.save(address2);
        addressRepository.save(address3);
        request4.setPassenger(employeeRepository.save(request4.getPassenger()));
        request4.setAuthor(employeeRepository.save(request4.getAuthor()));
        request4.getWaypoints().forEach(waypoint -> waypoint.setAbsenceReason("не поехал"));
        request4.setTimeZone("GMT+03");
        RequestForPersonal request = requestRepository.save(request4);
        depLimitRepository.save(new DepLimit(UUID.randomUUID(),
                                             testEmployee1.getDepartment().getId(),
                                             testEmployee1.getId(),
                                             Calendar.getInstance().get(Calendar.YEAR), true));
        
        var dep2 = departmentRepository.save(department1.toBuilder().id(UUID.randomUUID()).build());
        department1.setParent(dep2.getId());
        departmentRepository.save(department1);
        
        mockMvc.perform(post("/complete/" + request.getId())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk());
        
        Request result = requestRepository.findById(request.getId()).orElse(null);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL);
    }
    
    @Test
    @Order(106)
    @DisplayName("Проверка заказа в личных целях")
    @Transactional
    void test_getExternalPrices() throws Exception {
        //создать заявку
        departmentRepository.save(department1);
        checkinSettingsService.add(TransportServiceType.EMPLOYEE_TRANSPORTATION,
                                   TransportTypeEnum.TAXI,
                                   UUID.randomUUID(),
                                   100, false);
        
        Employee author = new Employee(testEmployee1.getId());
        request1.setAuthor(author);
        request1.setPassenger(author);
        request1.setPassengerCount(2);
        request1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).plusMinutes(5));
        request1.setTariffId(TARIFF_ID_1);
        request1.setCoopTrip(false);
        request1.getRequestOptions().add(RequestOptions.CHILD_SEAT);
        request1.setPurpose(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        request1.getWaypoints().getFirst().setWaitTime(Duration.ofSeconds(111));
        request1.setTimeZone("GMT+3");
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        
        assertTrue(employeeRepository.findById(testEmployee2.getId()).isPresent());
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        var requestDTO = mapper.requestToGetDTO(request1);
        var request = objectMapper.writeValueAsString(requestDTO);
        
        TaxiPriceDto taxiPriceDto = TaxiPriceDto.builder()
                                                .price(100)
                                                .taxiClass(TaxiClass.ECONOMY)
                                                .provider("citymobil")
                                                .build();
        
        when(cityMobilPriceServiceImpl.getPrice(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(taxiPriceDto)));
        when(yandexPriceServiceImpl.getPrice(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(taxiPriceDto)));
        when(uberPriceServiceImpl.getPrice(any()))
                .thenReturn(CompletableFuture.completedFuture(Collections.singletonList(taxiPriceDto)));
        when(cityMobilPriceServiceImpl.isEnabled())
                .thenReturn(true);
        when(yandexPriceServiceImpl.isEnabled())
                .thenReturn(true);
        when(uberPriceServiceImpl.isEnabled())
                .thenReturn(true);
        String response = mockMvc.perform(post("/externalPrices")
                                                  .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                  .content(request))
                                 .andExpect(status().isOk()).andReturn().getResponse()
                                 .getContentAsString(StandardCharsets.UTF_8);
        
        List<TaxiPriceDto> list = objectMapper.readValue(response, new TypeReference<>() {
        });
        assertThat(list).hasSize(3);
        
    }


    private Address deepCopy(Address from) {
        return Address.builder()
                      .latitude(from.getLatitude())
                      .longitude(from.getLongitude())
                      .country(from.getCountry())
                      .region(from.getRegion())
                      .city(from.getCity())
                      .street(from.getStreet())
                      .house(from.getHouse())
                      .building(from.getBuilding())
                      .structure(from.getStructure()).build();
    }

    private ResponseEntity<String> deletedAnswer() {
        return loadMagentaAnswerMock("cancel.json");
    }

    /**
     * Создать индивидуальную заявку на такси
     *
     * @return результат вызова метода мока контроллера
     *
     * @throws Exception любая ошибка
     */
    private RequestDTO createSingleTaxiRequest() throws Exception {
        checkinSettingsService.add(TransportServiceType.EMPLOYEE_TRANSPORTATION,
                                   TransportTypeEnum.TAXI,
                                   UUID.randomUUID(),
                                   100, false);
        
        request1.setAuthor(testEmployee1);
        request1.setPassenger(testEmployee1);
        request1.setPassengerCount(2);
        request1.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).plusMinutes(5));
        request1.setTariffId(TARIFF_ID_1);
        request1.setCoopTrip(false);
        request1.getRequestOptions().add(RequestOptions.CHILD_SEAT);
        request1.setPurpose(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        request1.getWaypoints().getFirst().setWaitTime(Duration.ofSeconds(111));
        request1.setTimeZone("GMT+3");
        taxiTripRepository.saveAndFlush(taxiTrip1);
        request1.setTaxiTrip(taxiTrip1);
        
        assertTrue(employeeRepository.findById(testEmployee2.getId()).isPresent());
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        var requestDTO = mapper.requestToGetDTO(request1);
        var request = objectMapper.writeValueAsString(requestDTO);
        
        String response = mockMvc.perform(post("/")
                                                  .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                  .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                                  .content(request))
                                 .andExpect(status().isOk()).andReturn().getResponse()
                                 .getContentAsString(StandardCharsets.UTF_8);
        
        return objectMapper.readValue(response, RequestDTO.class);
    }
    
    @Autowired
    private RequestForTaxiServiceImpl requestForTaxiService;
    
    @Test
    @Transactional
    @DisplayName("Получение заявки такси с фактическими данными")
    void test() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        final var sharedRide = Instancio.create(SrmSharedRideDTO.class);
        when(srmService.postNewSharedRide(any(), any())).thenReturn(sharedRide);
        
        saveRequestForTaxiWithFactData();
        
        GetRequestWithFactDataDTO expected = requestForTaxiService.getWithFactData(request1.getId());
        
        var response = mockMvc.perform(
                                      get("/TAXI/fact_data/" + request1.getId())
                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                              .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andExpect(status().isOk()).andReturn();
        
        
        GetRequestWithFactDataDTO actual = objectMapper.readValue(response.getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                                  GetRequestWithFactDataDTO.class);
        
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPassenger().getFirstName(), actual.getPassenger().getFirstName());
        assertEquals(expected.getPassenger().getLastName(), actual.getPassenger().getLastName());
        assertEquals(expected.getPassenger().getPatronymic(), actual.getPassenger().getPatronymic());
        assertEquals(expected.getApprovedBy().getFirstName(), actual.getApprovedBy().getFirstName());
        assertEquals(expected.getApprovedBy().getLastName(), actual.getApprovedBy().getLastName());
        assertEquals(expected.getApprovedBy().getPatronymic(), actual.getApprovedBy().getPatronymic());
        assertEquals(expected.getPassenger().getDepartmentName(), actual.getPassenger().getDepartmentName());
        assertEquals(expected.getCreationTime().getMinute(), actual.getCreationTime().getMinute());
        
        assertEquals(expected.getWaypoints().getFirst().getCity(),
                     actual.getWaypoints().getFirst().getCity());
        
        int size = expected.getWaypoints().size();
        
        assertEquals(expected.getWaypoints().get(size - 1).getCity(),
                     actual.getWaypoints().get(size - 1).getCity());
        assertEquals(expected.getTaxiClass(), actual.getTaxiClass());
        assertEquals(expected.getPassengerCount(), actual.getPassengerCount());
        assertEquals(expected.getPurpose().getLabel(), actual.getPurpose().getLabel());
        assertEquals(expected.isCoopTrip(), actual.isCoopTrip());
        
        // Фактические данные по поездке
        FactDataDTO actFactData = actual.getFactDataDTO();
        FactDataDTO expFactData = expected.getFactDataDTO();
        
        assertEquals(actFactData.getTripStartTime(), expFactData.getTripStartTime());
        assertEquals(actFactData.getTripFactPrice(), expFactData.getTripFactPrice());
        
        assertEquals(actFactData.getTripFactWaitTime(), expFactData.getTripFactWaitTime());
        assertEquals(actFactData.getTripFactDistance(), expFactData.getTripFactDistance());
        assertEquals(actFactData.getTripFactDuration(), expFactData.getTripFactDuration());
        
        assertEquals(expected.getTariffId(), actual.getTariffId());
        assertEquals(expected.getTariffId(), actual.getTariffId());
        
        assertEquals(expected.getRequestOptions().size(), actual.getRequestOptions().size());
        assertEquals(expected.getCommentForDriver(), actual.getCommentForDriver());
        assertEquals(expected.getRequestRating().getRating(), actual.getRequestRating().getRating());
        assertEquals(expected.getSharedRideId(), actual.getSharedRideId());
        assertEquals(expected.getWaypoints().get(size - 1).isExistInVspGosbTbRegistry(),
                     actual.getWaypoints().get(size - 1).isExistInVspGosbTbRegistry());
    }
    
    void saveRequestForTaxiWithFactData() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        checkinSettingsService.add(TransportServiceType.EMPLOYEE_TRANSPORTATION,
                                   TransportTypeEnum.TAXI,
                                   regionId,
                                   200, false);
        
        request1.setPassengerCount(1);
        request1.setCoopTrip(false);
        request1.getRequestOptions().add(RequestOptions.CHILD_SEAT);
        
        addressRepository.saveAndFlush(address1);
        addressRepository.saveAndFlush(address2);
        addressRepository.saveAndFlush(address3);
        
        request1 = (RequestForTaxi) requestService.add(null, true, mapper.requestForTaxiToDTO(request1), testEmployee1);
        
        SingleTaxiTrip taxiTrip = SingleTaxiTrip.builder()
                                                .tripType(TripType.SINGLE)
                                                .dateTimeRegistered(LocalDateTime.now())
                                                .organizationId(organization1.getId())
                                                .tariffId(TARIFF_ID_1)
                                                .tripFinishTime(LocalDateTime.now().plusHours(2))
                                                .tripStartTime(LocalDateTime.now())
                                                .requests(List.of(request1))
                                                .status(InboundTaxiTripStatus.ORDER_FINISHED)
                                                .taxiId(UUID.randomUUID().toString())
                                                .tripFactDistance(1d)
                                                .tripFactDuration(Duration.ofHours(1))
                                                .tripFactPrice(100)
                                                .tripFactWaitTime(Duration.ofSeconds(100))
                                                .tripAssignmentDateTime(LocalDateTime.now())
                                                .active(true)
                                                .humanReadableId("US-0003-9")
                                                .contractorComment("comment")
                                                .resolution("Произвольное описание работ")
                                                .decisionCode(TaxiTripDecisionCode.FULLY_RESOLVED)
                                                .assignedCar(CarInfo.builder()
                                                                    .brandName("BMW")
                                                                    .model("X6")
                                                                    .color("Black")
                                                                    .registrationNumber("A888XY163RUS")
                                                                    .build())
                                                .timeWorkStart(LocalDateTime.now())
                                                .timeWorkFinish(LocalDateTime.now().plusHours(2))
                                                .lastXmlReceivedDateTime(LocalDateTime.now())
                                                .build();
        
        taxiTripRepository.saveAndFlush(taxiTrip);
        
        Employee approvedBy = Employee.builder()
                                      .id(UUID.fromString(USER1_ID))
                                      .humanReadableId("US-0002-99")
                                      .department(department1)
                                      .firstName("ИмяСогласующего")
                                      .lastName("ФамилияСогласующего")
                                      .patronymic("ОтчествоСогласующего")
                                      .build();
        
        employeeRepository.saveAndFlush(approvedBy);
        
        request1.setTaxiTrip(taxiTrip);
        request1.setApprovedBy(approvedBy);
        request1.setRequestRating(RequestRating.builder().rating(5).build());
        
        taxiRequestRepository.saveAndFlush(request1);
    }
}