package ru.sberbank.ditsib.transport.request.converter;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.Set;

@UtilityClass
public class StatusConverter {

    public static final Set<TripRequestStatus> DONE_STATUSES = Set.of(
            TripRequestStatus.CARSHARING_TRIP_FINISHED,
            TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED,
            TripRequestStatus.PERSONAL_TRIP_FINISHED,
            TripRequestStatus.PERSONAL_PAYMENT_DONE,
            TripRequestStatus.PERSONAL_PAYMENT_DECLINED,
            TripRequestStatus.PUBLIC_PAYMENT_DONE,
            TripRequestStatus.PUBLIC_PAYMENT_NOT_DONE,
            TripRequestStatus.TAXI_TRIP_FINISHED
    );

    public static final Set<TripRequestStatus> CANCELED_STATUSES = Set.of(
            TripRequestStatus.CARSHARING_CANCELLED,
            TripRequestStatus.GROUP_TRANSFER_CANCELLED,
            TripRequestStatus.PERSONAL_CANCELLED,
            TripRequestStatus.PUBLIC_CANCELLED,
            TripRequestStatus.TAXI_CANCELLED
    );

    public String tripRequestStatusToMetricsStatus(TripRequestStatus status) {
        if (CANCELED_STATUSES.contains(status)) {
            return "CANCELED";
        }
        if (DONE_STATUSES.contains(status)) {
            return "DONE";
        }
        return "IN_PROGRESS";
    }
}
