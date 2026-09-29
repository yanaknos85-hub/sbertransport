package ru.sberbank.ditsib.geo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.geo.database.geo.tables.records.CachedSuggestRequestRecord;
import ru.sber.transport.geo.database.geo.tables.records.ClusterRecord;
import ru.sber.transport.geo.database.geo.tables.records.NoisePointRecord;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.providers.CachedSuggestRequestProvider;
import ru.sberbank.ditsib.geo.providers.ClusterProvider;
import ru.sberbank.ditsib.geo.providers.NoisePointProvider;
import ru.sberbank.ditsib.geo.service.CacheService;
import ru.sberbank.ditsib.geo.utils.geometry.SphereUtil;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@DisplayName("Проверка cервиса кеширования")
class CacheServiceImplTest {

    private final ClusterProvider clusterProvider = mock(ClusterProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NoisePointProvider noisePointProvider = mock(NoisePointProvider.class);
    private final CachedSuggestRequestProvider cachedSuggestRequestProvider = mock(CachedSuggestRequestProvider.class);

    @Test
    @DisplayName("Получение адреса с учетом кеширования")
    void test_getAddressByLocation() throws JsonProcessingException {
        var clusterCount = 3;
        var clusters = new LinkedList<ClusterRecord>();
        for (var i = 0; i < clusterCount; i++) {
            var cluster = new ClusterRecord();
            cluster.setId(UUID.randomUUID());
            cluster.setLatitude(55.0 + i);
            cluster.setLongitude(37.0 + i);
            cluster.setRadius(10.);
            clusters.add(cluster);
        }

        var response = Instancio.ofList(Address.class)
                .ignore(Select.field(Address::getGeometry))
                .ignore(Select.field(Address::getObjectId))
                .ignore(Select.field(Address::getPoint))
                .ignore(Select.field(Address::getAttributeGroups))
                .ignore(Select.field(Address::getNameEx))
                .create();
        var cachedRequest = new CachedSuggestRequestRecord();
        cachedRequest.setResponse(JSON.valueOf(objectMapper.writeValueAsString(response)));

        when(clusterProvider.findAll()).thenReturn(clusters);
        when(cachedSuggestRequestProvider.findByQuery(any(), any())).thenReturn(Optional.of(cachedRequest));

        var supplier = mock(Supplier.class);
        var addressRequest = Instancio.of(AddressRequestDto.class)
                .set(Select.field(AddressRequestDto::getCenterLatitude), clusters.get(0).getLatitude())
                .set(Select.field(AddressRequestDto::getCenterLongitude), clusters.get(0).getLongitude())
                .set(Select.field(AddressRequestDto::getRequestType), RequestType.BUILDING)
                .create();

        var cacheService = new CacheServiceImpl(clusterProvider, objectMapper, noisePointProvider, cachedSuggestRequestProvider, new SphereUtil(), 5);
        var result = cacheService.getAddressByLocation(addressRequest, supplier);
        assertEquals(response.size(), result.size());
        for (var i = 0; i < result.size(); i++) {
            var expected = response.get(i);
            var actual = (Address) result.get(i);

            assertEquals(expected.getLatitude(), actual.getLatitude());
            assertEquals(expected.getLongitude(), actual.getLongitude());
        }
    }

    @Test
    @DisplayName("Получение адреса с учетом кеширования. Не найден кластер")
    void test_getAddressByLocation_withoutCluster() throws InterruptedException {
        when(clusterProvider.findAll()).thenReturn(new ArrayList<>());

        var supplier = mock(Supplier.class);
        var addressRequest = Instancio.of(AddressRequestDto.class)
                .set(Select.field(AddressRequestDto::getRequestType), RequestType.BUILDING)
                .create();

        var response = Instancio.ofList(Address.class)
                .ignore(Select.field(Address::getGeometry))
                .ignore(Select.field(Address::getObjectId))
                .ignore(Select.field(Address::getPoint))
                .ignore(Select.field(Address::getAttributeGroups))
                .ignore(Select.field(Address::getNameEx))
                .create();

        when(supplier.get()).thenReturn(response);

        var cacheService = new CacheServiceImpl(clusterProvider, objectMapper, noisePointProvider, cachedSuggestRequestProvider, new SphereUtil(), 5);
        var result = cacheService.getAddressByLocation(addressRequest, supplier);
        assertEquals(response.size(), result.size());
        for (var i = 0; i < result.size(); i++) {
            var expected = response.get(i);
            var actual = (Address) result.get(i);

            assertEquals(expected.getLatitude(), actual.getLatitude());
            assertEquals(expected.getLongitude(), actual.getLongitude());
        }

        Thread.sleep(Duration.ofSeconds(1));

        var captor = ArgumentCaptor.forClass(NoisePointRecord.class);
        verify(noisePointProvider).save(captor.capture());

        var noise = captor.getValue();
        assertEquals(addressRequest.getCenterLatitude(), noise.getLatitude());
        assertEquals(addressRequest.getCenterLongitude(), noise.getLongitude());
        assertEquals(addressRequest.getLocation(), noise.getQuery());
    }

    @Test
    @DisplayName("Получение адреса с учетом кеширования. Нет кешированного запроса")
    void test_getAddressByLocation_noCachedRequest() throws JsonProcessingException, InterruptedException {
        var clusterCount = 3;
        var clusters = new LinkedList<ClusterRecord>();
        for (var i = 0; i < clusterCount; i++) {
            var cluster = new ClusterRecord();
            cluster.setId(UUID.randomUUID());
            cluster.setLatitude(55.0 + i);
            cluster.setLongitude(37.0 + i);
            cluster.setRadius(10.);
            clusters.add(cluster);
        }

        var response = Instancio.ofList(Address.class)
                .ignore(Select.field(Address::getGeometry))
                .ignore(Select.field(Address::getObjectId))
                .ignore(Select.field(Address::getPoint))
                .ignore(Select.field(Address::getAttributeGroups))
                .ignore(Select.field(Address::getNameEx))
                .create();

        var responseJson = objectMapper.writeValueAsString(response);

        when(clusterProvider.findAll()).thenReturn(clusters);
        when(cachedSuggestRequestProvider.findByQuery(any(), any())).thenReturn(Optional.empty());

        var supplier = mock(Supplier.class);
        var addressRequest = Instancio.of(AddressRequestDto.class)
                .set(Select.field(AddressRequestDto::getCenterLatitude), clusters.get(0).getLatitude())
                .set(Select.field(AddressRequestDto::getCenterLongitude), clusters.get(0).getLongitude())
                .set(Select.field(AddressRequestDto::getRequestType), RequestType.BUILDING)
                .create();

        when(supplier.get()).thenReturn(response);

        var cacheService = new CacheServiceImpl(clusterProvider, objectMapper, noisePointProvider, cachedSuggestRequestProvider, new SphereUtil(), 5);
        var result = cacheService.getAddressByLocation(addressRequest, supplier);
        assertEquals(response.size(), result.size());
        for (var i = 0; i < result.size(); i++) {
            var expected = response.get(i);
            var actual = (Address) result.get(i);

            assertEquals(expected.getLatitude(), actual.getLatitude());
            assertEquals(expected.getLongitude(), actual.getLongitude());
        }

        var cacheCaptor = ArgumentCaptor.forClass(CachedSuggestRequestRecord.class);
        Thread.sleep(Duration.ofSeconds(3));
        verify(cachedSuggestRequestProvider).save(cacheCaptor.capture());

        var cached = cacheCaptor.getValue();
        assertEquals(clusters.get(0).getId(), cached.getClusterId());
        assertEquals(addressRequest.getLocation().toLowerCase(Locale.ROOT).trim(), cached.getQuery());
        assertEquals(responseJson, cached.getResponse().data());
    }
}
