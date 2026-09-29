package ru.sberbank.ditsib.transport.request.messaging.message;

import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

public record CarLocationNotificationMessage(
        TripRequestStatus requestStatus,
        LocationMessage location,
        Integer duration
) {
    public record LocationMessage(
            double longitude,
            double latitude

    ) {
    }
}
