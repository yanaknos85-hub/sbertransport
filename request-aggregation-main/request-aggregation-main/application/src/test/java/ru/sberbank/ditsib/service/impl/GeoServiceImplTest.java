package ru.sberbank.ditsib.service.impl;

import com.google.protobuf.NullValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;
import ru.sberbank.ditsib.dto.GeoAddress;
import ru.sberbank.ditsib.exception.RouteNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка работы сервиса для взаимодействия с гео")
class GeoServiceImplTest {

    @Mock
    private GeoServiceGrpc.GeoServiceBlockingStub geoServiceStub;
    @InjectMocks
    private GeoServiceImpl geoService;
    private GeoAddress geoAddressFrom;
    private GeoAddress geoAddressTo;

    @BeforeEach
    void setUp() {
        geoAddressFrom = new GeoAddress("Россия", "Москва", "Москва",
                "Пятницкое шоссе", "д.42", null, null, BigDecimal.valueOf(37.6173),
                BigDecimal.valueOf(55.7558));
        geoAddressTo = new GeoAddress("Россия", "Москва", "Москва",
                "Малый Патриарший переулок", "д.7", null, null,
                BigDecimal.valueOf(37.6189), BigDecimal.valueOf(55.7509));
    }

    @Test
    void getRouteInfoEmpty() {
        var routeRespList = new ArrayList<GeoDescriptor.RouteResponse>();
        var routeResponses = routeRespList.iterator();

        doReturn(routeResponses).when(geoServiceStub).getRoute(any(GeoDescriptor.RouteRequest.class));
        var input =List.of(geoAddressFrom, geoAddressTo);
        assertThatThrownBy(() -> geoService.getRouteInfo(input))
                .isInstanceOf(RouteNotFoundException.class)
                .hasMessage("Не получен ответ о маршруте");
    }

    @Test
    void getRouteInfoSuccess() {
        var routeResp = List.of(GeoDescriptor.RouteResponse.newBuilder()
                .setDistance(100.0)
                .setTime(10)
                .build());
        var routeResponses = routeResp.iterator();
        var unit = "KILOMETERS";
        var routeAddresses = Stream.of(geoAddressFrom, geoAddressTo).map(this::mapToRouteAddress).toList();
        var request = GeoDescriptor.RouteRequest.newBuilder()
                .addAllCoordinates(routeAddresses)
                .setDistanceUnit(unit)
                .build();

        doReturn(routeResponses).when(geoServiceStub).getRoute(request);

        var res = geoService.getRouteInfo(List.of(geoAddressFrom, geoAddressTo));
        assertThat(res).isNotNull();
        assertThat(res.distance()).isEqualTo(BigDecimal.valueOf(100.0));
        assertThat(res.timeInSeconds()).isEqualTo(10);
        assertThat(res.distanceUnit()).isEqualTo(unit);
    }

    @Test
    void getGeoAddressWhenEmpty() {
        var address = "Москва, Пятницкое шоссе, д.42";
        var request = GeoDescriptor.AddressRequestString.newBuilder()
                .setSearch(address)
                .build();

        List<GeoDescriptor.AddressResponse> addressResponses = new ArrayList<>();
        var response = addressResponses.iterator();
        doReturn(response).when(geoServiceStub).getAddress(request);
        var result = geoService.getGeoAddress(address);

        assertThat(result).isEmpty();
    }

    @Test
    void getGeoAddressSuccess() {
        var address = "Москва, Пятницкое шоссе, д.42";
        var request = GeoDescriptor.AddressRequestString.newBuilder()
                .setSearch(address)
                .build();
        var resp = GeoDescriptor.AddressResponse.newBuilder()
                .setCountry("Россия")
                .setRegion("Москва")
                .setCity("Москва")
                .setBuilding(GeoDescriptor.NullableString.newBuilder()
                        .setNull(NullValue.NULL_VALUE)
                        .build())
                .setHouse(GeoDescriptor.NullableString.newBuilder()
                        .setData("42")
                        .build())
                .setStructure(GeoDescriptor.NullableString.newBuilder()
                        .setNull(NullValue.NULL_VALUE)
                        .build())
                .setLatitude(37.6173)
                .setLongitude(55.7558)
                .setStreet(GeoDescriptor.NullableString.newBuilder()
                        .setData("Пятницкое шоссе")
                        .build())
                .build();

        List<GeoDescriptor.AddressResponse> addressResponses = new ArrayList<>();
        addressResponses.add(resp);
        Iterator<GeoDescriptor.AddressResponse> response = addressResponses.iterator();

        doReturn(response).when(geoServiceStub).getAddress(request);
        var result = geoService.getGeoAddress(address);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).country()).isEqualTo("Россия");
        assertThat(result.get(0).city()).isEqualTo("Москва");
        assertThat(result.get(0).street()).isEqualTo("Пятницкое шоссе");
        assertThat(result.get(0).house()).isEqualTo("42");
        assertThat(result.get(0).latitude()).isEqualTo(BigDecimal.valueOf(37.6173));
        assertThat(result.get(0).longitude()).isEqualTo(BigDecimal.valueOf(55.7558));
    }

    private GeoDescriptor.RouteAddress mapToRouteAddress(GeoAddress address) {
        return GeoDescriptor.RouteAddress.newBuilder()
                .setCountry(address.country())
                .setRegion(address.region())
                .setCity(address.city())
                .setDistrict(createNullableString(null)) // district отсутствует в GeoAddress
                .setStreet(createNullableString(address.street()))
                .setHouse(createNullableString(address.house()))
                .setBuilding(createNullableString(address.building()))
                .setStructure(createNullableString(address.structure()))
                .setLatitude(address.latitude().doubleValue())
                .setLongitude(address.longitude().doubleValue())
                .build();
    }

    private GeoDescriptor.NullableString createNullableString(String value) {
        if (value == null) {
            return GeoDescriptor.NullableString.newBuilder()
                    .setNull(NullValue.NULL_VALUE)
                    .build();
        }
        return GeoDescriptor.NullableString.newBuilder()
                .setData(value)
                .build();
    }
}