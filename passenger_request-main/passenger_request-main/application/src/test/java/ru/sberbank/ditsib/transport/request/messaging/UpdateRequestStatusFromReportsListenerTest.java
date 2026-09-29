package ru.sberbank.ditsib.transport.request.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.RequestOptions;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.message.UpdateTripRequestStatusMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;
import ru.sberbank.ditsib.transport.request.service.grpc.CorporateDocumentValidationGrpcClient;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.client.PersonalCarDataResolver;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;

import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@MockitoBean(types = LimitReservationServiceGrpc.LimitReservationServiceBlockingStub.class)
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@AutoConfigureMockMvc
@DisplayName("Проверка получения обновлений статуса заявок")
@Transactional
@Slf4j
@MockitoBean(types = JwtDecoder.class)
class UpdateRequestStatusFromReportsListenerTest extends SharedTest {
    
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EntityDTOMapper mapper;
    @Autowired
    private RequestRepository repository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private PersonalTariffRepository personalTariffRepository;
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    @MockitoBean
    private EasupGrpcService easupGrpcService;
    @MockitoBean
    private RequestChecksGrpcService requestChecksGrpcService;
    @MockitoBean
    private CorporateDocumentValidationGrpcClient corporateDocumentValidationGrpcClient;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @MockitoBean
    private PersonalCarDataResolver personalCarDataResolver;
    
    @Autowired
    @Qualifier("updateRequestStatusFromReportsInput")
    private Consumer<Message<UpdateTripRequestStatusMessage>> updateRequestStatusFromReportsInput;
    
    @Autowired
    private ReservationService reservationService;
    
    private Function<OldReserve.LimitReservationRequest, OldReserve.LimitReservationResponse> limitReservationFunction;
    
    @BeforeEach
    public void setUp() throws IOException {
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
        AuthorizeUtils.authorize(roleCheckService);
        testEmployee1.setUserId(userId);
        testEmployee2.setUserId(userId2);
        testEmployee3.setUserId(userId3);
        testEmployee4.setUserId(userId4);
        testEmployee1.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_1);
        testEmployee2.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_2);
        testEmployee3.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_3);
        testEmployee4.setHumanReadableId(HUMAN_READABLE_EMPLOYEE_ID_4);
        
        request1.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_1);
        request2.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_2);
        request3.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_3);
        request4.setHumanReadableId(HUMAN_READABLE_REQUEST_ID_4);
        
        
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
        
        var personalTariff = personalTariffRepository.saveAndFlush(
                PersonalTariff.builder()
                              .id(TARIFF_ID_1)
                              .humanReadableId("PT-123-23")
                              .departmentId(department1.getId())
                              .transportType(TransportTypeEnum.CARSHARING)
                              .rideCostPerKm(1)
                              .rideCostPerMin(2)
                              .build());
        
        RegionDto regionDto = RegionDto.builder().code("1").id(UUID.randomUUID()).name("moscow").build();
        when(regionDataResolver.getRegion(any())).thenReturn(regionDto);
        when(regionDataResolver.getRegionBranch(any(WaypointDTO.class))).thenReturn(List.of(
                RegionDto.builder()
                         .id(UUID.randomUUID())
                         .parentId(UUID.randomUUID())
                         .timeZone("+3")
                         .name("name")
                         .code("123")
                         .build()));
        
        var personalCarDTO = PersonalCarDTO.builder()
                                           .id(PERSONAL_CAR_ID)
                                           .registrationNumber("registrationNumber")
                                           .registrationCertificate("registrationCertificate")
                                           .build();
        when(personalCarDataResolver.getPersonalCar(any(UUID.class),
                                                    any(UUID.class),
                                                    any(UUID.class),
                                                    any(UUID.class),
                                                    anyString()))
                .thenReturn(personalCarDTO);
    }
    
    @Test
    @WithMockUser(username = USER1_ID, roles = "GUEST")
    @DisplayName("Проверка получения обновления статуса заявки")
    void handleUpdateRequestStatus() throws Exception {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        request4.setAuthor(testEmployee1);
        request4.setPassengerCount(2);
        request4.setDesiredDate(LocalDateTime.now(ZoneId.of("UTC")).plusHours(3).plusMinutes(5));
        request4.setTariffId(TARIFF_ID_1);
        request4.setCoopTrip(false);
        request4.setPassenger(testEmployee2);
        request4.getRequestOptions().add(RequestOptions.CHILD_SEAT);
        request4.setPurpose(TripPurpose.builder().id(purposeId1).purpose(PURPOSE_DESCRIPTION_1).build());
        request4.getWaypoints().getFirst().setWaitTime(Duration.ofSeconds(111));
        request4.setTimeZone("GMT+3");
        assertTrue(employeeRepository.findById(testEmployee2.getId()).isPresent());
        assertTrue(employeeRepository.findById(testEmployee1.getId()).isPresent());
        NewRequestDTO requestDTO = mapper.requestForPersonalToDTO(request4);//var requestDTO = mapper.requestToGetDTO(request4);
        var request = objectMapper.writeValueAsString(requestDTO);
        var initialCount = addressRepository.count();
        assertThat(addressRepository.count()).isEqualTo(initialCount);
        
        var response = mockMvc.perform(post("/")
                                               .with(jwt().jwt(builder -> builder.jti(USER1_ID).claim("roles", "ROLE_USER")))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE)
                                               .content(request))
                              .andExpect(status().isOk());
        var result = objectMapper.readValue(response.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                            GetRequestDTO.class);
        var message = UpdateTripRequestStatusMessage.builder().id(result.getId())
                                                    .status(TripRequestStatus.PERSONAL_PAYMENT_AWAITING.name())
                                                    .build();
        
        updateRequestStatusFromReportsInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(repository.count()).isEqualTo(1);
        var actual = repository.findAll().getFirst();
        assertThat(actual.getId()).isEqualTo(message.getId());
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.getFromString(message.getStatus()).orElseThrow());
    }
}
