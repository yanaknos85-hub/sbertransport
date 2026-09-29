package ru.sber.transport.trip.business.model;

import lombok.Getter;
import lombok.NonNull;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum TripStatus {

    /**
     * Опубликовано.
     */
    SENT_TO_CONTRACTOR(false, null),

    /**
     * Ожидает назначения.
     */
    WAITING_FOR_ASSIGNMENT(false, 1, TripRequestStatus.TAXI_AWAITING_SEARCH, TripRequestStatus.TAXI_APPROVED, TripRequestStatus.GROUP_TRANSFER_AWAITING_SEARCH, TripRequestStatus.GROUP_TRANSFER_APPROVED),

    /**
     * Водитель найден.
     */
    DRIVER_ASSIGNED(false, 2, TripRequestStatus.TAXI_DRIVER_FOUND, TripRequestStatus.GROUP_TRANSFER_DRIVER_FOUND),

    /**
     * Водитель выехал.
     */
    DRIVER_ON_THE_WAY(false, 4, TripRequestStatus.TAXI_DRIVER_ON_THE_WAY, TripRequestStatus.GROUP_TRANSFER_DRIVER_ON_THE_WAY),

    /**
     * Водитель ожидает клиента.
     */
    DRIVER_ARRIVED(false, 5, TripRequestStatus.TAXI_DRIVER_ARRIVED, TripRequestStatus.GROUP_TRANSFER_DRIVER_ARRIVED),

    /**
     * Поездка началась.
     */
    TRIP_IN_PROGRESS(false, 6, TripRequestStatus.TAXI_TRIP_IN_PROGRESS, TripRequestStatus.GROUP_TRANSFER_TRIP_IN_PROGRESS),

    /**
     * Остановка в промежуточной точке маршрута.
     */
    INTERMEDIATE_WAYPOINT_ARRIVED(false, null),

    /**
     * Заказ выполнен.
     */
    ORDER_FINISHED(true, 7, TripRequestStatus.TAXI_TRIP_FINISHED, TripRequestStatus.GROUP_TRANSFER_TRIP_FINISHED),

    /**
     * Клиент отменил заказ.
     */
    ORDER_CANCELLED_BY_CLIENT(true, 8, TripRequestStatus.TAXI_CANCELLED, TripRequestStatus.GROUP_TRANSFER_CANCELLED),

    /**
     * Водитель отменил заказ.
     */
    ORDER_CANCELLED_BY_DRIVER(true, 9),

    /**
     * Заказ просрочен.
     */
    ORDER_EXPIRED(true, 10),

    /**
     * Неизвестный статус.
     */
    UNDEFINED(false, null);

    private final boolean terminal;

    private final Integer code;
    private final TripRequestStatus[] tripRequestStatuses;

    TripStatus(boolean terminal, Integer code, TripRequestStatus... statuses) {
        this.terminal = terminal;
        this.code = code;
        tripRequestStatuses = statuses;
    }

    public static TripStatus getByTripRequestStatus(@NonNull TripRequestStatus tripRequestStatus){
        for (var tripStatus : TripStatus.values()) {
            for (var requestStatus : tripStatus.getTripRequestStatuses()) {
                if (tripRequestStatus.equals(requestStatus)) {
                    return tripStatus;
                }
            }
        }
        return UNDEFINED;
    }

    public static List<TripStatus> getTerminalStatuses(){
        return Arrays.stream(TripStatus.values()).filter(TripStatus::isTerminal).collect(Collectors.toList()); //NOSONAR
    }

    public static List<String> getStringTerminalStatuses(){
        return getTerminalStatuses().stream().map(TripStatus::name).collect(Collectors.toList()); //NOSONAR
    }
}
