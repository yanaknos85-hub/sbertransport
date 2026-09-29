package ru.sber.transport.address.providers.common_address;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.providers.RequestContext;
import ru.sber.transport.address.providers.common_address.client.GeoClient;

import java.math.BigDecimal;
import java.util.*;

/**
 * Реализация провайдера для запроса общего списка адреса.
 */
@RequiredArgsConstructor
@Component
@Slf4j
class GeoAddressProviderImpl implements AddressProvider<GeoAddress> {
    
    private final GeoClient geoClient;
    
    private final RequestContext requestContext;

    @SuppressWarnings("java:S3958")
    @Override
    public Set<GeoAddress> getAddresses(@NonNull String search) {
        var viewport = requestContext.getViewport();
        var result = new HashSet<GeoAddress>();
        try {
            var received = geoClient.getAddresses(search, viewport.getTopLeftLatitude(), viewport.getTopLeftLongitude(),
                                                  viewport.getBottomRightLatitude(), viewport.getBottomRightLongitude());
            result.addAll(received);
        } catch (Exception e) {
            log.error("Geo request failed", e);
        }
        return result;
    }
    
    @Override
    public Optional<GeoAddress> getAddress(@NonNull BigDecimal latitude, @NonNull BigDecimal longitude) {
        try {
            var received = geoClient.getAddress(latitude, longitude);
            if (!received.isEmpty()) {
                return Optional.of(received.get(0));
            }
        } catch (Exception e) {
            log.error("Geo request failed", e);
        }
        return Optional.empty();
    }
}
