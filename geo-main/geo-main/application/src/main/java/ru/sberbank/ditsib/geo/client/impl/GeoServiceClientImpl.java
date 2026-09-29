package ru.sberbank.ditsib.geo.client.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.config.properties.ApiProperties;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.config.properties.MappingFields;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.config.properties.routing.TypeProperties;
import ru.sberbank.ditsib.geo.config.properties.routing.UnitProperties;
import ru.sberbank.ditsib.geo.dto.*;
import ru.sberbank.ditsib.geo.exceptions.GeoApiException;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.*;
import java.util.stream.Stream;

import static ru.sberbank.ditsib.geo.controller.Constants.EMPLOYEE_TRANSPORTATION;

/**
 * Implementation of geo service client.
 */
@Slf4j
@Component
class GeoServiceClientImpl extends BaseRestClient implements GeoServiceClient {

    private final GeoProperties geoProperties;

    private final MapUtils mapUtils;

    private final List<String> OFF_ROAD_FILTERS =
            Stream.of(FilterEnum.DIRT_ROAD)
                    .map(FilterEnum::name).map(String::toLowerCase).toList();

    public GeoServiceClientImpl(GeoProperties geoProperties, ObjectMapper objectMapper, MapUtils mapUtils) {
        super(objectMapper, mapUtils);
        this.geoProperties = geoProperties;
        this.mapUtils = mapUtils;
    }

    @Override
    public List<Map<String, Object>> getAddressesByLocation(AddressRequestDto requestDto) {
        ApiProperties properties;
        var requestType = requestDto.getRequestType();
        if (RequestType.PARKING.equals(requestType)) {
            properties = geoProperties.getParkingByNumberProperties();
        } else {
            properties = geoProperties.getCoderProperties();
        }
        return getAddresses(properties, requestDto);
    }

    @Override
    public List<Map<String, Object>> getAddressesByCoordinates(AddressRequestDto requestDto) {
        ApiProperties properties;
        var requestType = requestDto.getRequestType();
        if (RequestType.PARKING.equals(requestType)) {
            properties = geoProperties.getParkingProperties();
        } else {
            properties = geoProperties.getReverseCoderProperties();
        }
        return getAddresses(properties, requestDto);
    }

    @Override
    public List<Map<String, Object>> getRoutes(
            List<WaypointDto> coordinates,
            DistanceUnit distanceUnit, RouteType routeType, String transportService, Boolean excludeDirtRoad
    ) {
        var unitProperties = geoProperties.getRouting().getUnit();
        var typeProperties = geoProperties.getRouting().getType();
        var unitString =
                ReflectionUtils.getMappedPropertyString(unitProperties, distanceUnit, UnitProperties::getDefault);
        var typeString = ReflectionUtils.getMappedPropertyString(typeProperties, routeType, TypeProperties::getDefault);
        return getRoutes(unitString, typeString, coordinates, transportService, excludeDirtRoad);
    }

    @Override
    public Map<String, Object> getRoute(List<RouteRecreationCoordinates> coords) {
        var properties = geoProperties.getRouteRecreationProperties();
        var mapping = properties.getFormat().getRequest().getFields();
        var processedCoordinates = processCoordinates(coords, mapping);
        var body = new HashMap<String, Object>();
        body.put(mapping.getLocation(), processedCoordinates);
        body.put(mapping.getBadPointTolerance(), geoProperties.getRouteRecreation().getDefaultBadPointTolerance());
        return request(properties, body);
    }

    @Override
    public Map<String, Object> getRegion(String id) {
        var properties = geoProperties.getRegionProperties();
        var requestMapping = properties.getFormat().getRequest().getFields();
        var data = new HashMap<String, Object>();
        data.put(requestMapping.getId(), id);
        var result = new HashMap<String, Object>();
        var response = ReflectionUtils.castObjectToMap(request(properties, data, Map.class));
        for (var entry : response.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return result;
    }

    /**
     * Get addresses.
     *
     * @return response.
     */
    @SuppressWarnings("java:S107")
    private List<Map<String, Object>> getAddresses(
            @NonNull ApiProperties properties, AddressRequestDto requestDto
    ) {
        var requestMapping = properties.getFormat().getRequest().getFields();

        var parameters = new HashMap<String, Object>();
        putIfNotNull(parameters, requestMapping.getLatitude(), requestDto.getLatitude());
        putIfNotNull(parameters, requestMapping.getLongitude(), requestDto.getLongitude());
        putIfNotNull(parameters, requestMapping.getQuery(), requestDto.getLocation());
        putIfNotNull(parameters, requestMapping.getType(), properties.getRequestType().getType());
        putIfNotNull(parameters, requestMapping.getSort(), properties.getSort().getDistance());
        putIfNotNull(parameters, requestMapping.getRadius(), requestDto.getRadius());
        putIfNotNull(parameters, requestMapping.getCenter(), resolveCenter(requestDto));

        var result = new ArrayList<Map<String, Object>>();
        var response = request(properties, parameters, List.class);
        if (response == null) {
            return result;
        }
        for (var item : response) {
            result.add(ReflectionUtils.castObjectToMap(item, String.class, Object.class));
        }
        return result;
    }

    private String resolveCenter(AddressRequestDto addressRequestDto) {
        if (addressRequestDto == null) {
            return null;
        }
        if (addressRequestDto.getCenterLongitude() != null && addressRequestDto.getCenterLatitude() != null) {
            return "%s, %s".formatted(addressRequestDto.getCenterLongitude(),
                    addressRequestDto.getCenterLatitude());
        }
        return null;
    }

    /**
     * Put a value if the key and the value are not null.
     *
     * @param parameters target map.
     * @param key        key.
     * @param value      value.
     */
    private void putIfNotNull(@lombok.NonNull Map<String, Object> parameters, String key,
                              Object value) {
        if (key != null && value != null) {
            parameters.put(key, value);
        }
    }

    /**
     * Get route by coordinates.
     *
     * @param unit        distance units.
     * @param coordinates source coordinates.
     * @return route.
     */
    private List<Map<String, Object>> getRoutes(
            String unit, String type, List<WaypointDto> coordinates, String transportService, Boolean excludeDirtRoad
    ) {
        var routing = geoProperties.getRouteProperties();
        var mapping = routing.getFormat().getRequest().getFields();

        var parameters = new HashMap<String, Object>();

        var processedCoordinates = processCoordinates(coordinates, mapping);

        parameters.put(geoProperties.getRouting().getUnit().getPath(), unit);
        parameters.put(mapping.getLocation(), processedCoordinates);
        parameters.put(geoProperties.getRouting().getType().getPath(), type);

        // Для TransportServiceType.EMPLOYEE_TRANSPORTATION указываем параметры для поиска наикратчайшего маршрута для синхронизации с srm
        if (EMPLOYEE_TRANSPORTATION.equals(transportService)) {
            parameters.put("type", TypeEnum.STATISTIC.name().toLowerCase());
            if (Boolean.TRUE.equals(excludeDirtRoad)){
                parameters.put("filters", OFF_ROAD_FILTERS);
            }
        }

        var properties = geoProperties.getRouteProperties();

        var response = Optional.ofNullable(request(properties, parameters, List.class)).orElse(Collections.emptyList());

        var result = new ArrayList<Map<String, Object>>();
        for (var item : response) {
            result.add(ReflectionUtils.castObjectToMap(item, String.class, Object.class));
        }
        return result;
    }

    /**
     * Perform a request.
     *
     * @param properties    properties to perform request.
     * @param parameters    parameters of request.
     * @param responseClass class of response.
     * @param <V>           type of response.
     * @return response.
     */
    private <V> V request(
            ApiProperties properties, Map<String, Object> parameters, Class<V> responseClass
    ) {
        var responseMapping = properties.getFormat().getResponse();
        var errorMessagesPath = properties.getErrorMessagesPath();
        var result = request(properties, parameters);
        checkMessages(errorMessagesPath, result);
        return mapUtils.extractNode(result, responseMapping.getRoot(), responseClass);
    }

    /**
     * Check messages in the response.
     *
     * @param errorMessagesPath path to error messages.
     * @param response          response.
     */
    private void checkMessages(
            String errorMessagesPath, Map<String, Object> response
    ) {
        var messages = mapUtils.extractNode(response, errorMessagesPath, List.class);
        if (response.containsKey("meta")) {
            var meta = ReflectionUtils.castObjectToMap(response.get("meta"), String.class, Object.class);
            if (meta.containsKey("code")) {
                var code = ReflectionUtils.cast(meta.get("code"), Integer.class);
                if (Objects.equals(404, code)) {
                    throw new GeoApiException(404, messages);
                }
            }
        }
        if (messages != null && !messages.isEmpty()) {
            throw new GeoApiException(messages);
        }
    }

    private List<Map<String, Object>> processCoordinates(List<?> coordinates, MappingFields mapping){
        var processedCoordinates = new ArrayList<Map<String, Object>>();

        for (var coordinate : new ObjectMapper().convertValue(coordinates, new TypeReference<List<Map<String, Object>>>() {
        })) {
            var processedCoordinate = new HashMap<String, Object>();
            for (var entry : coordinate.entrySet()) {
                var key = entry.getKey();
                if (mapping.containsKey(key)) {
                    processedCoordinate.put(mapping.get(key), entry.getValue());
                }
            }
            processedCoordinates.add(processedCoordinate);
        }

        return processedCoordinates;
    }
}
