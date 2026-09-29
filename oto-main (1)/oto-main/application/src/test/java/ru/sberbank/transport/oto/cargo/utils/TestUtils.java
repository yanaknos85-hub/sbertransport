package ru.sberbank.transport.oto.cargo.utils;


import org.instancio.Instancio;
import org.instancio.Select;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.route.messaging.RouteMessage;

public class TestUtils {
    
    public static RouteMessage buildRouteMessage() {
        return Instancio.of(RouteMessage.class)
                .set(Select.field(RouteMessage::status), Instancio.create(TripRequestStatus.class).name())
                .create();
    }
}
