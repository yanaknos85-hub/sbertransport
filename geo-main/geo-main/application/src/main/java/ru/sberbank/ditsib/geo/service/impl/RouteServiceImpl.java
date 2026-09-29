package ru.sberbank.ditsib.geo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.config.properties.MappingFields;
import ru.sberbank.ditsib.geo.config.properties.routing.DistanceUnit;
import ru.sberbank.ditsib.geo.config.properties.routing.RouteType;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.model.*;
import ru.sberbank.ditsib.geo.service.AddressService;
import ru.sberbank.ditsib.geo.service.DataExtractor;
import ru.sberbank.ditsib.geo.service.RouteService;
import ru.sberbank.ditsib.geo.utils.geometry.Line;
import ru.sberbank.ditsib.geo.utils.geometry.PolyLine;
import ru.sberbank.ditsib.geo.utils.geometry.WktParser;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Pattern;

import static ru.sberbank.ditsib.geo.controller.Constants.EMPLOYEE_TRANSPORTATION;

/**
 * Реализация сервиса маршрутов.
 */
@SuppressWarnings("java:S3958")
@RequiredArgsConstructor
@Component
@Slf4j
class RouteServiceImpl implements RouteService {

    private static final Pattern PATTERN = Pattern.compile("^LINESTRING\\([\\w\\d ,.]*\\)$");

    private final GeoServiceClient geoServiceClient;

    private final GeoProperties geoProperties;

    private final DataExtractor dataExtractor;

    private final AddressService addressService;

    @Override
    public List<Route> routeRequest(
            List<WaypointDto> coordinates, DistanceUnit distanceUnit, RouteType routeType, String transportService
                                   ) {
        var lastIndex = coordinates.size() - 1;
        var routeSegments = new ArrayList<List<Route>>();
        var maxSegments = 0;
        for (var index = 1; index <= lastIndex; index++) {
            var segment = new LinkedList<WaypointDto>();

            var startWaypoint = coordinates.get(index - 1);
            segment.add(startWaypoint);
            segment.add(coordinates.get(index));

            var response = getRoutes(distanceUnit, routeType, transportService, segment);
            var respondedRouteSegment = new LinkedList<Route>();
            if (maxSegments < response.size()) {
                maxSegments = response.size();
            }
            for (var routeMap : response) {
                var route = convertToRoute(routeMap);
                if (route == null) {
                    continue;
                }
                respondedRouteSegment.add(route);
            }
            routeSegments.add(respondedRouteSegment);
        }
        for (var routeSegment : routeSegments) {
            for (var i = routeSegment.size(); i < maxSegments; i++) {
                routeSegment.add(routeSegment.get(0));
            }
        }
        var routes = new ArrayList<Route>();
        for (var routeSegmentIndex = 0; routeSegmentIndex < routeSegments.size(); routeSegmentIndex++) {
            var curRoutes = routeSegments.get(routeSegmentIndex);
            for (var curRouteIndex = 0; curRouteIndex < curRoutes.size(); curRouteIndex++) {
                fillRoute(routes, routeSegmentIndex, curRoutes, curRouteIndex);
            }
        }

        copyWaypoints(routes, coordinates);
        return routes;
    }

    @Override
    public Route routeRequest(List<RouteRecreationCoordinates> coordinates) {
        var twoGisResponse = geoServiceClient.getRoute(coordinates);
        var distance = ReflectionUtils.cast(twoGisResponse.get("distance"), Double.class);
        var twoGisRoute = (String) twoGisResponse.get("route");

        var matcher = PATTERN.matcher(twoGisRoute);
        if (!matcher.matches()) {
            log.debug(twoGisRoute);
            throw new RuntimeException("Wrong 2gis geometry answer!");
        }

        var parsedPoints = twoGisRoute
                .replace("LINESTRING(", "")
                .replace(",", " ")
                .replace(")", "")
                .split(" ");

        var points = new LinkedList<Coordinates>();
        for (var i = 0; i < parsedPoints.length; i += 2) {
            points.add(new Coordinates(Double.parseDouble(parsedPoints[i + 1]), Double.parseDouble(parsedPoints[i])));
        }

        var segment = Segment.builder()
                .distance(distance)
                .time(Duration.ZERO)
                .coordinates(points)
                .build();
        return Route.builder()
                .distance(distance)
                .time(Duration.ZERO)
                .segments(List.of(segment))
                .build();
    }

    private List<Map<String, Object>> getRoutes(DistanceUnit distanceUnit, RouteType routeType, String transportService, LinkedList<WaypointDto> segment) {
        List<Map<String, Object>> response;
        if (EMPLOYEE_TRANSPORTATION.equals(transportService)){
            try {
                response = geoServiceClient.getRoutes(segment, distanceUnit, routeType, transportService, Boolean.TRUE);
            } catch (Exception e){
                log.info("An error occurred when building a route with exclusion of dirty roads", e);
                response = geoServiceClient.getRoutes(segment, distanceUnit, routeType, transportService, Boolean.FALSE);
            }
        } else {
            response = geoServiceClient.getRoutes(segment, distanceUnit, routeType, transportService, null);
        }
        return response;
    }

    private void fillRoute(List<Route> targetRoutes, int segmentIndex, List<Route> sourceRoutes, int curRouteIndex) {
        var curRoute = sourceRoutes.get(curRouteIndex);
        if (targetRoutes.size() <= curRouteIndex) {
            var route = Route.builder().build();
            targetRoutes.add(route);
        }
        var route = targetRoutes.get(curRouteIndex);
        route.addTime(curRoute.getTime());
        route.addDistance(curRoute.getDistance());
        var curSegments = curRoute.getSegments();
        for (var curSegment : curSegments) {
            route.getSegments().add(curSegment);
        }
        if (segmentIndex == 0) {
            route.getWaypoints().add(curRoute.getWaypoints().get(0));
        }
        route.getWaypoints().add(curRoute.getWaypoints().get(1));
    }

    private void copyWaypoints(List<Route> target, List<WaypointDto> source) {
        var sourceWaypoints = source.stream().filter(Objects::nonNull).toList();
        for (var i = 0; i < sourceWaypoints.size(); i++) {
            for (var route : target) {
                copyWaypoint(route.getWaypoints().get(i), sourceWaypoints.get(i));
            }
        }
    }

    private void copyWaypoint(Waypoint waypoint, WaypointDto waypointDto) {
        waypoint.setWaitTime(waypointDto.getWaitTime());
        waypoint.setCity(waypointDto.getCity());
        waypoint.setCountry(waypointDto.getCountry());
        waypoint.setHouse(waypointDto.getHouse());
        waypoint.setRegion(waypointDto.getRegion());
        waypoint.setDistrict(waypointDto.getDistrict());
        waypoint.setStreet(waypointDto.getStreet());
    }

    /**
     * Конвертация данных маршрут.
     *
     * @param map исходные данные.
     *
     * @return маршрут.
     */
    private Route convertToRoute(Map<String, Object> map) {
        var mapping = geoProperties.getRouteProperties().getFormat().getResponse().getFields();
        if (map.isEmpty()) {
            return null;
        }
        var route = Route.builder();
        var routeDistance = dataExtractor.getValueOrDefault(map, mapping.getDistance(), 0D);
        var routeTime = dataExtractor.getValueOrDefault(map, mapping.getTime(), 0D).longValue();

        var routeDistanceDefined = routeDistance > 0;
        var routeTimeDefined = routeTime > 0;

        var points = new ArrayList<Segment>();
        var responsePoints = dataExtractor.getValue(map, mapping.getSegments(), List.class);
        for (var responsePoint : responsePoints) {
            var rp = ReflectionUtils.castObjectToMap(responsePoint, String.class, Object.class);
            var responseManeuvers = new ArrayList<>(extractManeuvers(rp));
            var distance = dataExtractor.getValueOrDefault(rp, mapping.getDistance(), 0D);
            var time = dataExtractor.getValueOrDefault(rp, mapping.getTime(), 0D);
            if (!routeTimeDefined) {
                routeTime += time;
            }
            if (!routeDistanceDefined) {
                routeDistance += distance;
            }

            points.add(Segment.builder()
                              .distance(distance)
                              .time(Duration.ofSeconds(time.longValue()))
                              .coordinates(responseManeuvers).build());
        }

        var wayPoints = dataExtractor.getValue(map, mapping.getWaypoints(), List.class);
        var waypoints = new ArrayList<Waypoint>();
        if (wayPoints == null) {
            for (var point : points) {
                var coordinates = point.getCoordinates();
                if (coordinates.isEmpty()) {
                    continue;
                }
                var firstCoordinate = coordinates.get(0);
                var lastCoordinate = coordinates.get(coordinates.size() - 1);
                waypoints.addAll(addressService.getAddressByCoordinates(
                                AddressRequestDto.builder()
                                        .latitude(firstCoordinate.getLatitude())
                                        .longitude(firstCoordinate.getLongitude())
                                        .build())
                        .stream().map(Waypoint::new).toList());
                waypoints.addAll(addressService.getAddressByCoordinates(
                                AddressRequestDto.builder()
                                        .latitude(lastCoordinate.getLatitude())
                                        .longitude(lastCoordinate.getLongitude())
                                        .build())
                        .stream().map(Waypoint::new).toList());
            }
        } else {
            for (var waypoint : wayPoints) {
                var wp = ReflectionUtils.<Map<String, Object>>cast(waypoint);
                Duration waitTime = Duration.ZERO;
                var responseAddress = Address.builder()
                                             .city(dataExtractor.getValue(wp, mapping.getCity(), String.class))
                                             .country(dataExtractor.getValue(wp, mapping.getCountry(), String.class))
                                             .house(dataExtractor.getValue(wp, mapping.getHouse(), String.class))
                                             .region(dataExtractor.getValue(wp, mapping.getRegion(), String.class))
                                             .street(dataExtractor.getValue(wp, mapping.getStreet(), String.class))
                                             .id(AddressKey.builder()
                                                           .latitude(dataExtractor.getValueOrDefault(wp,
                                                                                                     mapping.getLatitude(),
                                                                                                     0D))
                                                           .longitude(
                                                                   dataExtractor.getValueOrDefault(wp,
                                                                                                   mapping.getLongitude(),
                                                                                                   0D))
                                                           .build())
                                             .build();
                waypoints.add(new Waypoint(responseAddress, waitTime));
            }
        }

        route.distance(routeDistance)
             .time(Duration.ofSeconds(routeTime))
             .segments(points)
             .waypoints(waypoints);
        return route.build();
    }

    private List<Coordinates> extractManeuvers(Map<String, Object> segment) {
        var mapping = geoProperties.getRouteProperties().getFormat().getResponse().getFields();
        var coordinates = dataExtractor.getValue(segment, mapping.getCoordinates(), Object.class);
        if (coordinates == null) {
            return new ArrayList<>();
        }
        var responseManeuvers = new ArrayList<Coordinates>();
        for (var maneuver : (List<?>) coordinates) {
            if (Map.class.isAssignableFrom(maneuver.getClass())) {
                var man = ReflectionUtils.castObjectToMap(maneuver, String.class, Object.class);
                responseManeuvers.add(Coordinates.builder()
                                                 .latitude(dataExtractor.getValue(man, mapping.getLatitude(),
                                                                                                                      Double.class))
                                                 .longitude(dataExtractor.getValue(man, mapping.getLongitude(),
                                                                                   Double.class))
                                                 .build());
            } else if (List.class.isAssignableFrom(maneuver.getClass())) {
                var man = ReflectionUtils.castObjectToList(maneuver, List.class);
                for (var rawItem : man) {
                    responseManeuvers.add(extractCoordinates(mapping, rawItem));
                }
            } else {
                var polyLine = WktParser.<PolyLine>parseWkt(String.valueOf(maneuver));
                for (var line : polyLine.<List<Line>>get("lines")) {
                    var point = Coordinates.builder().latitude(line.getY())
                                           .longitude(line.getX()).build();
                    responseManeuvers.add(point);
                }
            }
        }
        return responseManeuvers;
    }

    private Coordinates extractCoordinates(MappingFields mapping, List<?> rawItem) {
        var latitudeMapping = mapping.getLatitude();
        var longitudeMapping = mapping.getLongitude();
        var latitudeIndex = 0;
        var longitudeIndex = 0;
        var coordinatePattern = Pattern.compile("\\[\\d?+]");
        var latitudeMatcher = coordinatePattern.matcher(latitudeMapping);
        if (latitudeMatcher.find()) {
            var latitudeString = latitudeMatcher.group();
            latitudeString = latitudeString.replace("[", "");
            latitudeString = latitudeString.replace("]", "");
            latitudeIndex = Integer.parseInt(latitudeString);
        }
        var longitudeMatcher = coordinatePattern.matcher(longitudeMapping);
        if (longitudeMatcher.find()) {
            var longitudeString = longitudeMatcher.group();
            longitudeString = longitudeString.replace("[", "");
            longitudeString = longitudeString.replace("]", "");
            longitudeIndex = Integer.parseInt(longitudeString);
        }
        var latitude = ReflectionUtils.<Double>cast(rawItem.get(latitudeIndex));
        var longitude = ReflectionUtils.<Double>cast(rawItem.get(longitudeIndex));
        return Coordinates.builder().latitude(latitude).longitude(longitude).build();
    }

}
