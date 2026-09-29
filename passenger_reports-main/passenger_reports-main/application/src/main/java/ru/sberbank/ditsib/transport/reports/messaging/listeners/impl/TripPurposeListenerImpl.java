package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.TripPurposeListener;
import ru.sberbank.ditsib.transport.reports.model.TripPurpose;
import ru.sberbank.ditsib.transport.reports.service.TripPurposeService;

@RequiredArgsConstructor
@Component("tripPurposeInput")
public class TripPurposeListenerImpl implements TripPurposeListener   {
    private final TripPurposeService tripPurposeService;
    
    @Transactional
    @Override
    public void handle(TripPurposeMessage message) {
        var purposeId = message.getId();
        if (message.isDeleted()) {
            tripPurposeService.setInactiveByPurposeId(purposeId);
        } else {
            var tripPurpose = tripPurposeService.findById(purposeId).orElse(
                    TripPurpose.builder().id(purposeId).purpose(message.getLabel()).build()
            );
            tripPurpose.setOrganization(message.getOrganization());
            tripPurposeService.save(tripPurpose);
        }
    }
}
