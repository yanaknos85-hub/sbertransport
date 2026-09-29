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
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.Waypoint;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.dto.LimitReservationStatus;
import ru.sberbank.ditsib.transport.request.messaging.message.ApproveFinalTripMessage;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения аппрувов финального маршрута")
@Transactional
@Slf4j
@MockitoBean(types = JwtDecoder.class)
class ApproveFinalTripListenerTest extends SharedTest {
    
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();
    
    @Autowired
    private Consumer<Message<ApproveFinalTripMessage>> approveRequestInput;
    
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
    private ContractorRepository contractorRepository;
    @Autowired
    private ReservationService reservationService;

    
    private Function<OldReserve.LimitReservationRequest, OldReserve.LimitReservationResponse> limitReservationFunction;
    
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
    @DisplayName("Approve")
    @WithMockUser(USER1_ID)
    void handleApprove() {
        // prepare data
        var contractor = contractorRepository.save(Instancio.create(Contractor.class));
        organizationRepository.save(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        request1.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        purpose.setOrganization(organization1.getId());
        purpose.setActive(true);
        tripPurposeRepository.saveAndFlush(purpose);
        request1.setHumanReadableId("123");
        request1.setPurpose(purpose);
        request1.setStatus(TripRequestStatus.TAXI_AWAITING_APPROVAL);
        request1.setContractorId(contractor.getId());
        requestRepository.saveAndFlush(request1);

        assertThat(requestRepository.count()).isEqualTo(1);

        var message = ApproveFinalTripMessage.builder()
                                             .approved(true)
                                             .requestId(request1.getId())
                                             .actorEmployeeId(testEmployee1.getId())
                                             .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());

        assertThat(requestRepository.count()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Approve public")
    @WithMockUser(USER1_ID)
    void handleApprovePublic() {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.LIMIT_SPENT.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
        // prepare data
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.save(department1);
        employeeRepository.save(testEmployee1);
        employeeRepository.save(testEmployee2);
        request9.getWaypoints().stream().map(Waypoint::getAddress).forEach(addressRepository::saveAndFlush);
        var purpose = new TripPurpose();
        purpose.setPurpose("Purpose");
        purpose.setId(UUID.randomUUID());
        tripPurposeRepository.saveAndFlush(purpose);
        request9.setHumanReadableId("123");
        request9.setPurpose(purpose);
        request9.setStatus(TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        request9.setPurpose(purpose);
        request9.setPassenger(employeeRepository.save(request9.getPassenger()));
        request9.setAuthor(employeeRepository.save(request9.getAuthor()));
        request9.getCompensationDocuments().clear();
        request9.setTimeZone("UTC");
        requestRepository.saveAndFlush(request9);
        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(
                TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE);
        var message = ApproveFinalTripMessage.builder()
                                             .approved(true)
                                             .requestId(request9.getId())
                                             .actorEmployeeId(testEmployee1.getId())
                                             .build();
        
        approveRequestInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(requestRepository.count()).isEqualTo(1);
        assertThat(requestRepository.findAll().getFirst().getStatus()).isEqualTo(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION);
    }
}
