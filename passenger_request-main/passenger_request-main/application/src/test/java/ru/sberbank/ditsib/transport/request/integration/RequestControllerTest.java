package ru.sberbank.ditsib.transport.request.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.limits.grpc.LimitReservationServiceGrpc;
import ru.sber.transport.limits.grpc.OldReserve;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.dto.*;
import ru.sberbank.ditsib.transport.request.dto.fraud.EasupAbsenceRequest;
import ru.sberbank.ditsib.transport.request.messaging.resolvers.SrmResolver;
import ru.sberbank.ditsib.transport.request.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.request.service.grpc.RequestChecksGrpcService;
import ru.sberbank.ditsib.transport.request.service.ReservationService;
import ru.sberbank.ditsib.transport.request.service.grpc.EasupGrpcService;
import ru.sberbank.ditsib.transport.request.service.impl.ReservationServiceImpl;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.request.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@AutoConfigureMockMvc
@MockitoBean(types = JwtDecoder.class)
@MockitoBean(types = LimitReservationServiceGrpc.LimitReservationServiceBlockingStub.class)
class RequestControllerTest extends KafkaTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ReservationService reservationService;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private RegionDataResolver regionDataResolver;
    @MockitoBean
    private SrmResolver srmResolver;
    @MockitoBean
    private EasupGrpcService easupGrpcService;
    @MockitoBean
    private RequestChecksGrpcService requestChecksGrpcService;
    private Function<OldReserve.LimitReservationRequest, OldReserve.LimitReservationResponse> limitReservationFunction;
    @RegisterExtension
    private static final GrpcCleanupExtension grpcCleanupExtension = new GrpcCleanupExtension();

    @SneakyThrows
    @Test
    @Sql({
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql"
    })
    void frequentlyTripPurpose() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        mockMvc.perform(get("/frequentlyTripPurpose")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("91bf3e90-0e53-4ea4-b53e-57c68f77c615"))
                .andExpect(jsonPath("$.label").value("Тестовый_департамент_01"));
    }

    @SneakyThrows
    @Test
    @Sql({
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql"
    })
    void addNewRequestTOSharedRide() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var employeeId = UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8");
        var newRequestDTO = Instancio.of(NewRequestDTO.class)
                .set(field(NewRequestDTO::getTimeZone), "+3")
                .set(field(NewRequestDTO::getTransportType), TransportTypeEnum.TAXI)
                .set(field(NewRequestDTO::getTaxiClass), TaxiClass.ECONOMY)
                .set(field(NewRequestDTO::getTariffId), UUID.fromString("4b6f382d-f6fa-4478-88d5-9c62b25fe16b"))
                .set(field(NewRequestDTO::getOutcomeTariffId), UUID.fromString("4b6f382d-f6fa-4478-88d5-9c62b25fe16b"))
                .set(field(NewRequestDTO::getPurpose), Instancio.of(TripPurposeDTO.class)
                        .set(field(TripPurposeDTO::getId), UUID.fromString("91bf3e90-0e53-4ea4-b53e-57c68f77c615"))
                        .create())
                .set(field(NewRequestDTO::getExpected), Instancio.of(ExpectedDataDTO.class)
                        .set(field(ExpectedDataDTO::getCost), 50.0)
                        .set(field(ExpectedDataDTO::getBonusCost), 10L)
                        .create())
                .set(field(NewRequestDTO::getPassenger), Instancio.of(EmployeeDTO.class)
                        .set(field(EmployeeDTO::id), employeeId)
                        .create())
                .create();
        var regionDtoList = Collections.singletonList(Instancio.of(RegionDto.class)
                .set(field(RegionDto::getTimeZone), "+3")
                        .create());
        doReturn(regionDtoList).when(regionDataResolver).getRegionBranch(any(WaypointDTO.class));
        doReturn(Instancio.create(SrmSharedRideDTO.class)).when(srmResolver).joinRequest(any(UUID.class), any(SrmRequestDTO.class), anyString());
        setLimitReservation();
        doReturn(Optional.empty()).when(easupGrpcService).resolveAbsence(any(EasupAbsenceRequest.class));
        mockMvc.perform(post("/shared/" + UUID.randomUUID())
                        .with(jwt().jwt(builder -> builder.jti(employeeId.toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(newRequestDTO)))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    @Sql({
            "/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/request_for_taxi.sql"
    })
    void changeRequestStatus() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        mockMvc.perform(post("/status/7ffd0e5c-b2d9-4c5c-b206-804c34c87617/" + TripRequestStatus.TAXI_DRIVER_ON_THE_WAY.name())
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(Instancio.of(ChangeStatusDTO.class))))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    private void setLimitReservation() {
        limitReservationFunction = request -> OldReserve.LimitReservationResponse
                .newBuilder()
                .setLimitReservationStatus(LimitReservationStatus.RESERVED_FROM_EMPLOYEE.name())
                .setTripRequestId(request.getRequestId())
                .setMessage(OldReserve.NullableString.newBuilder().setData("Message"))
                .build();
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
}