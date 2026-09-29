package ru.sberbank.ditsib.transport.srm.grpc;

import com.google.protobuf.NullValue;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.srm.grpc.dto.SrmDescriptor;
import ru.sber.transport.srm.grpc.service.SrmServiceGrpc;
import ru.sber.transport.srm.model.SrmRequestKpiDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.srm.service.SrmService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@GrpcService
class SrmGrpcServerImpl extends SrmServiceGrpc.SrmServiceImplBase {

    private static final String GETTING_ADDRESSES_FAILED_MESSAGE = "Getting addresses failed";
    private final SrmService srmService;

    @Override
    @Transactional
    public void cancelRequest(SrmDescriptor.SrmCancelRequest request, StreamObserver<SrmDescriptor.SrmCancelResponse> responseObserver) {
        final var requestId = request.getRequestId();
        try {
            final var sharedRideDTO = srmService.cancelRequest(UUID.fromString(requestId));
            responseObserver.onNext(cancelRequestToGrpc(sharedRideDTO));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.warn(GETTING_ADDRESSES_FAILED_MESSAGE);
            responseObserver.onError(e);
        }
    }

    private SrmDescriptor.SrmCancelResponse cancelRequestToGrpc(SrmSharedRideDTO sharedRideDTO) {
        if (sharedRideDTO == null) {
            return SrmDescriptor.SrmCancelResponse.newBuilder()
                    .setResult(false)
                    .setOwnerRequest(getNullableString(null))
                    .setErrorDescription("exception while running cancel")
                    .build();
        }
        final var ownerRequest = sharedRideDTO.getRequestKpiList()
                .stream()
                .findFirst()
                .map(SrmRequestKpiDTO::getId)
                .map(UUID::toString)
                .orElse(null);
        return SrmDescriptor.SrmCancelResponse.newBuilder()
                .setResult(sharedRideDTO.isActive())
                .setOwnerRequest(getNullableString(ownerRequest))
                .setErrorDescription("")
                .build();
    }

    @Override
    @Transactional
    public void getRequest(SrmDescriptor.SrmGetRequest request, StreamObserver<SrmDescriptor.SrmGetResponse> responseObserver) {
        final var sharedRideId = request.getSharedRideId();
        try {
            final var sharedRideDTO = srmService.get(UUID.fromString(sharedRideId));
            responseObserver.onNext(sharedRideDtoToGrpc(sharedRideDTO));
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error(GETTING_ADDRESSES_FAILED_MESSAGE, e);
            responseObserver.onError(e);
        }
    }

    private SrmDescriptor.SrmGetResponse sharedRideDtoToGrpc(SrmSharedRideDTO sharedRideDTO) {
        final var waypointList = new ArrayList<SrmDescriptor.SrmWaypoint>();
        final var waypointFinalList = new ArrayList<SrmDescriptor.SrmWaypointFinal>();
        for (final var w : sharedRideDTO.getWaypointsFinal().stream().filter(Objects::nonNull).toList()) {
            final var waypoint = SrmDescriptor.SrmWaypoint.newBuilder()
                    .setId(w.getId().toString())
                    .setRequestKpiId(
                            w.getRequestDataList().get(0).getRequestKpiId().toString())
                    .setEventType(w.getEventType())
                    .setStartTime(convertZonedDateTimeToGoogleTimestamp(w.getStartTime()))
                    .setEndTime(convertZonedDateTimeToGoogleTimestamp(w.getEndTime()))
                    .setWaitingTime(w.getWaitingTime())
                    .setLatitude(w.getLatitude())
                    .setLongitude(w.getLongitude())
                    .setAddress(w.getAddress())
                    .setOrgOrderingIndex(0)
                    .setOrderingIndex(w.getOrderingIndex())
                    .setActive(true)
                    .build();
            waypointList.add(waypoint);

            waypointFinalList.add(SrmDescriptor.SrmWaypointFinal.newBuilder()
                    .setEventType(w.getEventType())
                    .setStartTime(convertZonedDateTimeToGoogleTimestamp(w.getStartTime()))
                    .setEndTime(convertZonedDateTimeToGoogleTimestamp(w.getEndTime()))
                    .setLatitude(w.getLatitude())
                    .setLongitude(w.getLongitude())
                    .setAddress(w.getAddress())
                    .setOrderingIndex(w.getOrderingIndex())
                    .setDistanceFromPrevWaypoint(w.getDistanceFromPrevWaypoint())
                    .addAllRequestDataList(w.getRequestDataList().stream()
                            .map(r -> SrmDescriptor.SrmWaypointFinal
                                    .SrmWaypointFinalRequest.newBuilder()
                                    .setEventType(
                                            r.getEventType())
                                    .setRequestKpiId(r
                                            .getRequestKpiId()
                                            .toString())
                                    .build())
                            .toList())
                    .build());
        }
        final var requestKpiList = new ArrayList<SrmDescriptor.SrmRequestKpi>();
        for (final var w : sharedRideDTO.getRequestKpiList().stream().filter(Objects::nonNull).toList()) {
            final var requestKpi = SrmDescriptor.SrmRequestKpi.newBuilder()
                    .setId(w.getId().toString())
                    .setOldId(getPrimitive(w.getOldId()))
                    .setRequiredPassengers(getPrimitive(w.getRequiredPassengers()))
                    .setRequiredVolume(getNullableDouble(w.getRequiredVolume()))
                    .setRequiredWeight(getNullableDouble(w.getRequiredWeight()))
                    .setPickupTime(convertZonedDateTimeToGoogleTimestamp(w.getPickupTime()))
                    .setDropTime(convertZonedDateTimeToGoogleTimestamp(w.getDropTime()))
                    .setRequestDistance(getPrimitive(w.getRequestDistance()))
                    .setRequestTime(getPrimitive(w.getRequestTime()))
                    .setRequestPrice(w.getRequestPrice())
                    .setCostSharePart(getPrimitive(w.getCostSharePart()))
                    .setSavingsCash(getPrimitive(w.getSavingsCash()))
                    .setSavingsProcents(getPrimitive(w.getSavingsProcents()))
                    .setCreationTime(convertLocalDateTimeToGoogleTimestamp(w.getCreationTime()))
                    .setOrderingIndex(w.getOrderingIndex())
                    .build();
            requestKpiList.add(requestKpi);
        }

        return SrmDescriptor.SrmGetResponse.newBuilder().setId(sharedRideDTO.getId().toString())
                .setOldId(getPrimitive(sharedRideDTO.getOldId()))
                .setTariffId(sharedRideDTO.getTariffId().toString())
                .setTimeZone(sharedRideDTO.getTimeZone())
                .setTransportType(sharedRideDTO.getTransportType().toString())
                .setRideCost(getPrimitive(sharedRideDTO.getRideCost()))
                .setRideDistance(getPrimitive(sharedRideDTO.getRideDistance()))
                .setRideTime(getPrimitive(sharedRideDTO.getRideTime()))
                .addAllWaypoints(waypointList)
                .addAllRequestKpis(requestKpiList)
                .setActive(sharedRideDTO.isActive())
                .addAllWaypointsFinal(waypointFinalList)
                .build();
    }

    // common methods
    private com.google.protobuf.Timestamp convertZonedDateTimeToGoogleTimestamp(ZonedDateTime zonedDateTime) {
        Instant instant;
        if (zonedDateTime != null) {
            instant = zonedDateTime.toInstant();
        } else {
            instant = Instant.ofEpochSecond(0);
        }

        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    protected com.google.protobuf.Timestamp convertLocalDateTimeToGoogleTimestamp(LocalDateTime localDateTime) {
        Instant instant;
        if (localDateTime != null) {
            instant = localDateTime.toInstant(ZoneOffset.UTC);
        } else {
            instant = Instant.ofEpochSecond(0);
        }
        return com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private SrmDescriptor.NullableString getNullableString(String source) {
        if (source == null) {
            return SrmDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return SrmDescriptor.NullableString.newBuilder().setData(source).build();
    }

    private SrmDescriptor.NullableDouble getNullableDouble(Double source) {
        if (source == null) {
            return SrmDescriptor.NullableDouble.newBuilder().setNull(NullValue.NULL_VALUE).build();
        }
        return SrmDescriptor.NullableDouble.newBuilder().setData(source).build();
    }

    private double getPrimitive(Double value) {
        return Objects.requireNonNullElse(value, 0D);
    }

    private int getPrimitive(Integer value) {
        return Objects.requireNonNullElse(value, 0);
    }

    private long getPrimitive(Long value) {
        return Objects.requireNonNullElse(value, 0L);
    }


}
