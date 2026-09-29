package ru.sber.transport.request_checks.messaging.listeners;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.CARSHARING;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.GROUP_TRANSFER;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PERSONAL;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PUBLIC;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.Message;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request_checks.service.TripRequestService;

@Slf4j
@RequiredArgsConstructor
public class TripRequestListener implements Consumer<Message<RequestMessage>> {

    private static final Set<String> VALID_TRANSPORT_TYPES = Set.of(
        GROUP_TRANSFER.name(),
        PUBLIC.name(),
        PERSONAL.name(),
        CARSHARING.name(),
        TAXI.name()
    );

    private final TripRequestService tripRequestService;

    @Override
    public void accept(Message<RequestMessage> requestMessage) {
        val payload = requestMessage.getPayload();
        log.debug("Received message: {}", payload);

        if (isValidTripRequest(payload)) {
            tripRequestService.saveFromRequestMessage(payload);
        }

        log.info("Trip request processed successfully: id={}", payload.getId());
    }

    private boolean isValidTripRequest(RequestMessage payload) {
        if (Objects.isNull(payload.getTransportType())) {
            return false;
        }

        return VALID_TRANSPORT_TYPES.contains(payload.getTransportType().toUpperCase());
    }

}
