package ru.sber.transport.request_checks.messaging.listeners;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.Message;
import ru.sber.transport.request_checks.messaging.message.ExternalRequestMessage;
import ru.sber.transport.request_checks.service.TripRequestService;

@Slf4j
@RequiredArgsConstructor
public class ExternalTripRequestListener implements Consumer<Message<ExternalRequestMessage>> {

    private final TripRequestService tripRequestService;

    @Override
    public void accept(Message<ExternalRequestMessage> externalRequestMessage) {
        val payload = externalRequestMessage.getPayload();
        log.debug("Received message: {}", payload);

        tripRequestService.saveFromExternalMessage(payload);

        log.info("External trip request processed successfully: id={}", payload.getId());
    }

}
