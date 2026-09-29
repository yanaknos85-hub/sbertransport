package ru.sberbank.ditsib.transport.request.controller.publicTransport;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.PublicTrApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.dao.publicTransport.CompensationDocumentRepository;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.corp.Position;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceResponse;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.*;
import ru.sberbank.ditsib.transport.request.mappers.EmployeeMapper;
import ru.sberbank.ditsib.transport.request.messaging.message.RequestDocumentMessage;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@AutoConfigureMockMvc
@SpringBootTest(classes = RequestApplication.class)
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Тест контроллера для создания новой заявки для компенсации за городской общественный транспорт")
class RequestForPublicControllerTest extends KafkaTest {
    
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();
    
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private FraudRepository fraudRepository;
    @Autowired
    private RequestForPublicRepository requestForPublicRepository;
    @Autowired
    private TransportCompensationRepository transportCompensationRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private EntityDTOMapper entityDTOMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CompensationDocumentRepository compensationDocumentRepository;
    
    @MockitoBean("requestOutput")
    private OutputBridge requestOutput;

    @MockitoBean("requestDocumentOutput")
    private OutputBridge requestDocumentOutput;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private PublicTrApprovalsSettingsRepository publicApprovalsSettingsRepository;
    @Autowired
    private PublicTariffRepository publicTariffRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @MockitoBean
    private RegionDataResolver regionDataResolver;

    @MockitoBean
    private EasupGrpcService easupGrpcService;
    
    @Autowired
    private ReservationService reservationService;
    
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

    private static final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final UUID DEPARTMENT_ID = UUID.randomUUID();
    private static final UUID POSITION_ID = UUID.randomUUID();
    private static final UUID EMPLOYEE1_ID = UUID.randomUUID();
    private static final String USER1_ID_STR = "ca90f985-a6db-443a-86d2-962cf0ef2b75";
    private static final String USER2_ID_STR = "c46d76cd-febd-464e-9446-2f14b9f4548b";
    private static final UUID USER1_ID = UUID.fromString(USER1_ID_STR);
    private static final UUID EMPLOYEE2_ID = UUID.randomUUID();
    private static final UUID EMPLOYEE3_ID = UUID.randomUUID();
    private static final UUID USER2_ID = UUID.fromString(USER2_ID_STR);
    private static final UUID USER3_ID = UUID.randomUUID();
    private static final UUID PURPOSE1_ID = UUID.randomUUID();
    private static final UUID PURPOSE2_ID = UUID.randomUUID();
    private static final String COMPENSATION_URL = "/public/compensation";

    private CompensationDocument doc3;
    private EmployeeDTO employeeDTO1;
    private EmployeeDTO employeeDTO2;
    private EmployeeDTO employeeDTO3;
    private PublicExpectedDataDTO expectedDataDTO2;
    private WaypointDTO waypointDto2;
    private WaypointDTO waypointDto3;
    private RouteSegmentDTO segmentDTO2;
    private TripPurposeDTO purposeDTO2;
    private List<CompensationDocumentDTO> docsDto1;
    private List<CompensationDocumentDTO> docsDto2;
    private NewRequestForCompensationDTO cityTripAndTravelCardCompensationDTO;
    private NewRequestForCompensationDTO cityTripAndTravelCardAndPaidCompensationDTO;
    private NewRequestForCompensationDTO cityTripCompensationDTO;
    private NewRequestForCompensationDTO failedCityTripCompensationDTO;
    private NewRequestForCompensationDTO travelCardCompensationDTO;
    private NewRequestForCompensationDTO suburbTripCompensationDTO;
    private PublicTariff publicTariff;

    @BeforeEach
    void init() {
        var organization = organizationService.save(Organization.builder().id(ORGANIZATION_ID).digitId(1L).build());

        var department = departmentService.save(Department.builder()
                .id(DEPARTMENT_ID)
                .organization(organization)
                .departmentName("Department")
                .build());

        var position = positionRepository.save(Position.builder().id(POSITION_ID).positionName("Position")
                .organizationId(organization.getId()).build());

        var employee1 = employeeService.save(
                Employee.builder().id(EMPLOYEE1_ID).humanReadableId("EMLP-0001").firstName("Name1")
                        .lastName("LastName1")
                        .patronymic("Patronymic1").userId(USER1_ID).personnelNumber("Pers1")
                        .department(department).positionId(position.getId()).mobilePhone("+792792725")
                        .build());

        var employee2 = employeeService.save(
                Employee.builder().id(EMPLOYEE2_ID).humanReadableId("EMLP-0002").firstName("Name2")
                        .lastName("LastName2")
                        .patronymic("Patronymic2").userId(USER2_ID).personnelNumber("Pers2")
                        .department(department).positionId(position.getId()).mobilePhone("+792792726")
                        .build());

        var employee3 = employeeService.save(
                Employee.builder().id(EMPLOYEE3_ID).humanReadableId("EMLP-0003").firstName("Name3")
                        .lastName("LastName3")
                        .patronymic("Patronymic3").userId(USER3_ID).personnelNumber("Pers3")
                        .department(department).positionId(position.getId()).mobilePhone("+792792727")
                        .build());

        var purpose1 = tripPurposeRepository.save(
                TripPurpose.builder().id(PURPOSE1_ID).purpose("Purpose1").organization(organization.getId()).build());

        var purpose2 = tripPurposeRepository.save(
                TripPurpose.builder().id(PURPOSE2_ID).purpose("Purpose2").organization(organization.getId()).build());

        var doc1 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File1.jpeg")
                        .folder(UUID.randomUUID()).creationTime(LocalDateTime.now())
                        .fileSize(10 * 1024 * 1024).fileFormat(UploadFileFormats.JPEG).build());

        var doc2 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File2.pdf")
                        .folder(UUID.randomUUID()).creationTime(LocalDateTime.now())
                        .fileSize(1024 * 1024).fileFormat(UploadFileFormats.PDF).build());

        doc3 = compensationDocumentRepository.save(
                CompensationDocument.builder().fileName("File3.png")
                        .folder(UUID.randomUUID()).creationTime(LocalDateTime.now())
                        .fileSize(5 * 1024 * 1024).fileFormat(UploadFileFormats.PNG).build());

        employeeDTO1 = employeeMapper.toDto(employee1);
        employeeDTO2 = employeeMapper.toDto(employee2);
        employeeDTO3 = employeeMapper.toDto(employee3);

        var expectedDataDTO1 = PublicExpectedDataDTO.builder().cost(64.0).distance(10.0).time(Duration.ofHours(1)).build();
        expectedDataDTO2 = PublicExpectedDataDTO.builder().cost(128.0).distance(20.0).time(Duration.ofHours(2)).build();

        var waypointDto1 = WaypointDTO
                .builder()
                .country("Country")
                .region("Region")
                .city("City")
                .street("Street1")
                .house("1")
                .latitude(50.1234)
                .longitude(53.0123)
                .waitTime(Duration.ofMinutes(10))
                .build();

        waypointDto2 = WaypointDTO.builder().country("Country").region("Region").city("City")
                .street("Street2").house("2").latitude(50.2345).longitude(53.3214)
                .waitTime(Duration.ofMinutes(20)).build();

        waypointDto3 = WaypointDTO.builder().country("Country").region("Region").city("City")
                .street("Street3").house("3").latitude(50.3456).longitude(53.2103)
                .waitTime(Duration.ofMinutes(30)).build();

        var segmentDTO1 = RouteSegmentDTO.builder().cost(64.0).distance(10.0).time(Duration.ofHours(1))
                .coordinates(List.of(
                        new CoordinatesDTO(50.1234, 53.0123),
                        new CoordinatesDTO(50.2345, 53.3214))).build();

        segmentDTO2 = RouteSegmentDTO.builder().cost(128.0).distance(20.0).time(Duration.ofHours(2))
                .coordinates(List.of(
                        new CoordinatesDTO(50.2345, 53.3214),
                        new CoordinatesDTO(50.3456, 53.2103))).build();

        var purposeDTO1 = entityDTOMapper.tripPurposeToDTO(purpose1);
        purposeDTO2 = entityDTOMapper.tripPurposeToDTO(purpose2);

        docsDto1 = entityDTOMapper.documentsToDto(List.of(doc1, doc2));
        docsDto2 = entityDTOMapper.documentsToDto(List.of(doc2, doc3));

        var cityTripCompensationDto = PublicCompensationTypeDTO.builder()
                .name(PublicCompensationType.CITY_TRIP_COMPENSATION.name())
                .build();
        var travelCardCompensationDto = PublicCompensationTypeDTO.builder()
                .name(PublicCompensationType.TRAVEL_CARD_COMPENSATION.name())
                .build();
        var suburbCompensationDto = PublicCompensationTypeDTO.builder()
                .name(PublicCompensationType.SUBURB_TRIP_COMPENSATION.name())
                .build();
        var paidCompensationDto = PublicCompensationTypeDTO.builder()
                .name(PublicCompensationType.PAID_SERVICES_COMPENSATION.name())
                .build();

        var cityBusTransportType = PublicTransportTypeDTO.builder()
                .name(PublicTransportType.CITY_BUS.name())
                .build();
        var cityMetroTransportType = PublicTransportTypeDTO.builder()
                .name(PublicTransportType.CITY_METRO.name())
                .build();
        var travelCardBusTransportType = PublicTransportTypeDTO.builder()
                .name(PublicTransportType.TRAVEL_CARD_BUS.name())
                .build();
        var suburbTransportType = PublicTransportTypeDTO.builder()
                .name(PublicTransportType.SUBURB_BUS.name())
                .build();
        var paidTransportType = PublicTransportTypeDTO.builder()
                .name(PublicTransportType.PAID_PARKING.name())
                .build();

        var cityTripAndTravelCardCompensationList = List.of(
                NewTransportCompensationDTO.builder()
                        .compensationType(cityTripCompensationDto)
                        .transportType(cityBusTransportType)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build(),
                NewTransportCompensationDTO.builder()
                        .compensationType(cityTripCompensationDto)
                        .transportType(cityMetroTransportType)
                        .ticketsCost(500)
                        .ticketsCount(1)
                        .build(),
                NewTransportCompensationDTO.builder()
                        .compensationType(travelCardCompensationDto)
                        .transportType(travelCardBusTransportType)
                        .ticketsCost(500)
                        .ticketsExpirationStart(LocalDate.now())
                        .ticketsExpirationEnd(LocalDate.now().plusMonths(1))
                        .build()
        );
        var cityTripAndTravelCardAndPaidCompensationList = List.of(
                NewTransportCompensationDTO.builder()
                        .compensationType(cityTripCompensationDto)
                        .transportType(cityBusTransportType)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build(),
                NewTransportCompensationDTO.builder()
                        .compensationType(cityTripCompensationDto)
                        .transportType(cityMetroTransportType)
                        .ticketsCost(500)
                        .ticketsCount(1)
                        .build(),
                NewTransportCompensationDTO.builder()
                        .compensationType(travelCardCompensationDto)
                        .transportType(travelCardBusTransportType)
                        .ticketsCost(500)
                        .ticketsExpirationStart(LocalDate.now())
                        .ticketsExpirationEnd(LocalDate.now().plusMonths(1))
                        .build(),
                NewTransportCompensationDTO.builder()
                        .compensationType(paidCompensationDto)
                        .transportType(paidTransportType)
                        .ticketsCost(500)
                        .ticketsCount(1)
                        .build()
        );
        List<NewTransportCompensationDTO> cityTripCompensationList = List.of(NewTransportCompensationDTO.builder()
                .compensationType(cityTripCompensationDto)
                .transportType(cityBusTransportType)
                .ticketsCost(350)
                .ticketsCount(1)
                .attachedDocumentId(doc1.getId())
                .build()
        );
        List<NewTransportCompensationDTO> failedCityTripCompensationList = List.of(NewTransportCompensationDTO.builder()
                .compensationType(cityTripCompensationDto)
                .ticketsCost(350)
                .ticketsCount(1)
                .attachedDocumentId(doc1.getId())
                .build()
        );
        List<NewTransportCompensationDTO> travelCardCompensationList = List.of(NewTransportCompensationDTO.builder()
                .compensationType(travelCardCompensationDto)
                .transportType(travelCardBusTransportType)
                .ticketsCost(500)
                .ticketsExpirationStart(LocalDate.now())
                .ticketsExpirationEnd(LocalDate.now().plusMonths(1))
                .build()
        );
        List<NewTransportCompensationDTO> suburbTripCompensationList = List.of(NewTransportCompensationDTO.builder()
                .compensationType(suburbCompensationDto)
                .transportType(suburbTransportType)
                .ticketsCost(1000)
                .ticketsExpirationStart(LocalDate.now())
                .ticketsExpirationEnd(LocalDate.now().plusMonths(1))
                .build()
        );

        publicTariff = publicTariffRepository.save(PublicTariff.builder()
                .id(UUID.randomUUID())
                .humanReadableId("PT-123-23")
                .departmentId(department.getId())
                .transportType(TransportTypeEnum.CARSHARING)
                .metroTicketCost(1)
                .tramTicketCost(2)
                .trolleybusTicketCost(3)
                .busTicketCost(4)
                .metroAvailability(true)
                .tramAvailability(true)
                .trolleybusAvailability(true)
                .busAvailability(true)
                .build());

        cityTripAndTravelCardCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .compensationDocuments(docsDto1)
                .transportCompensation(cityTripAndTravelCardCompensationList)
                .tariffId(publicTariff.getId())
                .build();

        cityTripAndTravelCardAndPaidCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .compensationDocuments(docsDto1)
                .transportCompensation(cityTripAndTravelCardAndPaidCompensationList)
                .tariffId(publicTariff.getId())
                .build();

        cityTripCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .compensationDocuments(docsDto1)
                .transportCompensation(cityTripCompensationList)
                .tariffId(publicTariff.getId())
                .build();

        failedCityTripCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .compensationDocuments(docsDto1)
                .transportCompensation(failedCityTripCompensationList)
                .tariffId(publicTariff.getId())
                .build();

        travelCardCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .transportCompensation(travelCardCompensationList)
                .tariffId(publicTariff.getId())
                .build();
        suburbTripCompensationDTO = NewRequestForCompensationDTO.builder()
                .author(employeeDTO2)
                .passenger(employeeDTO1)
                .desiredDate(LocalDateTime.now())
                .expected(expectedDataDTO1)
                .waypoints(List.of(waypointDto1, waypointDto2))
                .segments(List.of(segmentDTO1))
                .purpose(purposeDTO1)
                .compensationDocuments(docsDto1)
                .transportCompensation(suburbTripCompensationList)
                .tariffId(publicTariff.getId())
                .build();

        var regionDto = List.of(RegionDto.builder().id(UUID.randomUUID()).name("Москва").timeZone("+3").build());
        when(regionDataResolver.getRegionBranch(any())).thenReturn(regionDto);
    }

    @Transactional
    @DisplayName("Создание - городской транспорт - успех")
    @Test
    void addCityPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO actual = postCompensationRequest(cityTripCompensationDTO);
        
        assertThat(actual.getSegments().getFirst().getCost()).isEqualTo(cityTripCompensationDTO.getSegments().getFirst().getCost());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cityTripCompensationDTO.getWaypoints());
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getApprovalState()).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cityTripCompensationDTO.getExpected().getDistance());
        assertThat(actual.getPurpose().getLabel()).isEqualTo(cityTripCompensationDTO.getPurpose().getLabel());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().id()).isEqualTo(cityTripCompensationDTO.getPassenger().id());
        //проверить перезапись инициатора из Authentication
        assertThat(actual.getAuthor().firstName()).isNotEqualTo(cityTripCompensationDTO.getAuthor().firstName());
        assertThat(actual.getAuthor().userId()).hasToString(USER1_ID_STR);
        TransportCompensationDTO transportCompensationDTO = actual.getTransportCompensation().getFirst();
        assertThat(transportCompensationDTO.getCompensationDocumentDTO().getId())
                .isEqualTo(transportCompensationDTO.getAttachedDocumentId());
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().id());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().id());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isEqualTo(actual.getTariffId());
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }

    @Transactional
    @DisplayName("Создание - городской транспорт - успех - с фродом")
    @Test
    void addCityPublicRequest_success_with_fraud() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        var response = Instancio.create(EasupAbsenceResponse.class);

        doReturn(Optional.of(response)).when(easupGrpcService).resolveAbsence(any(EasupAbsenceRequest.class));
        cityTripCompensationDTO.setAuthor(employeeDTO1);
        cityTripCompensationDTO.setPassenger(employeeDTO2);
        RequestForCompensationDTO actual = postCompensationRequest(cityTripCompensationDTO);

        assertThat(actual.getSegments().getFirst().getCost()).isEqualTo(cityTripCompensationDTO.getSegments().getFirst().getCost());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cityTripCompensationDTO.getWaypoints());
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getApprovalState()).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cityTripCompensationDTO.getExpected().getDistance());
        assertThat(actual.getPurpose().getLabel()).isEqualTo(cityTripCompensationDTO.getPurpose().getLabel());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().id()).isEqualTo(cityTripCompensationDTO.getPassenger().id());
        assertThat(actual.getAuthor().userId()).hasToString(USER1_ID_STR);
        TransportCompensationDTO transportCompensationDTO = actual.getTransportCompensation().getFirst();
        assertThat(transportCompensationDTO.getCompensationDocumentDTO().getId())
                .isEqualTo(transportCompensationDTO.getAttachedDocumentId());

        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());

        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getFraudData()).hasSize(1);
        assertThat(message.getFraudData().getFirst().type()).isEqualTo(FraudType.ABSENCE.name());

        var fraudList = fraudRepository.findAllByRequestIdIn(List.of(actual.getId()));
        assertThat(fraudList).hasSize(1);
        assertThat(fraudList.getFirst().getType()).isEqualTo(FraudType.ABSENCE);
        assertThat(fraudList.getFirst().getComment()).isEqualTo("На выбранные дату/время пассажир в отпуске/на больничном");

    }
    
    @Transactional
    @DisplayName("Создание - городской транспорт - ошибка валидации")
    @Test
    void addCityPublicRequest_validation_failed() throws Exception {
        String request = objectMapper.writeValueAsString(failedCityTripCompensationDTO);
        
        MvcResult result = mockMvc.perform(
                                          post(COMPENSATION_URL).content(request).characterEncoding("UTF-8")
                                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                  .andExpect(status().is4xxClientError())
                                  .andReturn();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<>() {
        };
        var response = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), typeRef);
        assertThat(response).containsEntry("message", "Bad Request");
    }
    
    @Transactional
    @DisplayName("Создание - платный сервис с другими видами компенсации - ошибка валидации")
    @Test
    void paidServiceWithOtherTypes_validation_failed() throws Exception {
        String request = objectMapper.writeValueAsString(cityTripAndTravelCardAndPaidCompensationDTO);
        
        MvcResult result = mockMvc.perform(
                                          post(COMPENSATION_URL).content(request).characterEncoding("UTF-8")
                                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                  .andExpect(status().is4xxClientError())
                                  .andReturn();
        TypeReference<HashMap<String, Object>> typeRef
                = new TypeReference<>() {
        };
        var response = objectMapper.readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8), typeRef);
        assertThat(response).containsEntry("message", "Заявка на компенсацию платных сервисов не должна содержать других типов компенсаций.");
    }
    
    @Transactional
    @DisplayName("Отмена - городской транспорт - успех")
    @Test
    void cancelCityPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO actual = postCompensationRequest(cityTripCompensationDTO);
        
        verify(requestOutput).send(any(), anyMap());
        
        String requestContent =
                objectMapper.writeValueAsString(CancelDTO.builder().reason("reason").code(100).build());
        
        mockMvc.perform(put("/cancel/" + actual.getId())
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                .content(requestContent)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_CANCELLED.name());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().id());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().id());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isEqualTo(actual.getTariffId());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }
    
    @Transactional
    @DisplayName("Отмена - городской транспорт в статусе 'Формирование приказа на выплату' - успех")
    @Test
    void cancelCityPublicRequestPaymentStatus_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO actual = postCompensationRequest(cityTripCompensationDTO);
        
        Request cityTripCompensation = requestRepository.findById(actual.getId()).orElseThrow();
        cityTripCompensation.setStatus(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
        requestRepository.save(cityTripCompensation);
        
        String requestContent =
                objectMapper.writeValueAsString(CancelDTO.builder().reason("reason").code(100).build());
        
        mockMvc.perform(put("/cancel/" + actual.getId())
                                .header("Authorization", "Basic login:password")
                                .content(requestContent)
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk()).andReturn();
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_CANCELLED.name());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().id());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().id());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isEqualTo(actual.getTariffId());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }
    
    @Transactional
    @DisplayName("Создание - пригородный транспорт - успех")
    @Test
    void addSuburbPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO actual = postCompensationRequest(cityTripAndTravelCardCompensationDTO);
        
        assertThat(actual.getSegments().getFirst().getCost()).isEqualTo(cityTripAndTravelCardCompensationDTO.getSegments().getFirst().getCost());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cityTripAndTravelCardCompensationDTO.getWaypoints());
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getApprovalState()).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cityTripAndTravelCardCompensationDTO.getExpected().getDistance());
        assertThat(actual.getPurpose().getLabel()).isEqualTo(cityTripAndTravelCardCompensationDTO.getPurpose().getLabel());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().id()).isEqualTo(cityTripAndTravelCardCompensationDTO.getPassenger().id());
        assertThat(actual.getCompensationDocuments()).hasSameSizeAs(cityTripAndTravelCardCompensationDTO.getCompensationDocuments());
        //проверить перезапись инициатора из Authentication
        assertThat(actual.getAuthor().firstName()).isNotEqualTo(cityTripAndTravelCardCompensationDTO.getAuthor().firstName());
        assertThat(actual.getAuthor().userId()).hasToString(USER1_ID_STR);
        checkSendsDocuments(UUID.fromString(USER1_ID_STR), actual.getId(), actual.getCompensationDocuments());
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().id());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().id());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isNotNull();
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }

    @Transactional
    @DisplayName("Создание - пригородный транспорт - успех")
    @Test
    void addPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO actual = postCompensationRequest(cityTripAndTravelCardCompensationDTO);
        
        assertThat(transportCompensationRepository.count()).isEqualTo(3);
        assertGetRequest(actual);
    }
    
    private void assertGetRequest(RequestForCompensationDTO actual) {
        assertGetRequest(actual, 1);
    }
    
    private void assertGetRequest(RequestForCompensationDTO actual, int times) {
        assertThat(actual.getSegments().getFirst().getCost()).isEqualTo(cityTripAndTravelCardCompensationDTO.getSegments().getFirst().getCost());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cityTripAndTravelCardCompensationDTO.getWaypoints());
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getApprovalState()).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cityTripAndTravelCardCompensationDTO.getExpected().getDistance());
        assertThat(actual.getPurpose().getLabel()).isEqualTo(cityTripAndTravelCardCompensationDTO.getPurpose().getLabel());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().id()).isEqualTo(cityTripAndTravelCardCompensationDTO.getPassenger().id());
        assertThat(actual.getCompensationDocuments()).hasSameSizeAs(cityTripAndTravelCardCompensationDTO.getCompensationDocuments());
        //проверить перезапись инициатора из Authentication
        assertThat(actual.getAuthor().firstName()).isNotEqualTo(cityTripAndTravelCardCompensationDTO.getAuthor().firstName());
        
        assertThat(actual.getAuthor().userId()).hasToString(USER1_ID_STR);
        checkSendsDocuments(UUID.fromString(USER1_ID_STR), actual.getId(), actual.getCompensationDocuments());
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(times)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().id());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().id());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isNotNull();
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }
    
    @Transactional
    @DisplayName("Изменение цены проездного городской транспорт - успех")
    @Test
    void editCostTravelCardPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO postRequest = postCompensationRequest(cityTripAndTravelCardCompensationDTO);
        List<TransportCompensationDTO> insertCompensationList = postRequest.getTransportCompensation();
        insertCompensationList.getFirst().setTicketsCost(100);
        
        RequestForPublic putRequest = putCompensationRequest(postRequest, postRequest.getId());
        List<TransportCompensation> editCompensationList = putRequest.getTransportCompensation();
        
        assertThat(insertCompensationList.getFirst().getId()).isEqualTo(editCompensationList.getFirst().getId());
        assertThat(insertCompensationList.getFirst().getTicketsCost()).isEqualTo(editCompensationList.getFirst().getTicketsCost());
        assertThat(insertCompensationList.getFirst().getTransportType().getName())
                .isEqualTo(editCompensationList.getFirst().getTransportType().name());
        
        assertThat(postRequest.getId()).isEqualTo(putRequest.getId());
        assertThat(transportCompensationRepository.count()).isEqualTo(3);
        
        RequestForCompensationDTO actual = getCompensationRequest(postRequest.getId());
        assertGetRequest(actual, 2);
    }
    
    @Transactional
    @DisplayName("Изменение - городской транспорт - успех")
    @Test
    void editPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO postRequest = postCompensationRequest(cityTripAndTravelCardCompensationDTO);
        List<TransportCompensationDTO> insertCompensationList = postRequest.getTransportCompensation();
        
        PublicCompensationTypeDTO cityTripCompensationDto = PublicCompensationTypeDTO.builder()
                                                                                     .name(PublicCompensationType.CITY_TRIP_COMPENSATION.name())
                                                                                     .build();
        PublicTransportTypeDTO cityTramTransportType = PublicTransportTypeDTO.builder()
                                                                             .name(PublicTransportType.CITY_TRAM.name())
                                                                             .build();
        TransportCompensationDTO editRequestCompensationList = TransportCompensationDTO
                .builder()
                .compensationType(cityTripCompensationDto)
                .transportType(cityTramTransportType)
                .ticketsCost(500)
                .build();
        
        insertCompensationList.remove(2);
        insertCompensationList.remove(1);
        insertCompensationList.getFirst().setTicketsCost(100);
        insertCompensationList.add(editRequestCompensationList);
        
        RequestForPublic putRequest = putCompensationRequest(postRequest, postRequest.getId());
        List<TransportCompensation> editCompensationList = putRequest.getTransportCompensation();
        
        assertThat(insertCompensationList.getFirst().getId()).isEqualTo(editCompensationList.getFirst().getId());
        assertThat(insertCompensationList.getFirst().getTicketsCost()).isEqualTo(editCompensationList.getFirst().getTicketsCost());
        assertThat(insertCompensationList.getFirst().getTransportType().getName())
                .isEqualTo(editCompensationList.getFirst().getTransportType().name());
        
        assertThat(postRequest.getId()).isEqualTo(putRequest.getId());
        assertThat(transportCompensationRepository.count()).isEqualTo(2);
        
        RequestForCompensationDTO actual = getCompensationRequest(postRequest.getId());
        assertGetRequest(actual, 2);
    }

    @Transactional
    @DisplayName("Создание - городской транспорт - валидация")
    @Test
    void testValidationForCityPublicRequest_create() throws Exception {
        commonCreateValidationTest(COMPENSATION_URL, NewRequestForCompensationDTO.builder().build());
    }
    
    @Transactional
    @DisplayName("Изменение - городской транспорт - успех")
    @Test
    void editCityPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO beforeUpdateCityDto = postCompensationRequest(cityTripCompensationDTO);
        
        RequestForCompensationDTO cityDto = RequestForCompensationDTO.builder()
                                                                     .author(employeeDTO1)
                                                                     .passenger(employeeDTO3)
                                                                     .desiredDate(LocalDateTime.now())
                                                                     .expected(expectedDataDTO2)
                                                                     .waypoints(List.of(waypointDto2, waypointDto3))
                                                                     .segments(List.of(segmentDTO2))
                                                                     .purpose(purposeDTO2)
                                                                     .compensationDocuments(docsDto1)
                                                                     .transportCompensation(beforeUpdateCityDto.getTransportCompensation())
                                                                     .tariffId(publicTariff.getId())
                                                                     .build();
        cityDto.setId(beforeUpdateCityDto.getId());
        cityDto.setHumanReadableId(beforeUpdateCityDto.getHumanReadableId());
        cityDto.setStatus(beforeUpdateCityDto.getStatus());
        cityDto.setCreationTime(beforeUpdateCityDto.getCreationTime());
        
        RequestForPublic actual = putCompensationRequest(cityDto, beforeUpdateCityDto.getId());
        
        assertThat(actual.getSegmentsJSON().getFirst().getCost()).isEqualTo(cityDto.getSegments().getFirst().getCost());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cityDto.getWaypoints());
        //проверить изменения вейпойнтов
        assertThat(actual.getWaypoints().getFirst().getAddress().getStreet())
                .isEqualTo(cityDto.getWaypoints().getFirst().getStreet());
        assertThat(actual.getWaypoints().get(1).getAddress().getBuilding())
                .isEqualTo(cityDto.getWaypoints().get(1).getBuilding());
        
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cityDto.getExpected().getDistance());
        assertThat(actual.getExpected().getCost()).isEqualTo(cityDto.getExpected().getCost());
        assertThat(actual.getPurpose().getId()).isEqualTo(cityDto.getPurpose().getId());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().getId()).isEqualTo(cityDto.getPassenger().id());
        assertThat(actual.getAuthor().getFirstName()).isEqualTo(cityDto.getAuthor().firstName());
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().getId());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().getId());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isEqualTo(actual.getTariffId());
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }

    @DisplayName("Изменение - транспортные карты - успех")
    @Test
    @Transactional
    void editTravelCardPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO beforeUpdateCardsDto = postCompensationRequest(travelCardCompensationDTO);
        
        RequestForCompensationDTO cardsDto = RequestForCompensationDTO.builder()
                                                                      .author(employeeDTO1)
                                                                      .passenger(employeeDTO3)
                                                                      .desiredDate(LocalDateTime.now())
                                                                      .expected(expectedDataDTO2)
                                                                      .waypoints(List.of(waypointDto2, waypointDto3))
                                                                      .segments(List.of(segmentDTO2))
                                                                      .purpose(purposeDTO2)
                                                                      .compensationDocuments(docsDto2)
                                                                      .transportCompensation(beforeUpdateCardsDto.getTransportCompensation())
                                                                      .tariffId(publicTariff.getId())
                                                                      .build();
        cardsDto.setId(beforeUpdateCardsDto.getId());
        cardsDto.setHumanReadableId(beforeUpdateCardsDto.getHumanReadableId());
        cardsDto.setStatus(beforeUpdateCardsDto.getStatus());
        cardsDto.setCreationTime(beforeUpdateCardsDto.getCreationTime());
        
        RequestForPublic actual = putCompensationRequest(cardsDto, beforeUpdateCardsDto.getId());
        
        assertThat(actual.getSegmentsJSON().getFirst().getCost()).isEqualTo(cardsDto.getSegments().getFirst().getCost());
        assertThat(actual.getSegmentsJSON().getFirst().getDistance())
                .isEqualTo(cardsDto.getSegments().getFirst().getDistance());
        assertThat(actual.getWaypoints()).hasSameSizeAs(cardsDto.getWaypoints());
        //проверить изменения вейпойнтов
        assertThat(actual.getWaypoints().getFirst().getAddress().getStreet())
                .isEqualTo(cardsDto.getWaypoints().getFirst().getStreet());
        assertThat(actual.getWaypoints().get(1).getAddress().getBuilding())
                .isEqualTo(cardsDto.getWaypoints().get(1).getBuilding());
        
        assertThat(actual.getHumanReadableId()).isNotBlank();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getCreationTime()).isNotNull();
        assertThat(actual.getStatus()).isNotNull();
        assertThat(actual.getExpected().getDistance()).isEqualTo(cardsDto.getExpected().getDistance());
        assertThat(actual.getPurpose().getId()).isEqualTo(cardsDto.getPurpose().getId());
        assertThat(actual.getDesiredDate()).isNotNull();
        assertThat(actual.getPassenger().getId()).isEqualTo(cardsDto.getPassenger().id());
        assertThat(actual.getAuthor().getId()).isEqualTo(cardsDto.getAuthor().id());
        
        assertThat(actual.getCompensationDocuments()).hasSameSizeAs(cardsDto.getCompensationDocuments());
        //проверить изменения документов
        assertThat(actual.getCompensationDocuments().getFirst().getId())
                .isEqualTo(cardsDto.getCompensationDocuments().getFirst().getId());
        assertThat(actual.getCompensationDocuments().get(1).getId())
                .isEqualTo(cardsDto.getCompensationDocuments().get(1).getId());
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().getId());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().getId());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getTariffId()).isNotNull();
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
    }
    
    @DisplayName("Получение - транспортные карты - успех")
    @Test
    @Transactional
    void getTravelCardPublicRequest_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO travelCardPost = postCompensationRequest(travelCardCompensationDTO);
        RequestForCompensationDTO travelCardRequest = getCompensationRequest(travelCardPost.getId());
        
        assertThat(travelCardRequest.getId()).isNotNull();
        assertThat(travelCardRequest.getId()).isEqualTo(travelCardPost.getId());
    }
    
    @Transactional
    @DisplayName("Изменение - городской транспорт - валидация")
    @Test
    void testValidationForCityPublicRequest_update() throws Exception {
        commonUpdateValidationTest(COMPENSATION_URL + "/" + USER1_ID_STR, NewRequestForCompensationDTO.builder().build());
    }
    
    @Transactional
    @DisplayName("Подтверждение - пригородный транспорт - успех")
    @Test
    void confirmTest_suburb_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO compensation = postCompensationRequest(suburbTripCompensationDTO);
        
        RequestForPublic requestFromDb = requestForPublicRepository.findById(compensation.getId()).orElse(null);
        assertThat(requestFromDb).isNotNull();
        requestFromDb.setStatus(TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
        requestForPublicRepository.save(requestFromDb);
        
        String url = COMPENSATION_URL + "/" + requestFromDb.getId() + "/confirm";
        
        List<CompensationDocumentDTO> newDoc = entityDTOMapper.documentsToDto(List.of(doc3));
        String request = objectMapper.writeValueAsString(newDoc);
        
        mockMvc.perform(
                       post(url).content(request).characterEncoding("UTF-8")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        
        RequestForPublic actual = requestForPublicRepository.findById(compensation.getId()).orElse(null);
        
        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(actual.getCompensationDocuments()).contains(doc3);
        assertThat(actual.getCompensationDocuments()).hasSize(3);
        
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().getId());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().getId());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
        
        //проверить сообщение о создании нового документа + предыдущие сообщения
        var allDocsDTOs = entityDTOMapper.documentsToDto(actual.getCompensationDocuments());
        checkSendsDocuments(UUID.fromString(USER1_ID_STR), actual.getId(), allDocsDTOs, List.of(), 3);
    }

    @Transactional
    @DisplayName("Подтверждение - транспортные карты - успех")
    @Test
    void confirmTest_travelCards_success() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO compensation = postCompensationRequest(travelCardCompensationDTO);
        
        RequestForPublic actual = requestForPublicRepository.findById(compensation.getId()).orElse(null);
        assertThat(actual).isNotNull();
        actual.setStatus(TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
        requestForPublicRepository.save(actual);
        
        String url = COMPENSATION_URL + "/" + actual.getId() + "/confirm";
        
        List<CompensationDocumentDTO> newDoc = entityDTOMapper.documentsToDto(List.of(doc3));
        String request = objectMapper.writeValueAsString(newDoc);
        
        mockMvc.perform(
                       post(url).content(request).characterEncoding("UTF-8")
                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isOk())
               .andReturn();
        
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(actual.getCompensationDocuments()).contains(doc3);
        assertThat(actual.getCompensationDocuments()).hasSize(1);
        
        //проверить сообщение об изменении Заявки
        var requestMessageCaptor = ArgumentCaptor.forClass(RequestMessage.class);
        var headerCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestOutput, times(2)).send(requestMessageCaptor.capture(), headerCaptor.capture());
        var message = requestMessageCaptor.getValue();
        assertThat(headerCaptor.getValue()).containsEntry("transportType", actual.getTransportType().name());
        
        assertThat(message.getId()).isEqualTo(actual.getId());
        assertThat(message.getAuthorId()).isEqualTo(actual.getAuthor().getId());
        assertThat(message.getPassengerId()).isEqualTo(actual.getPassenger().getId());
        assertThat(message.getExpected().getCost()).isEqualTo(actual.getExpected().getCost());
        assertThat(message.getPurposeId()).isEqualTo(actual.getPurpose().getId());
        assertThat(message.getStatus()).isEqualTo(actual.getStatus().name());
        assertThat(message.getWaypoints()).hasSameSizeAs(actual.getWaypoints());
        assertThat(message.getTransportType()).isEqualTo(actual.getTransportType().name());
        
        //проверить сообщение о создании нового документа + предыдущие сообщения
        var allDocsDTOs = entityDTOMapper.documentsToDto(actual.getCompensationDocuments());
        checkSendsDocuments(UUID.fromString(USER1_ID_STR), actual.getId(), allDocsDTOs, List.of(), 1);
    }
    
    @Transactional
    @DisplayName("Подтверждение - пригородный транспорт - некорректный статус Заявки")
    @Test
    void confirmTest_suburb_incorrectStatus() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        //статус AWAITING_APPROVAL является некорректным
        RequestForCompensationDTO compensation = postCompensationRequest(travelCardCompensationDTO);
        RequestForPublic requestFromDb = requestForPublicRepository.findById(compensation.getId()).orElse(null);
        assertThat(requestFromDb).isNotNull();
        String url = COMPENSATION_URL + "/" + requestFromDb.getId() + "/confirm";
        
        List<CompensationDocumentDTO> newDoc = entityDTOMapper.documentsToDto(List.of(doc3));
        String request = objectMapper.writeValueAsString(newDoc);
        
        var exception = mockMvc.perform(
                                       post(url).content(request).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON_VALUE)
                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER"))))
                               .andExpect(status().is4xxClientError())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(IllegalStateResponseException.class);
    }

    @Transactional
    @DisplayName("Подтверждение - проездной документ - одинаковые даты")
    @Test
    void confirmTest_travel_duplicateData() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        RequestForCompensationDTO compensation = postCompensationRequest(travelCardCompensationDTO);
        RequestForPublic requestFromDb = requestForPublicRepository.findById(compensation.getId()).orElse(null);
        assertThat(requestFromDb).isNotNull();
        
        String request = objectMapper.writeValueAsString(travelCardCompensationDTO);
        
        var exception = mockMvc.perform(
                                       post(COMPENSATION_URL).content(request).characterEncoding("UTF-8")
                                                             .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                               .andExpect(status().is4xxClientError())
                               .andReturn().getResolvedException();
        assertThat(exception).isInstanceOf(IllegalStateResponseException.class);
    }
    
    
    private RequestForCompensationDTO postCompensationRequest(NewRequestForCompensationDTO dto) throws Exception {
        String request = objectMapper.writeValueAsString(dto);
        
        MvcResult result = mockMvc.perform(
                                          post(COMPENSATION_URL).content(request).characterEncoding("UTF-8")
                                                                .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER")))
                                                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                                  .andExpect(status().isOk())
                                  .andReturn();
        
        return objectMapper.readValue(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8), RequestForCompensationDTO.class);
    }
    
    
    private RequestForCompensationDTO getCompensationRequest(UUID requestId) throws Exception {
        String url = COMPENSATION_URL + "/" + requestId;
        
        ResultActions resultActions = mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON_VALUE)
                                                              .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER"))))
                                             .andExpect(status().isOk());
        
        return objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                      RequestForCompensationDTO.class);
    }
    
    private RequestForPublic putCompensationRequest(RequestForCompensationDTO dto, UUID requestId)
            throws Exception {
        var request = objectMapper.writeValueAsString(dto);
        String url = COMPENSATION_URL + "/" + requestId;
        
        mockMvc.perform(
                       put(url).content(request).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON_VALUE)
                               .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER"))))
               .andExpect(status().isOk())
               .andReturn();
        
        return requestForPublicRepository.findById(requestId).orElse(null);
    }
    
    /**
     * Общий метод теста валидации для контроллеров при создании Заявки
     *
     * @param url String
     * @param dto NewRequestForPublicDTO
     *
     * @throws Exception ошибка
     */
    private void commonCreateValidationTest(String url, NewRequestForPublicDTO dto) throws Exception {
        var request = objectMapper.writeValueAsString(dto);
        
        var ex = mockMvc.perform(
                                post(url).content(request).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON_VALUE)
                                         .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER"))))
                        .andExpect(status().is4xxClientError())
                        .andReturn().getResolvedException();
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    /**
     * Общий метод теста валидации для контроллеров при изменении Заявки
     *
     * @param url String
     * @param dto NewRequestForPublicDTO
     *
     * @throws Exception ошибка
     */
    private void commonUpdateValidationTest(String url, NewRequestForPublicDTO dto) throws Exception {
        var request = objectMapper.writeValueAsString(dto);
        
        var ex = mockMvc.perform(
                                put(url).content(request).characterEncoding("UTF-8").contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .with(jwt().jwt(builder -> builder.jti(USER1_ID_STR).claim("roles", "ROLE_USER"))))
                        .andExpect(status().is4xxClientError())
                        .andReturn().getResolvedException();
        assertThat(ex).isInstanceOf(MethodArgumentNotValidException.class);
    }
    
    private void checkSendsDocuments(
            UUID userId, UUID requestId,
            List<CompensationDocumentDTO> newDocuments
                                    ) {
        checkSendsDocuments(userId, requestId, newDocuments, new ArrayList<>());
    }
    
    private void checkSendsDocuments(
            UUID userId, UUID requestId,
            List<CompensationDocumentDTO> newDocuments,
            List<CompensationDocumentDTO> deleteDocuments
                                    ) {
        checkSendsDocuments(userId, requestId, newDocuments, deleteDocuments, 2);
    }
    
    /**
     * Проверяем, что документы были отосланы
     *
     * @param userId id пользователя
     * @param requestId id заявки
     * @param newDocuments новые документы для компенсации
     * @param deleteDocuments документы для компенсации для удаления
     */
    private void checkSendsDocuments(
            UUID userId, UUID requestId,
            List<CompensationDocumentDTO> newDocuments,
            List<CompensationDocumentDTO> deleteDocuments,
            int times
                                    ) {
        var allExpectedDocuments = new ArrayList<>(newDocuments);
        allExpectedDocuments.addAll(deleteDocuments);
        // Проверяем, что документы были отосланы
        var employee = employeeRepository.findByUserId(userId);
        assertThat(employee).isPresent();
        UUID callEmployeeId = employee.get().getId();
        // 1. Получаем все сообщения
        var documentRequestMessageCaptor = ArgumentCaptor.forClass(RequestDocumentMessage.class);
        verify(requestDocumentOutput, times(times)).send(documentRequestMessageCaptor.capture());
        List<RequestDocumentMessage> messages = documentRequestMessageCaptor.getAllValues();
        assertThat(messages).hasSameSizeAs(allExpectedDocuments);
        // 2. Проверяем, что сообщения есть на все документы
        allExpectedDocuments.forEach(doc ->
                                             assertThat(messages.stream().anyMatch(
                                                     message -> message.getDocumentId().equals(doc.getId()))).isTrue()
                                    );
        // 3. Проверяем, что выставлен правильный requestId
        messages.forEach(message ->
                                 assertThat(message.getRequestId()).isEqualTo(requestId));
        // 4. Проверяем, что выставлен правильный employeeId
        messages.forEach(message ->
                                 assertThat(message.getEmployeeId()).isEqualTo(callEmployeeId));
        // 5. Проверяем, что сообщение на создание или удаление
        Map<UUID, RequestDocumentMessage> documentId2Message =
                messages.stream().collect(Collectors.toMap(RequestDocumentMessage::getDocumentId, Function.identity()));
        newDocuments.forEach(doc -> assertThat(documentId2Message.get(doc.getId()).isDeleted()).isFalse());
        deleteDocuments.forEach(doc -> assertThat(documentId2Message.get(doc.getId()).isDeleted()).isTrue());
        
    }
}