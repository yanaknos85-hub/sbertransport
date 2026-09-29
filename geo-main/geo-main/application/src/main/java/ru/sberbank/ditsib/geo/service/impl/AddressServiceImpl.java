package ru.sberbank.ditsib.geo.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.config.properties.MappingFields;
import ru.sberbank.ditsib.geo.constants.ParkingConstants;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.AddressKey;
import ru.sberbank.ditsib.geo.service.AddressService;
import ru.sberbank.ditsib.geo.service.CacheService;
import ru.sberbank.ditsib.geo.service.DataExtractor;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса адресов.
 */
@RequiredArgsConstructor
@Service
class AddressServiceImpl implements AddressService {

    private final GeoProperties geoProperties;

    private final GeoServiceClient geoServiceClient;

    private final DataExtractor dataExtractor;

    private final CacheService cacheService;

    @Override
    public List<Address> getAddressByCoordinates(AddressRequestDto addressRequest) {
        return addressesRequestByCoordinates(addressRequest);
    }

    @Override
    public List<Address> getAddressByLocation(AddressRequestDto addressRequest) {
        return cacheService.getAddressByLocation(addressRequest, () -> addressesRequestByLocation(addressRequest));
    }

    /**
     * Запрос к поставщику данных адресов.
     *
     * @param addressRequest params of address request.
     * @return коллекция адресов.
     */
    private List<Address> addressesRequestByLocation(AddressRequestDto addressRequest) {
        ApiProperties properties;
        if (Objects.equals(addressRequest.getRequestType(), RequestType.PARKING)) {
            properties = geoProperties.getParkingByNumberProperties();
        } else {
            properties = geoProperties.getCoderProperties();
        }
        var response = geoServiceClient.getAddressesByLocation(addressRequest);
        return response.stream().map(item -> convertToEntity(item, properties))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * Запрос к поставщику данных адресов.
     *
     * @param addressRequest params of address request.
     * @return адрес.
     */
    private List<Address> addressesRequestByCoordinates(AddressRequestDto addressRequest) {
        var response = geoServiceClient.getAddressesByCoordinates(addressRequest);

        if (Objects.equals(addressRequest.getRequestType(), RequestType.PARKING)) {
            return response.stream().map(item -> convertToEntity(item, geoProperties.getParkingProperties()))
                    .flatMap(Collection::stream).collect(Collectors.toList());
        } else {
            return response.stream().map(item -> convertToEntity(item, geoProperties.getReverseCoderProperties()))
                    .flatMap(Collection::stream).collect(Collectors.toList());
        }
    }


    /**
     * Convert map to address entity.
     *
     * @param map        source map.
     * @param properties properties for decoding.
     * @return address.
     */
    private List<Address> convertToEntity(Map<String, Object> map, ApiProperties properties) {
        var fields = properties.getFormat().getResponse().getFields();
        var addresses = new ArrayList<Address>();
        if (Objects.equals(properties.getRequestType().getType(), "branch")) {
            return branchToEntity(map, fields, addresses);
        } else {
            return getAddresses(map, fields, addresses);
        }
    }

    private List<Address> getAddresses(Map<String, Object> map, MappingFields fields, ArrayList<Address> addresses) {
        var latitude = dataExtractor.getValue(map, fields.getLatitude(), Double.class);
        var longitude = dataExtractor.getValue(map, fields.getLongitude(), Double.class);
        if (latitude == null || longitude == null) {
            return Collections.emptyList();
        }

        var city = dataExtractor.getValue(map, fields.getCity(), String.class);
        var settlement = dataExtractor.getValue(map, fields.getSettlement(), String.class);
        var livingArea = dataExtractor.getValue(map, fields.getLivingArea(), String.class);
        var place = dataExtractor.getValue(map, fields.getPlace(), String.class);
        var district = dataExtractor.getValue(map, fields.getDistrict(), String.class);
        var country = dataExtractor.getValue(map, fields.getCountry(), String.class);
        var region = dataExtractor.getValue(map, fields.getRegion(), String.class);
        var house = dataExtractor.getValue(map, fields.getHouse(), Object.class);
        var street = dataExtractor.getValue(map, fields.getStreet(), Object.class);
        if ("street".equals(map.get("type")) && street == null) {
            street = map.get("name");
        }
        var name = dataExtractor.getValue(map, fields.getName(), String.class);
        var subtype = dataExtractor.getValue(map, fields.getSubtype(), String.class);
        var type = dataExtractor.getValue(map, fields.getType(), String.class);

        if (city == null && name != null && subtype != null && (type.equals("adm_div") || type.equals("station"))) {
            city = name;
        }

        if (city == null && settlement != null) {
            city = settlement;
            settlement = null;
        }

        var streets = new ArrayList<String>();
        var houses = new ArrayList<String>();
        if (street != null && Collection.class.isAssignableFrom(street.getClass())) {
            streets.addAll(ReflectionUtils.castObjectToList(street, String.class));
            houses.addAll(ReflectionUtils.castObjectToList(house, String.class));
        } else {
            append(streets, street);
            append(houses, house);
        }

        var key = AddressKey.builder().latitude(latitude).longitude(longitude)
                .provider(geoProperties.getUrl()).build();

        for (String streetFromCycle : streets) {
            var address = Address.builder()
                    .city(city)
                    .settlement(settlement)
                    .livingArea(livingArea)
                    .place(place)
                    .country(country)
                    .region(region)
                    .district(district)
                    .id(key)
                    .latitude(latitude)
                    .street(streetFromCycle == null ? null : streetFromCycle.trim())
                    .house(houses.stream().filter(Objects::nonNull).findFirst().map(String::trim).orElse(null))
                    .longitude(longitude)
                    .name(name)
                    .build();
            addresses.add(address);
            if (!houses.isEmpty()) {
                houses.remove(0);
            }
        }

        if (addresses.isEmpty()) {
            addresses.add(Address.builder()
                    .city(city)
                    .settlement(settlement)
                    .livingArea(livingArea)
                    .place(place)
                    .country(country)
                    .district(district)
                    .region(region)
                    .id(key)
                    .latitude(latitude)
                    .longitude(longitude)
                    .name(name)
                    .build());
        }
        return addresses;
    }

    private List<Address> branchToEntity(Map<String, Object> map, MappingFields fields, ArrayList<Address> addresses) {
        var objectId = dataExtractor.getValue(map, fields.getObjectId(), Object.class);
        var attributeGroups = dataExtractor.getValue(map, fields.getAttributeGroups(), Object.class);
        var nameEx = dataExtractor.getValue(map, fields.getNameEx(), Object.class);
        var isPaid = dataExtractor.getValue(map, fields.getIsPaid(), Boolean.class);
        var geometry = dataExtractor.getValue(map, fields.getGeometry(), Object.class);
        var point = dataExtractor.getValue(map, fields.getPoint(), Object.class);
        var fullName = dataExtractor.getValue(map, fields.getFullName(), String.class);
        var name = dataExtractor.getValue(map, fields.getName(), String.class);
        var type = dataExtractor.getValue(map, fields.getType(), String.class);
        var purposeName = dataExtractor.getValue(map, fields.getPurposeName(), String.class);

        if (name == null || !ParkingConstants.matchingCheck(name)) {
            return Collections.emptyList();
        }
        addresses.add(Address.builder().objectId(objectId).attributeGroups(attributeGroups).nameEx(nameEx)
                .isPaid(isPaid).geometry(geometry).point(point).fullName(fullName).name(name).type(type)
                .purposeName(purposeName).build());
        return addresses;
    }

    /**
     * Добавить объект в коллекцию, если не <code>null</code>.
     *
     * @param collection коллекция.
     * @param object     объект для добавления.
     * @param <C>        тип коллекции.
     */
    private <C extends Collection<? super String>> void append(C collection, Object object) {
        Optional.ofNullable(object).map(String::valueOf).ifPresent(collection::add);
    }
}
