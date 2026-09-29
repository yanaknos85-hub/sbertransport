package ru.sber.transport.trip.providers.srm;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Lookup;
import ru.sber.transport.grpc.client.GrpcClient;
import ru.sber.transport.srm.grpc.dto.SrmDescriptor;
import ru.sber.transport.srm.grpc.service.SrmServiceGrpc;
import ru.sber.transport.trip.business.model.Request;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.Waypoint;
import ru.sber.transport.trip.business.providers.SrmProvider;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация запросов к SRM.
 */
@Slf4j
@GrpcClient(serverId = "grpc-srm", handler = SrmServiceGrpc.SrmServiceFutureStub.class)
@RequiredArgsConstructor
class SrmProviderImpl implements SrmProvider {

    @Override
    public CompletableFuture<Trip> update(Trip trip) {
        var response = stub().getRequest(createRequest(trip.getId()));
        return mapResponse(trip, response);
    }

    private CompletableFuture<Trip> mapResponse(Trip trip, ListenableFuture<SrmDescriptor.SrmGetResponse> response) {
        var completionFuture = new CompletableFuture<Trip>();
        Futures.addCallback(response, new FutureCallback<>() {

            @Override
            public void onSuccess(SrmDescriptor.SrmGetResponse resp) {
                var waypointList = new LinkedList<Waypoint>();

                if (resp == null) {
                    throw new IllegalStateException("Data from SRM has not been received");
                }
                var waypoints = trip.getRequests().stream().map(Request.class::cast).map(Request::getWaypoints).flatMap(Collection::stream).toList(); // NOSONAR
                var idWaypoints = waypoints.stream().collect(Collectors.toMap(Waypoint::id, Function.identity())); // NOSONAR
                var waypointsList = resp.getWaypointsList();
                for (var waypoint : waypointsList) {
                    var idWaypoint = idWaypoints.get(UUID.fromString(waypoint.getId()));
                    if (idWaypoint != null) {
                        waypointList.add(idWaypoint);
                    } else {
                        waypointList.add(waypoints.get(waypointsList.indexOf(waypoint)));
                    }
                }

                var srmWaypoints = new LinkedList<>(waypointsList);
                if (!waypointList.isEmpty()) {
                    var startTime = srmWaypoints.getFirst().getStartTime();
                    var endTime = srmWaypoints.getLast().getEndTime();
                    trip.setExpectedStartTime(Instant.ofEpochSecond(startTime.getSeconds(), startTime.getNanos()).atZone(ZoneOffset.UTC).toOffsetDateTime());
                    trip.setExpectedEndTime(Instant.ofEpochSecond(endTime.getSeconds(), endTime.getNanos()).atZone(ZoneOffset.UTC).toOffsetDateTime());
                }

                trip.setWaypoints(waypointList);
                trip.setPassengerCount(trip.getRequests().stream().map(Request.class::cast).mapToInt(Request::getPassengerCount).sum());
                trip.setExpectedCost(resp.getRideCost());
                trip.setExpectedDistance(resp.getRideDistance());
                trip.setExpectedTime(resp.getRideTime());
                completionFuture.complete(trip);
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                log.error("Trip data loading failed", t);
                completionFuture.completeExceptionally(t);
            }
        }, Executors.newSingleThreadExecutor());
        return completionFuture;
    }

    private SrmDescriptor.SrmGetRequest createRequest(UUID rideId) {
        return SrmDescriptor.SrmGetRequest.newBuilder()
                .setSharedRideId(rideId.toString())
                .build();
    }

    @Lookup
    SrmServiceGrpc.SrmServiceFutureStub stub() {
        return null;
    }
}
