package ru.sber.transport.request.external.providers.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.geo_zones.grpc.dto.GeoZonesDescriptor;
import ru.sber.transport.geo_zones.grpc.service.GeoZonesServiceGrpc;
import ru.sber.transport.request.external.model.waypoint.WaypointDTO;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_request_external")
@DisplayName("Проверка провайдера геозон")
class GeoZonesGrpcProviderImplTest {

    private final GeoZonesServiceGrpc.GeoZonesServiceBlockingStub stub = mock(GeoZonesServiceGrpc.GeoZonesServiceBlockingStub.class);

    private final GeoZonesProvider geoZonesProvider = new GeoZonesGrpcProviderImpl(stub);

    @Test
    @DisplayName("Получение геозоны")
    void test_getGeoZone() {
        final var waypoint = Instancio.create(WaypointDTO.class);
        final var code = Instancio.create(String.class);
        final var id = Instancio.create(String.class);
        final var timeZone = "+03:00";

        final var response = GeoZonesDescriptor.Region.newBuilder()
                .setCode(code)
                .setId(id)
                .setTimeZone(timeZone)
                .build();
        when(stub.region(any(GeoZonesDescriptor.Waypoint.class))).thenReturn(response);

        final var actual = geoZonesProvider.get(waypoint);

        assertThat(actual.timeZone()).isEqualTo(timeZone);
    }
}