package ru.sberbank.ditsib.geo.controller.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.geo.dto.AddressDto;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RegionDto;
import ru.sberbank.ditsib.geo.dto.RouteDto;
import ru.sberbank.ditsib.geo.dto.RouteRequestDto;
import ru.sberbank.ditsib.geo.dto.WaypointDto;
import ru.sberbank.ditsib.geo.service.GeoControllerService;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoControllerImplTest {

    @Mock
    private GeoControllerService service;

    @InjectMocks
    private GeoControllerImpl geoController;

    private AddressRequestDto addressRequestDto;
    private RouteRequestDto routeRequestDto;
    private WaypointDto waypointDto;

    @BeforeEach
    void setUp() {
        addressRequestDto = AddressRequestDto.builder()
                .location("Москва")
                .latitude(55.75583)
                .longitude(37.6177)
                .build();

        routeRequestDto = new RouteRequestDto();
        var waypoint = new WaypointDto();
        waypoint.setLatitude(55.75583);
        waypoint.setLongitude(37.6177);
        waypoint.setWaitTime(Duration.ZERO);
        routeRequestDto.getCoordinates().add(waypoint);

        waypointDto = new WaypointDto();
        waypointDto.setLatitude(55.75583);
        waypointDto.setLongitude(37.6177);
    }

    @Test
    @DisplayName("getAddress должен делегировать вызов сервису и вернуть адреса")
    void getAddress_shouldReturnAddressesFromService() {
        var expectedAddress = AddressDto.builder()
                .street("Тверская улица")
                .house("1")
                .city("Москва")
                .latitude(55.75583)
                .longitude(37.6177)
                .build();

        when(service.getAddress(any(AddressRequestDto.class))).thenReturn(List.of(expectedAddress));

        Collection<AddressDto> result = geoController.getAddress(addressRequestDto);

        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getStreet()).isEqualTo("Тверская улица");
        verify(service, times(1)).getAddress(addressRequestDto);
    }

    @Test
    @DisplayName("getAddress должен вернуть пустую коллекцию когда сервис возвращает пустую коллекцию")
    void getAddress_shouldReturnEmptyCollection() {
        when(service.getAddress(any(AddressRequestDto.class))).thenReturn(Collections.emptyList());

        Collection<AddressDto> result = geoController.getAddress(addressRequestDto);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getRoute должен вернуть первый маршрут от сервиса")
    void getRoute_shouldReturnFirstRoute() {
        var expectedRoute = RouteDto.builder()
                .distance(100.5)
                .time(Duration.ofSeconds(3600))
                .build();

        when(service.getRoutes(any(RouteRequestDto.class))).thenReturn(List.of(expectedRoute));

        RouteDto result = geoController.getRoute(routeRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDistance()).isEqualTo(100.5);
        assertThat(result.getTime()).isEqualTo(Duration.ofSeconds(3600));
        verify(service, times(1)).getRoutes(routeRequestDto);
    }

    @Test
    @DisplayName("getRoute должен вернуть пустой RouteDto когда сервис выбрасывает NoSuchElementException")
    void getRoute_shouldReturnEmptyWhenNoRoutesFound() {
        when(service.getRoutes(any(RouteRequestDto.class))).thenThrow(new NoSuchElementException("No routes found"));

        RouteDto result = geoController.getRoute(routeRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDistance()).isZero();
        assertThat(result.getTime()).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("getRoute должен вернуть пустой RouteDto когда сервис возвращает пустой список")
    void getRoute_shouldReturnEmptyWhenListIsEmpty() {
        when(service.getRoutes(any(RouteRequestDto.class))).thenReturn(Collections.emptyList());

        RouteDto result = geoController.getRoute(routeRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDistance()).isZero();
        assertThat(result.getTime()).isEqualTo(Duration.ZERO);
    }

    @Test
    @DisplayName("getRoutes должен вернуть список от сервиса")
    void getRoutes_shouldReturnRoutesFromService() {
        var route1 = RouteDto.builder().distance(50.0).time(Duration.ofSeconds(1800)).build();
        var route2 = RouteDto.builder().distance(75.0).time(Duration.ofSeconds(2700)).build();

        when(service.getRoutes(any(RouteRequestDto.class))).thenReturn(List.of(route1, route2));

        List<RouteDto> result = geoController.getRoutes(routeRequestDto);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDistance()).isEqualTo(50.0);
        assertThat(result.get(1).getDistance()).isEqualTo(75.0);
    }

    @Test
    @DisplayName("getRegion должен вернуть хардкоденный RegionDto")
    void getRegion_shouldReturnHardcodedRegion() {
        RegionDto result = geoController.getRegion(waypointDto);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(50);
        assertThat(result.getName()).isEqualTo("Москва");
    }
}
