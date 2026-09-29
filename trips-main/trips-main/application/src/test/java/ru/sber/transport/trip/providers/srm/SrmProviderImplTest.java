package ru.sber.transport.trip.providers.srm;

import com.google.protobuf.Timestamp;
import io.grpc.BindableService;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.grpc.test.extension.GrpcCleanupExtension;
import ru.sber.transport.srm.grpc.dto.SrmDescriptor;
import ru.sber.transport.srm.grpc.service.SrmServiceGrpc;
import ru.sber.transport.trip.business.model.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка СРМ-провайдера")
@UnitTest
@IsolatedTest
@Feature("app_platform_trips")
class SrmProviderImplTest {

    @RegisterExtension
    private final GrpcCleanupExtension grpcCleanupRule = new GrpcCleanupExtension();

    @Test
    @DisplayName("Запрос. Сортировка по идентификаторам")
    void test_request() throws ExecutionException, InterruptedException {
        var requests = new HashSet<>(Instancio.ofSet(Request.class)
                .set(Select.field(Request::getStatus), TripRequestStatus.TAXI_AWAITING_SEARCH.name()).create());
        var waypoints = requests.stream().map(Request.class::cast).map(Request::getWaypoints).flatMap(Collection::stream).collect(Collectors.toList()); // NOSONAR

        var provider = new SrmProviderImpl() {

            @SneakyThrows
            @Override
            SrmServiceGrpc.SrmServiceFutureStub stub() {
                Collections.shuffle(waypoints);
                var channel = grpcCleanupRule.addService(createService(waypoints));
                return SrmServiceGrpc.newFutureStub(channel);
            }
        };

        var id = UUID.randomUUID();
        var trip = new Trip();
        trip.setId(id);
        trip.setTaxiClass(TaxiClass.ECONOMY.name());
        trip.setPassengerCount(2);
        trip.setExpectedStartTime(OffsetDateTime.now(ZoneOffset.UTC));
        trip.setExpectedEndTime(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
        trip.setRequests(requests);

        var updatedTrip = provider.update(trip).get();

        assertThat(updatedTrip.getId()).isEqualTo(id);
        assertThat(updatedTrip.getStatus()).isEqualTo(TripStatus.WAITING_FOR_ASSIGNMENT);
        assertThat(updatedTrip.getWaypoints()).hasSameSizeAs(waypoints);
        assertThat(updatedTrip.getWaypoints().stream().map(Waypoint::id).toList()).containsExactlyElementsOf(waypoints.stream().map(Waypoint::id).collect(Collectors.toList()));
        assertThat((updatedTrip.getPassengerCount())).isEqualTo(trip.getRequests().stream().map(Request.class::cast).mapToInt(Request::getPassengerCount).sum());
    }

    private BindableService createService(List<Waypoint> waypoints) {
        return new SrmServiceGrpc.SrmServiceImplBase() {

            @Override
            public void getRequest(SrmDescriptor.SrmGetRequest request, StreamObserver<SrmDescriptor.SrmGetResponse> responseObserver) {
                var builder = SrmDescriptor.SrmGetResponse.newBuilder()
                        .addRequestKpis(createRequestKpi())
                        .setActive(true)
                        .setId(UUID.randomUUID().toString())
                        .setRideCost(4)
                        .setRideDistance(5.5)
                        .setTariffId(UUID.randomUUID().toString())
                        .setTimeZone("TZ")
                        .setRideCost(6);
                waypoints.stream().map(this::convertWaypoint).forEach(builder::addWaypoints);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }

            private SrmDescriptor.SrmWaypoint convertWaypoint(Waypoint waypoint) {
                return SrmDescriptor.SrmWaypoint.newBuilder()
                        .setActive(true)
                        .setId(waypoint.id().toString())
                        .setLatitude(waypoint.latitude())
                        .setLongitude(waypoint.longitude())
                        .setAddress(createAddress(waypoint))
                        .build();
            }

        };
    }

    @NonNull
    private String createAddress(Waypoint waypoint) {
        return String.join(", ", waypoint.region(), waypoint.city(), waypoint.street(), waypoint.house(), waypoint.building());
    }

    private SrmDescriptor.SrmRequestKpi createRequestKpi() {
        return SrmDescriptor.SrmRequestKpi.newBuilder()
                .setCostSharePart(1.1)
                .setCreationTime(Timestamp.newBuilder().setNanos(1000).setSeconds(1000).build()).build();
    }

}