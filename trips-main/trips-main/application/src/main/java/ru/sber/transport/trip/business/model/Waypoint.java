package ru.sber.transport.trip.business.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.sber.transport.trip.business.dto.PassengerActionType;
import ru.sber.transport.trip.business.dto.RequestDto;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record Waypoint(
        UUID id,
        double latitude,
        double longitude,
        int orderingIndex,
        String country,
        String region,
        String city,
        String street,
        String house,
        String building,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Duration waitingTime,
        String fullAddress,
        Contact contact,
        List<Passenger> passengers

) {

    /**
     * Контакт на точке маршрута
     * @deprecated используется только для обратной совместимости, актуальный класс - {@link Passenger}
     */
    @Deprecated(since = "D-04.008.000")
    public record Contact(

            String phone,

            String name
    ) {
    }

    public record Passenger(

            PassengerActionType type,

            String firstName,

            String patronymic,

            String phone
    ) {
    }

}
