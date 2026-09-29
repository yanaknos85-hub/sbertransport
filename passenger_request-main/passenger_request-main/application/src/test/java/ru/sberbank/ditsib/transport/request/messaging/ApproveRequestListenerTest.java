package ru.sberbank.ditsib.transport.request.messaging;

import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.dao.approvals.settings.PublicTrApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationStatus;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveTripRequestMessage;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;
import ru.sberbank.ditsib.transport.request.shared.TestSharedData;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения аппрувов")
@Transactional
@Slf4j
class ApproveRequestListenerTest extends SharedTest {
    
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();
    
    @Autowired
    @Qualifier("approveRequestInput")
    private Consumer<Message<ApproveTripRequestMessage>> approveRequestInput;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private TripPurposeRepository tripPurposeRepository;
    
    @Autowired
    private PublicTrApprovalsSettingsRepository publicApprovalsSettingsRepository;
    
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ContractorRepository contractorRepository;
    
    private Function<OldReserve.LimitReservationRequest, OldReserve.LimitReservationResponse> limitReservationFunction;
    
    private final TestSharedData sharedData = new TestSharedData();
    
    void initApprovalsSettings(
            ) {
        PublicTrApprovalsSettings publicSettings = sharedData.createPublicSettings(
                true, 0, organization1.getId(), true,
                true, false, true, null);
        publicApprovalsSettingsRepository.save(publicSettings);
    }
    
    @BeforeEach
    void setup() throws IOException {
        
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
    
    @Test
    @DisplayName("Approve taxi")
    @WithMockUser(USER1_ID)
    void handleApprove() {
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        request1.setHumanReadableId("123");
        request1.setPurpose(purpose);
        request1.setContractorId(contractor.getId());
        requestRepository.saveAndFlush(request1);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.TAXI_APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(request1.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNotNull();
        assertThat(requestRepository.findAll().getFirst().getApprovedBy().getId()).isEqualTo(testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Approve public city request")
    @WithMockUser(USER1_ID)
    void handleApprovePublicCity() {
        List<TransportCompensation> cityTripCompensationList = List.of(
                TransportCompensation
                        .builder()
                        .compensationType(PublicCompensationType.CITY_TRIP_COMPENSATION)
                        .transportType(PublicTransportType.CITY_BUS)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build()
                                                                      );
        
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee5);
        publicRequest.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        publicRequest.setHumanReadableId("123");
        publicRequest.setPurpose(purpose);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.setTimeZone("GMT+3");
        publicRequest.getTransportCompensation().addAll(cityTripCompensationList);
        publicRequest.setTimeZone("UTC");
        requestRepository.saveAndFlush(publicRequest);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(publicRequest.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        Request actual = requestRepository.findAll().getFirst();
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
        assertThat(actual.getApprovalState()).isEqualTo(ApprovalState.APPROVED);
        assertThat(actual.getApprovedBy()).isNotNull();
        assertThat(actual.getApprovedBy().getId()).isEqualTo(testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Approve public card request")
    @WithMockUser(USER1_ID)
    void handleApprovePublicCard() {
        List<TransportCompensation> travelCardCompensationList = List.of(
                TransportCompensation
                        .builder()
                        .compensationType(PublicCompensationType.TRAVEL_CARD_COMPENSATION)
                        .transportType(PublicTransportType.TRAVEL_CARD_BUS)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build()
                                                                        );
        
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee5);
        publicRequest.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        publicRequest.setHumanReadableId("123");
        publicRequest.setPurpose(purpose);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.setTimeZone("GMT+3");
        publicRequest.getTransportCompensation().addAll(travelCardCompensationList);
        publicRequest.setTimeZone("UTC");
        requestRepository.saveAndFlush(publicRequest);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(publicRequest.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
//        requestRepository.flush();

        assertThat(requestRepository.count()).isEqualTo(1);
        Request actual = requestRepository.findAll().getFirst();
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
        assertThat(actual.getApprovalState()).isEqualTo(ApprovalState.APPROVED);
        assertThat(actual.getApprovedBy()).isNotNull();
        assertThat(actual.getApprovedBy().getId()).isEqualTo(testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Approve public Suburb request")
    @WithMockUser(USER1_ID)
    void handleApprovePublicSuburb() {
        List<TransportCompensation> suburbTripCompensationList = List.of(
                TransportCompensation
                        .builder()
                        .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                        .transportType(PublicTransportType.SUBURB_BUS)
                        .ticketsCost(350)
                        .ticketsCount(1)
                        .build()
                                                                        );
        
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee5);
        publicRequest.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        publicRequest.setHumanReadableId("123");
        publicRequest.setPurpose(purpose);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.setTimeZone("GMT+3");
        publicRequest.getTransportCompensation().addAll(suburbTripCompensationList);
        requestRepository.saveAndFlush(publicRequest);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(publicRequest.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
//        requestRepository.flush();

        assertThat(requestRepository.count()).isEqualTo(1);
        Request actual = requestRepository.findAll().getFirst();
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_TRIP_CONFIRMATION);
        assertThat(actual.getApprovalState()).isEqualTo(ApprovalState.APPROVED);
        assertThat(actual.getApprovedBy()).isNotNull();
        assertThat(actual.getApprovedBy().getId()).isEqualTo(testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Approve public Suburb request - without Confirmation")
    @WithMockUser(USER1_ID)
    void handleApprovePublicSuburb_withoutConfirmation() {
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee5);
        publicRequest.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        publicRequest.setHumanReadableId("123");
        publicRequest.setPurpose(purpose);
        publicRequest.setDesiredDate(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        publicRequest.getTransportCompensation()
                     .add(
                             TransportCompensation.builder()
                                                  .transportType(PublicTransportType.SUBURB_TRAIN)
                                                  .request(publicRequest)
                                                  .ticketsCost(0)
                                                  .compensationType(PublicCompensationType.SUBURB_TRIP_COMPENSATION)
                                                  .build()
                         );
        requestRepository.saveAndFlush(publicRequest);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();
        
        // Отменим этап подтверждения в настройках
        initApprovalsSettings();
        

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(publicRequest.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(requestRepository.count()).isEqualTo(1);
        Request actual = requestRepository.findAll().getFirst();
        assertThat(actual.getStatus()).isEqualTo(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        assertThat(actual.getApprovalState()).isEqualTo(ApprovalState.APPROVED);
        assertThat(actual.getApprovedBy()).isNotNull();
        assertThat(actual.getApprovedBy().getId()).isEqualTo(testEmployee1.getId());
    }
    
    @Test
    @DisplayName("Approve finished")
    @WithMockUser(USER1_ID)
    void handleApproveFinished() {
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        request1.setHumanReadableId("123");
        request1.setPurpose(purpose);
        request1.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        request1.setContractorId(contractor.getId());
        requestRepository.saveAndFlush(request1);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_FINISHED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        var message = ApproveTripRequestMessage.builder()
                                               .approved(true)
                                               .actionId(request1.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
//        requestRepository.flush();

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_FINISHED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();
    }
    
    @Test
    @DisplayName("Decline")
    @WithMockUser(USER1_ID)
    void handleDecline() {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.LIMIT_CANCELLED.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        request1.setHumanReadableId("123");
        request1.setPurpose(purpose);
        request1.setContractorId(contractor.getId());
        requestRepository.saveAndFlush(request1);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isNotEqualTo(TripRequestStatus.TAXI_CANCELLED);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isNotEqualTo(ApprovalState.APPROVED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();

        final String reason = "Test message: 10635";
        var message = ApproveTripRequestMessage.builder()
                                               .approved(false)
                                               .actionId(request1.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .message(reason)
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
        requestRepository.flush();

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_CANCELLED);
        assertThat(requestRepository.findAll().getFirst().getApprovalState()).isEqualTo(ApprovalState.DECLINED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();
        
        assertThat(((RequestForTaxi) requestRepository.findAll().getFirst()).getHistoryItemsForTaxi().stream()
                                                                        .filter(item -> item.getComment().contains(reason) &&
                                                                                        (null != item.getInitiator()) &&
                                                                                        (item.getInitiator().equals(testEmployee1.getId())))
                                                                        .count()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("Decline finished")
    @WithMockUser(USER1_ID)
    void handleDeclineFinished() {
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.saveAndFlush(testEmployee1);
        employeeRepository.saveAndFlush(testEmployee2);
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        request1.setHumanReadableId("123");
        request1.setPurpose(purpose);
        request1.setStatus(TripRequestStatus.TAXI_TRIP_FINISHED);
        request1.setContractorId(contractor.getId());
        requestRepository.saveAndFlush(request1);

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_FINISHED);

        final String reason = "Test message: 10635";
        var message = ApproveTripRequestMessage.builder()
                                               .approved(false)
                                               .actionId(request1.getId())
                                               .actorEmployeeId(testEmployee1.getId())
                                               .message(reason)
                                               .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
        requestRepository.flush();

        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.TAXI_TRIP_FINISHED);
        assertThat(requestRepository.findAll().getFirst().getApprovedBy()).isNull();
    }
}
