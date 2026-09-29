package ru.sber.transport.address.web.controller.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.client.HttpStatusCodeException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.address.business.model.Address;
import ru.sber.transport.address.business.model.FavoriteAddress;
import ru.sber.transport.address.business.model.FrequentlyAddress;
import ru.sber.transport.address.business.model.GeoAddress;
import ru.sber.transport.address.business.use_cases.Search;
import ru.sber.transport.address.providers.RequestContext;
import ru.sber.transport.address.providers.Viewport;
import ru.sber.transport.address.web.mapper.FavoriteAddressWebMapperImpl;
import ru.sber.transport.address.web.mapper.FrequentlyAddressWebMapperImpl;
import ru.sber.transport.address.web.mapper.GeoAddressWebMapperImpl;
import ru.sber.transport.web.api.AddressesApiDelegate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_address")
@DisplayName("Проверка контроллера адресов")
class AddressControllerImplTest {

    private final Search search = mock(Search.class);

    private final RequestContext context = mock(RequestContext.class);

    private final AddressesApiDelegate delegate = new AddressControllerImpl(search, new FrequentlyAddressWebMapperImpl(), new FavoriteAddressWebMapperImpl(), new GeoAddressWebMapperImpl(), context);

    @Test
    @DisplayName("Получение данных. Нет данных в запросе")
    void test_get_noData() {
        assertThatThrownBy(() -> delegate.getMap(null, null, null, null, null, null, null))
            .isInstanceOf(HttpStatusCodeException.class)
            .hasMessage("400 Один из параметров, search, latitude или longitude должен быть указан");
    }

    @Test
    @DisplayName("Получение списка адресов по строке")
    void test_getMap_location() {
        var location = Instancio.create(String.class);
        var topLeftLat = Instancio.create(BigDecimal.class);
        var topLeftLong = Instancio.create(BigDecimal.class);
        var bottomRightLat = Instancio.create(BigDecimal.class);
        var bottomRightLong = Instancio.create(BigDecimal.class);
        var frequentlies = Instancio.createList(FrequentlyAddress.class);
        var favorites = Instancio.createList(FavoriteAddress.class);
        var geos = Instancio.createList(GeoAddress.class);
        var addresses = Stream.concat(frequentlies.parallelStream(), Stream.concat(favorites.parallelStream(), geos.parallelStream())).map(Address.class::cast).collect(Collectors.toList());

        Collections.shuffle(addresses);

        when(search.search(location)).thenReturn(addresses);

        var result = delegate.getMap(location, null, null, topLeftLat, topLeftLong, bottomRightLat, bottomRightLong);

        var viewportCaptor = ArgumentCaptor.forClass(Viewport.class);

        verify(context).setViewport(viewportCaptor.capture());

        assertThat(viewportCaptor.getValue().getBottomRightLatitude()).isEqualTo(bottomRightLat);
        assertThat(viewportCaptor.getValue().getBottomRightLongitude()).isEqualTo(bottomRightLong);
        assertThat(viewportCaptor.getValue().getTopLeftLatitude()).isEqualTo(topLeftLat);
        assertThat(viewportCaptor.getValue().getTopLeftLongitude()).isEqualTo(topLeftLong);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().keySet()).hasSameElementsAs(List.of("FREQUENTLY", "FAVORITE", "COMMON"));

        frequentlies = frequentlies.stream().sorted(Comparator.comparing(Address::getHouse)).toList();
        var actualList = result.getBody().get("FREQUENTLY").stream().sorted(Comparator.comparing(ru.sber.transport.web.model.Address::getHouse)).toList();

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = frequentlies.get(i);
            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCount()).isEqualTo(expected.getCount());
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLabel()).isNull();
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
        favorites = favorites.stream().sorted(Comparator.comparing(Address::getHouse)).toList();
        actualList = result.getBody().get("FAVORITE").stream().sorted(Comparator.comparing(ru.sber.transport.web.model.Address::getHouse)).toList();

        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = favorites.get(i);
            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCount()).isNull();
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
        geos = geos.stream().sorted(Comparator.comparing(Address::getHouse)).toList();
        actualList = result.getBody().get("COMMON").stream().sorted(Comparator.comparing(ru.sber.transport.web.model.Address::getHouse)).toList();
        for (var i = 0; i < actualList.size(); i++) {
            var actual = actualList.get(i);
            var expected = geos.get(i);
            assertThat(actual.getBuilding()).isEqualTo(expected.getBuilding());
            assertThat(actual.getCity()).isEqualTo(expected.getCity());
            assertThat(actual.getCount()).isNull();
            assertThat(actual.getCountry()).isEqualTo(expected.getCountry());
            assertThat(actual.getHouse()).isEqualTo(expected.getHouse());
            assertThat(actual.getLabel()).isNull();
            assertThat(actual.getLatitude()).isEqualTo(expected.getLatitude());
            assertThat(actual.getLongitude()).isEqualTo(expected.getLongitude());
            assertThat(actual.getRegion()).isEqualTo(expected.getRegion());
            assertThat(actual.getStreet()).isEqualTo(expected.getStreet());
            assertThat(actual.getStructure()).isEqualTo(expected.getStructure());
        }
    }

    @Test
    @DisplayName("Получение списка адресов по ккоринатам")
    void test_getMap_latitude_longitude() {
        var latitude = Instancio.create(BigDecimal.class);
        var longitude = Instancio.create(BigDecimal.class);
        var frequently = Instancio.create(FrequentlyAddress.class);

        when(search.search(latitude, longitude)).thenReturn(frequently);

        var result = delegate.getMap(null, latitude, longitude, null, null, null, null);

        assertThat(result).isNotNull();
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().keySet()).hasSameElementsAs(List.of("FREQUENTLY", "FAVORITE", "COMMON"));

        var actual = result.getBody().get("FREQUENTLY").get(0);
        assertThat(actual.getBuilding()).isEqualTo(frequently.getBuilding());
        assertThat(actual.getCity()).isEqualTo(frequently.getCity());
        assertThat(actual.getCount()).isEqualTo(frequently.getCount());
        assertThat(actual.getCountry()).isEqualTo(frequently.getCountry());
        assertThat(actual.getHouse()).isEqualTo(frequently.getHouse());
        assertThat(actual.getLabel()).isNull();
        assertThat(actual.getLatitude()).isEqualTo(frequently.getLatitude());
        assertThat(actual.getLongitude()).isEqualTo(frequently.getLongitude());
        assertThat(actual.getRegion()).isEqualTo(frequently.getRegion());
        assertThat(actual.getStreet()).isEqualTo(frequently.getStreet());
        assertThat(actual.getStructure()).isEqualTo(frequently.getStructure());
    }
}