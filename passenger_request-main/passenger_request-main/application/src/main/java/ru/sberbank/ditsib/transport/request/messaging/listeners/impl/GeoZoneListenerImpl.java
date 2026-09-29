package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.request.messaging.message.GeoZoneMessage;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.request.service.GeoZoneService;

import java.util.function.Consumer;

/**
 * Реализация слушателя геозон.
 */
@RequiredArgsConstructor
public class GeoZoneListenerImpl implements Consumer<Message<GeoZoneMessage>> {
    
    private final GeoZoneService geoZoneService;
    
    public void accept(Message<GeoZoneMessage> message) {
        handle(message.getPayload());
    }
    
    
    private void handle(GeoZoneMessage message) {
        var geoZone = geoZoneService.get(message.getId()).orElseGet(GeoZone::new);
        
        if (!message.isDeleted()) {
            geoZone.setId(message.getId());
            geoZone.setCode(message.getCode());
            geoZone.setName(message.getName());
            geoZone.setParentId(message.getParentId());
            
            geoZoneService.save(geoZone);
        }
    }
}
