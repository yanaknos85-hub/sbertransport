package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.TripPurposeMessage;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.service.TripPurposeService;

import java.util.function.Consumer;

/**
 * Реализация слушателя целей поездки.
 */
@RequiredArgsConstructor
public class TripPurposeListenerImpl implements Consumer<Message<TripPurposeMessage>> {
    private final TripPurposeService tripPurposeService;
    
    public void accept(Message<TripPurposeMessage> message) {
        handle(message.getPayload());
    }

    private void handle(TripPurposeMessage message) {
        if (message.getId() == null) {
            return;
        }

        TripPurpose tripPurpose = tripPurposeService.get(message.getId()).orElse(new TripPurpose());
        if (message.isDeleted()) {
            tripPurpose.setActive(false);
        } else {
            tripPurpose.setId(message.getId());
            tripPurpose.setPurpose(message.getLabel());
            tripPurpose.setOrganization(message.getOrganization());
        }
        tripPurposeService.save(tripPurpose);
    }

}
