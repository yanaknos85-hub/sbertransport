package ru.sberbank.ditsib.transport.request.util;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.util.List;
import java.util.Set;

import static ru.sberbank.ditsib.transport.constants.TripRequestStatus.*;
import static ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus.*;

@UtilityClass
public class StatusProcessingHelper {

    private static final Set<TripRequestStatus> ACCEPTABLE_STATUSES_FOR_GENAI_CHECK =
            Set.of(PERSONAL_AWAITING_TRIP_APPROVAL, PERSONAL_TRIP_FINISHED,
                   PUBLIC_AWAITING_AFFIRMATIVE, PUBLIC_TRIP_CONFIRMATION);

    private static final List<InboundTaxiTripStatus> TAXI_TRIP_STATUS_ORDER = List.of(
            SENT_TO_CONTRACTOR,
            WAITING_FOR_ASSIGNMENT,
            DRIVER_ASSIGNED,
            DRIVER_APPROVED,
            DRIVER_ON_THE_WAY,
            DRIVER_ARRIVED,
            TRIP_IN_PROGRESS,
            ORDER_FINISHED,
            ORDER_CANCELLED_BY_CLIENT,
            ORDER_CANCELLED_BY_DRIVER,
            ORDER_EXPIRED);

    public boolean cantChangeTaxiTripStatus(InboundTaxiTripStatus newStatus, InboundTaxiTripStatus status) {
        if (newStatus == null) {
            return false;
        }
        if (status != null && status.equals(newStatus)) {
            return false;
        }
        if (status == null || UNDEFINED.equals(status)) {
            return false;
        }
        int newIdx = TAXI_TRIP_STATUS_ORDER.indexOf(newStatus);
        int curIdx = TAXI_TRIP_STATUS_ORDER.indexOf(status);
        return newIdx < curIdx;
    }

    public TripRequestStatus inboundTaxiTripStatusToGroupTransferStatus(InboundTaxiTripStatus status) {
        return switch (status) {
            case SENT_TO_CONTRACTOR, WAITING_FOR_ASSIGNMENT -> TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH;
            case DRIVER_ASSIGNED, DRIVER_APPROVED -> TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND;
            case DRIVER_ON_THE_WAY -> TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY;
            case DRIVER_ARRIVED -> TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED;
            case TRIP_IN_PROGRESS -> TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS;
            case ORDER_FINISHED -> TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED;
            default -> TripRequestStatus.GROUP_TRANSFER_CANCELLED;
        };
    }

    /**
     * Проверяет, можно ли перевести заявку в статус {@link TripRequestStatus#GENAI_CHECK}
     * из текущего статуса.
     *
     * @param currentStatus текущий статус заявки.
     * @return {@code true}, если переход в GENAI_CHECK разрешён.
     */
    public static boolean canChangeToGenaiCheck(TripRequestStatus currentStatus) {
        return currentStatus != null && ACCEPTABLE_STATUSES_FOR_GENAI_CHECK.contains(currentStatus);
    }
}
