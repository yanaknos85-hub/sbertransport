package ru.sber.transport.address.providers.common_address;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.providers.RequestContext;
import ru.sber.transport.address.providers.Viewport;
import ru.sber.transport.address.providers.common_address.client.GeoClient;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка провайдера адресов")
class GeoAddressProviderImplTest {

    private final GeoClient client = mock(GeoClient.class);

    private final RequestContext context = mock(RequestContext.class);

    private final AddressProvider<GeoAddress> provider = new GeoAddressProviderImpl(client, context);

    @Test
    @DisplayName("Проверка поиска по строке")
    void test_search() {
        var search = Instancio.create(String.class);
        var viewport = Instancio.create(Viewport.class);
        var expectedList = Instancio.createList(GeoAddress.class);

        when(context.getViewport()).thenReturn(viewport);
        when(client.getAddresses(search, viewport.getTopLeftLatitude(), viewport.getTopLeftLongitude(), viewport.getBottomRightLatitude(), viewport.getBottomRightLongitude()))
            .thenReturn(expectedList);

        var addresses = provider.getAddresses(search);

        assertThat(addresses).hasSameElementsAs(expectedList);
    }

    @Test
    @DisplayName("Проверка провайдера поиска по строке. null")
    void test_search_request_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> provider.getAddresses(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка провайдера поиска по координатам. null")
    void test_search_coordinates_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> provider.getAddress(null, BigDecimal.ONE))
            .isInstanceOf(NullPointerException.class);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> provider.getAddress(BigDecimal.ONE, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка поиска по строке. Ошибка")
    void test_search_error() {
        var search = Instancio.create(String.class);
        var viewport = Instancio.create(Viewport.class);

        when(context.getViewport()).thenReturn(viewport);
        when(client.getAddresses(search, viewport.getTopLeftLatitude(), viewport.getTopLeftLongitude(), viewport.getBottomRightLatitude(), viewport.getBottomRightLongitude()))
            .thenThrow(new RuntimeException("Failed"));

        var addresses = provider.getAddresses(search);

        assertThat(addresses).isEmpty();
    }

    @Test
    @DisplayName("Проверка поиска по координатам")
    void test_search_coordinates() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var expected = Instancio.create(GeoAddress.class);

        when(client.getAddress(latitude, longitude)).thenReturn(List.of(expected));

        var addresses = provider.getAddress(latitude, longitude);

        assertThat(addresses).isEqualTo(Optional.of(expected));
    }

    @Test
    @DisplayName("Проверка поиска по координатам. Нет ответа")
    void test_search_coordinates_noResult() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);

        when(client.getAddress(latitude, longitude)).thenReturn(List.of());

        var addresses = provider.getAddress(latitude, longitude);

        assertThat(addresses).isEmpty();
    }

    @Test
    @DisplayName("Проверка поиска по координатам. Ошибка")
    void test_search_coordinates_error() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);

        when(client.getAddress(latitude, longitude)).thenThrow(new ResolutionException("Failed"));

        var addresses = provider.getAddress(latitude, longitude);

        assertThat(addresses).isEmpty();
    }

}