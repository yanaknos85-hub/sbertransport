package ru.sberbank.ditsib.geo.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.grpc.stub.StreamObserver;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sberbank.ditsib.geo.mappers.AddressMapper;
import ru.sberbank.ditsib.geo.mappers.AddressMapperImpl;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.model.Route;
import ru.sberbank.ditsib.geo.service.AddressService;
import ru.sberbank.ditsib.geo.service.RouteService;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка cервиса кеширования")
public class GeoAddressActionReplierImplTest {

    private final RouteService routeService = mock(RouteService.class);
    private final AddressService addressService = mock(AddressService.class);
    private final AddressMapper addressMapper = new AddressMapperImpl();
    private final GeoAddressActionReplierImpl geoAddressActionReplier = new GeoAddressActionReplierImpl(addressService,
            routeService, new ObjectMapper().registerModule(new JavaTimeModule()), addressMapper);

    @Test
    void test_getAddressCoordsWithEmptyCity() {
        var request = GeoDescriptor.AddressRequestCoordinates.newBuilder()
                .setLatitude(56.807235)
                .setLongitude(35.833788)
                .build();

        var addresses = Instancio.ofList(Address.class)
                .size(1)
                .ignore(field(Address::getCity))
                .ignore(field(Address::getAttributeGroups))
                .ignore(field(Address::getNameEx))
                .ignore(field(Address::getGeometry))
                .ignore(field(Address::getObjectId))
                .ignore(field(Address::getPoint))
                .create();

        when(addressService.getAddressByCoordinates(any())).thenReturn(addresses);

        var responseObserver = new StreamObserver<GeoDescriptor.AddressResponse>() {

            @Override
            public void onNext(GeoDescriptor.AddressResponse value) {
                assertThat(value.getCity())
                        .isEqualTo("");
            }

            @Override
            public void onError(Throwable t) {
                fail();
            }

            @Override
            public void onCompleted() {
            }
        };

        geoAddressActionReplier.getAddressCoords(request, responseObserver);
    }

    @Test
    void test_getRouteByCoords() {
        var count = 10;
        var points = IntStream.range(0, count)
                .mapToObj((i) -> GeoDescriptor.RouteRecreationPoint.newBuilder()
                        .setLatitude(Instancio.create(Double.class))
                        .setLongitude(Instancio.create(Double.class))
                        .setTime(Instancio.create(Long.class))
                        .build())
                .toList();
        var request = GeoDescriptor.RouteRecreationRequest.newBuilder()
                .addAllCoordinates(points)
                .build();

        var expectedRoute = Instancio.create(Route.class);
        when(routeService.routeRequest(any())).thenReturn(expectedRoute);

        var responseObserver = new StreamObserver<GeoDescriptor.RouteResponse>() {

            @Override
            public void onNext(GeoDescriptor.RouteResponse value) {
                assertEquals(expectedRoute.getDistance(), value.getDistance());
                assertEquals(expectedRoute.getSegments().size(), value.getSegmentsCount());

                var expectedSegments = expectedRoute.getSegments();
                var actualSegments = value.getSegmentsList();
                for (var i = 0; i < actualSegments.size(); i++) {
                    var expectedSegment = expectedSegments.get(i);
                    var actualSegment = actualSegments.get(i);
                    assertEquals(expectedSegment.getDistance(), actualSegment.getDistance());
                    assertEquals(expectedSegment.getCoordinates().size(), actualSegment.getCoordinatesCount());

                    var expectedCoordinates = expectedSegment.getCoordinates();
                    var actualCoordinates = actualSegment.getCoordinatesList();
                    for (var j = 0; j < expectedSegment.getCoordinates().size(); j++) {
                        var expectedCoordinate = expectedCoordinates.get(j);
                        var actualCoordinate = actualCoordinates.get(j);
                        assertEquals(expectedCoordinate.getLatitude(), actualCoordinate.getLatitude());
                        assertEquals(expectedCoordinate.getLongitude(), actualCoordinate.getLongitude());
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                fail();
            }

            @Override
            public void onCompleted() {
            }
        };

        geoAddressActionReplier.getRouteByCoords(request, responseObserver);
    }
}
