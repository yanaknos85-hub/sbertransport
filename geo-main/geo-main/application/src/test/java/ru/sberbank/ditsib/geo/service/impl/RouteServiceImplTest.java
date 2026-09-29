package ru.sberbank.ditsib.geo.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.model.RouteRecreationCoordinates;
import ru.sberbank.ditsib.geo.service.RouteService;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка cервиса маршрутизации")
public class RouteServiceImplTest {
    private final GeoServiceClient geoServiceClient = mock(GeoServiceClient.class);
    private final RouteService routeService = new RouteServiceImpl(geoServiceClient, null, null, null);

    @Test
    void test_routeRequest() {
        var distance = Instancio.create(Double.class);

        var twoGisResponse = new HashMap<String, Object>();
        twoGisResponse.put("distance", distance);
        twoGisResponse.put("route", "LINESTRING(1 2,3 4,5 6,7 8)");
        when(geoServiceClient.getRoute(any())).thenReturn(twoGisResponse);

        var coords = Instancio.createList(RouteRecreationCoordinates.class);
        var result = routeService.routeRequest(coords);
        assertEquals(distance, result.getDistance());

        var segments = result.getSegments();
        assertEquals(1, segments.size());

        var segment = segments.get(0);
        assertEquals(distance, segment.getDistance());

        var points = segment.getCoordinates();
        assertEquals(4, points.size());

        for (var i = 0; i < 4; i++) {
            var point = points.get(i);
            assertEquals((i + 1) * 2 - 1, point.getLongitude());
            assertEquals((i + 1) * 2, point.getLatitude());
        }
    }
}
