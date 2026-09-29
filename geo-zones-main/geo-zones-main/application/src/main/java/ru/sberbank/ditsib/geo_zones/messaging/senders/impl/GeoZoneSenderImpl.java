package ru.sberbank.ditsib.geo_zones.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.geo_zones.messaging.senders.GeoZoneSender;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.web.http.mapper.GeoZoneMapper;
import ru.sberbank.ditsib.transport.messaging.messages.GeoZoneMessage;

/**
 * Реализация отправителя.
 */
@RequiredArgsConstructor
@Component
class GeoZoneSenderImpl implements GeoZoneSender {

    @Qualifier("geoZoneSource")
    private final ObjectProvider<OutputBridge> geoZoneSource;

    @Qualifier("geoZoneSourceSsl")
    private final ObjectProvider<OutputBridge> geoZoneSourceSsl;
    
    private final GeoZoneMapper mapper;
    
    @Override
    public void send(GeoZone geoZone, boolean deleted) {
        var message = mapper.toMessage(geoZone, deleted);
        geoZoneSource.ifAvailable(ob -> ob.send(message));
        geoZoneSourceSsl.ifAvailable(ob -> ob.send(message));
    }
}
