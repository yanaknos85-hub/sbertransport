package ru.sber.transport.address.providers.common_address.client.impl;

import com.google.protobuf.NullValue;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.geo.grpc.dto.GeoDescriptor;
import ru.sber.transport.geo.grpc.service.GeoServiceGrpc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка запросов адреса")
@ExtendWith(MockitoExtension.class)
class GeoClientImplTest {
    @InjectMocks
    private GeoClientImpl geoClient;

    @Mock
    GeoServiceGrpc.GeoServiceBlockingStub stub;

    @Test
    @DisplayName("Проверка запроса адреса по строке")
    void test_requestAddress_search() {
        var search = Instancio.create(String.class);
        var topLeftLat = Instancio.create(BigDecimal.class);
        var topLeftLong = Instancio.create(BigDecimal.class);
        var bottomRightLat = Instancio.create(BigDecimal.class);
        var bottomRightLong = Instancio.create(BigDecimal.class);
        var response = new ArrayList<GeoDescriptor.AddressResponse>();
        for (var i = 0; i < 10; i++) {
            response.add(GeoDescriptor.AddressResponse.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setCountry(Instancio.create(String.class))
                .setRegion(Instancio.create(String.class))
                .setCity(Instancio.create(String.class))
                .setStreet(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
                .setHouse(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
                .setSettlement(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
                .setBuilding(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
                .setStructure(GeoDescriptor.NullableString.newBuilder().setNull(NullValue.NULL_VALUE).build())
                .build());
        }

        doReturn(response.iterator()).when(stub).getAddress(any(GeoDescriptor.AddressRequestString.class));

        assertDoesNotThrow(() -> geoClient.getAddresses(search, topLeftLat, topLeftLong, bottomRightLat, bottomRightLong));
    }

    @Test
    @DisplayName("Проверка провайдера поиска по строке. null")
    void test_search_request_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> geoClient.getAddresses(null, null, null, null, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка провайдера поиска по координатам. null")
    void test_search_coordinates_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> geoClient.getAddress(null, BigDecimal.ONE))
                .isInstanceOf(NullPointerException.class);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> geoClient.getAddress(BigDecimal.ONE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка запроса адреса по координатам")
    void test_requestAddress_coordinates() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var response = GeoDescriptor.AddressResponse.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setCountry(Instancio.create(String.class))
            .setRegion(Instancio.create(String.class))
            .setCity(Instancio.create(String.class))
            .setStreet(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
            .setHouse(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
            .setSettlement(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
            .setBuilding(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
            .setStructure(GeoDescriptor.NullableString.newBuilder().setData(Instancio.create(String.class)).build())
            .build();

        doReturn(Collections.singletonList(response).iterator())
                .when(stub).getAddressCoords(any(GeoDescriptor.AddressRequestCoordinates.class));

        assertDoesNotThrow(() -> geoClient.getAddress(latitude, longitude));
    }

}