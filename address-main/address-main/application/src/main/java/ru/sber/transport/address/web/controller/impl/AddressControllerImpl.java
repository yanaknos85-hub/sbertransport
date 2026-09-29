package ru.sber.transport.address.web.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.use_cases.Search;
import ru.sber.transport.address.providers.RequestContext;
import ru.sber.transport.address.providers.Viewport;
import ru.sber.transport.address.web.mapper.AddressMapperToWeb;
import ru.sber.transport.web.api.AddressesApiDelegate;
import ru.sber.transport.web.model.Address;

import java.math.BigDecimal;
import java.util.*;

/**
 * Реализация контроллера адресов.
 */
@Slf4j
@RequiredArgsConstructor
@Component
class AddressControllerImpl implements AddressesApiDelegate {

    private final Search search;

    private final AddressMapperToWeb<FrequentlyAddress> frequentlyAddressMapper;

    private final AddressMapperToWeb<FavoriteAddress> favoriteAddressMapper;

    private final AddressMapperToWeb<GeoAddress> geoAddressMapping;

    private final RequestContext requestContext;

    @Override
    public ResponseEntity<Map<String, List<Address>>> getMap(String location, BigDecimal latitude, BigDecimal longitude, BigDecimal viewportTopLeftLatitude, BigDecimal viewportTopLeftLongitude, BigDecimal viewportBottomRightLatitude, BigDecimal viewportBottomRightLongitude) {
        log.debug("""
                Request for searching address with data:
                string={}
                latitude={}
                longitude={}""",
            location, latitude, longitude);

        if (location == null && latitude == null && longitude == null) {
            throw new HttpStatusCodeException(HttpStatus.BAD_REQUEST,
                "Один из параметров, search, latitude или longitude должен быть указан") {
            };
        }

        requestContext.setViewport(Viewport.builder()
            .topLeftLongitude(viewportTopLeftLongitude)
            .topLeftLatitude(viewportTopLeftLatitude)
            .bottomRightLatitude(viewportBottomRightLatitude)
            .bottomRightLongitude(viewportBottomRightLongitude).build());

        var searchAddresses = latitude != null && longitude != null
            ? Collections.singletonList(this.search.search(latitude, longitude))
            : this.search.search(getSearchString(location));

        var result = new HashMap<String, List<Address>>();
        var frequentlyAddresses = new ArrayList<Address>();
        var favoriteAddresses = new ArrayList<Address>();
        var commonAddresses = new ArrayList<Address>();
        for (var address : searchAddresses) {
            if (address instanceof FrequentlyAddress frequently) {
                frequentlyAddresses.add(frequentlyAddressMapper.toWeb(frequently));
            } else if (address instanceof FavoriteAddress favorite) {
                favoriteAddresses.add(favoriteAddressMapper.toWeb(favorite));
            } else if (address instanceof GeoAddress geoAddress) {
                commonAddresses.add(geoAddressMapping.toWeb(geoAddress));
            }
        }
        result.put("FREQUENTLY", frequentlyAddresses);
        result.put("FAVORITE", favoriteAddresses);
        result.put("COMMON", commonAddresses);
        return ResponseEntity.ok(result);
    }

    private String getSearchString(String search) {
        return search == null ? "" : search;
    }
}
