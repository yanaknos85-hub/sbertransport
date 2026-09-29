package ru.sber.transport.address.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.provider.AddressProvider;
import ru.sber.transport.address.business.use_cases.Search;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка кейсов поиска")
class SearchImplTest {

    private final AddressProvider<FrequentlyAddress> frequentlyProvider = mock(AddressProvider.class);

    private final AddressProvider<FavoriteAddress> favoriteProvider = mock(AddressProvider.class);

    private final AddressProvider<GeoAddress> geoProvider = mock(AddressProvider.class);

    private final List<AddressProvider<? extends Address>> providers = List.of(frequentlyProvider, favoriteProvider, geoProvider);

    private final Search search = new SearchImpl(providers);

    @Test
    @DisplayName("Проверка бизнес-части поиска по строке")
    void test_search_request() {
        var request = Instancio.create(String.class);
        var frequentlies = Instancio.createSet(FrequentlyAddress.class);
        var favorites = Instancio.createSet(FavoriteAddress.class);
        var geos = Instancio.createSet(GeoAddress.class);

        when(frequentlyProvider.getAddresses(request)).thenReturn(frequentlies);
        when(favoriteProvider.getAddresses(request)).thenReturn(favorites);
        when(geoProvider.getAddresses(request)).thenReturn(geos);

        var actualList = search.search(request);

        assertThat(actualList).hasSize(frequentlies.size() + favorites.size() + geos.size());

        var expectedFrequentlies = frequentlies.stream().sorted(Comparator.comparing(FrequentlyAddress::getCount).reversed()).toList();
        var expectedFavorites = favorites.stream().sorted(Comparator.comparing(FavoriteAddress::getLabel)).toList();
        var expectedGeos = geos.stream().sorted(Comparator.comparing(this::toAddressString)).toList();

        var i = 0;
        for (; i < frequentlies.size(); i++) {
            var actual = actualList.get(i);
            var expected = expectedFrequentlies.get(i);

            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
        for (var j = 0; j < favorites.size(); i++, j++) {
            var actual = actualList.get(i);
            var expected = expectedFavorites.get(j);

            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
        for (var j = 0; j < geos.size(); i++, j++) {
            var actual = actualList.get(i);
            var expected = expectedGeos.get(j);

            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
    }

    @Test
    @DisplayName("Проверка бизнес-части поиска по строке. null")
    void test_search_request_null() {
        assertThatThrownBy(() -> search.search(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка бизнес-части поиска по координатам. null")
    void test_search_coordinates_null() {
        //noinspection DataFlowIssue
        assertThatThrownBy(() -> search.search(null, BigDecimal.ONE))
            .isInstanceOf(NullPointerException.class);

        //noinspection DataFlowIssue
        assertThatThrownBy(() -> search.search(BigDecimal.ONE, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Проверка бизнес-части поиска по координатам. Результат - частый адрес")
    void test_search_latitude_longitude_frequently() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var frequently = Instancio.create(FrequentlyAddress.class);

        when(frequentlyProvider.getAddress(latitude, longitude)).thenReturn(Optional.of(frequently));

        var actual = search.search(latitude, longitude);

        assertThat(actual.getBuilding()).isEqualTo(frequently.getBuilding());
        assertThat(actual.getCity()).isEqualTo(frequently.getCity());
        assertThat(actual.getCountry()).isEqualTo(frequently.getCountry());
        assertThat(actual.getHouse()).isEqualTo(frequently.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(frequently.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(frequently.getLongitude());
        assertThat(actual.getRegion()).isEqualTo(frequently.getRegion());
        assertThat(actual.getStreet()).isEqualTo(frequently.getStreet());
        assertThat(actual.getStructure()).isEqualTo(frequently.getStructure());
    }

    @Test
    @DisplayName("Проверка бизнес-части поиска по координатам. Результат - предпочитаемый адрес")
    void test_search_latitude_longitude_favorite() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var favorite = Instancio.create(FavoriteAddress.class);

        when(favoriteProvider.getAddress(latitude, longitude)).thenReturn(Optional.of(favorite));

        var actual = search.search(latitude, longitude);

        assertThat(actual.getBuilding()).isEqualTo(favorite.getBuilding());
        assertThat(actual.getCity()).isEqualTo(favorite.getCity());
        assertThat(actual.getCountry()).isEqualTo(favorite.getCountry());
        assertThat(actual.getHouse()).isEqualTo(favorite.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(favorite.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(favorite.getLongitude());
        assertThat(actual.getRegion()).isEqualTo(favorite.getRegion());
        assertThat(actual.getStreet()).isEqualTo(favorite.getStreet());
        assertThat(actual.getStructure()).isEqualTo(favorite.getStructure());
    }

    @Test
    @DisplayName("Проверка бизнес-части поиска по координатам. Результат - geo")
    void test_search_latitude_longitude_geo() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var geo = Instancio.create(GeoAddress.class);

        when(geoProvider.getAddress(latitude, longitude)).thenReturn(Optional.of(geo));

        var actual = search.search(latitude, longitude);

        assertThat(actual.getBuilding()).isEqualTo(geo.getBuilding());
        assertThat(actual.getCity()).isEqualTo(geo.getCity());
        assertThat(actual.getCountry()).isEqualTo(geo.getCountry());
        assertThat(actual.getHouse()).isEqualTo(geo.getHouse());
        assertThat(actual.getLatitude()).isEqualTo(geo.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(geo.getLongitude());
        assertThat(actual.getRegion()).isEqualTo(geo.getRegion());
        assertThat(actual.getStreet()).isEqualTo(geo.getStreet());
        assertThat(actual.getStructure()).isEqualTo(geo.getStructure());
    }

    private String toAddressString(Address address) {
        return "%s, %s, %s, %s, %s, %s".formatted(address.getRegion(), address.getCity(), address.getStreet(), address.getHouse(), address.getBuilding(), address.getStructure());
    }

}