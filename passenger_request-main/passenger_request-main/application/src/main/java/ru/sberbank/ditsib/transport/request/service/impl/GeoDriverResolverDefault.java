package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;
import ru.sberbank.ditsib.transport.request.service.GeoDriverResolver;

@Slf4j
@Component
public class GeoDriverResolverDefault implements GeoDriverResolver {
    @Override
    public TaxiExternalIntegrationType getIntegrationType() {
        return null;
    }
    
    @Override
    public GeoDriverDTO getGeoDriverByRequest(Request request) {
        log.debug("GeoDriverResolverDefault: getGeoDriverByRequest: request = {}", request);
        
        return GeoDriverDTO.builder()
                           .latitude(0.0d)
                           .longitude(0.0d)
                           .build();
    }
}
