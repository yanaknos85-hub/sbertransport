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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.geo.database.geo.tables.records.CachedSuggestRequestRecord;
import ru.sber.transport.geo.database.geo.tables.records.ClusterRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.providers.CachedSuggestRequestProvider;
import ru.sberbank.ditsib.geo.providers.ClusterProvider;
import ru.sberbank.ditsib.geo.service.CacheService;

import java.util.*;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Transactional
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_geo")
@SpringBootTest
@DisplayName("Проверка cервиса кеширования с базой данных")
@EmbeddedPostgres
public class CacheServiceImplDbTest {

    @Autowired
    private ClusterProvider clusterProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheService cacheService;

    @SpyBean
    private CachedSuggestRequestProvider cachedSuggestRequestProvider;

    @Test
    @DisplayName("Получение адреса с учетом кеширования. Кеширование в процессе")
    void test_getAddressByLocation_cacheInProgress() throws JsonProcessingException {

        /*
        1) попробовать получать адрес, который ещё не был закэширован
        2) через очень короткий промежуток времени (0.1 секунды, например) отправить второй запрос на такой же текст поиска (пользователь добавлял пробел)

        Что происходит в этот момент:
        1) в первом запросе происходит поиск значения из кэша, но так как там нет, запрос уходит в 2гис
        2) тут же приходит второй запрос с практически таким же текстом (в конце пробел), происходит поиск значения из кэша (поиск идет после трима в нижнем регистре), но так как там нет, запрос уходит в 2гис
        3) приходит ответ от 2гис на первый запрос, ответ сохраняется
        4) приходит ответ от 2гис на второй запрос, но ответ не может сохранится, так как в базе уже есть пара кластер-текст
         */

        var beginLatitude = 55.0;
        var beginLongitude = 37.0;
        for (var i = 0; i < 3; i++) {
            var cluster = new ClusterRecord();
            cluster.setId(UUID.randomUUID());
            cluster.setLatitude(beginLatitude + i);
            cluster.setLongitude(beginLongitude + i);
            cluster.setRadius(10.);
            clusterProvider.save(cluster);
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

        var supplier = mock(Supplier.class);
        var addressRequest = Instancio.of(AddressRequestDto.class)
                .set(Select.field(AddressRequestDto::getCenterLatitude), beginLatitude)
                .set(Select.field(AddressRequestDto::getCenterLongitude), beginLongitude)
                .set(Select.field(AddressRequestDto::getRequestType), RequestType.BUILDING)
                .create();

        when(supplier.get()).thenReturn(response);

        doReturn(Optional.empty()).when(cachedSuggestRequestProvider).findByQuery(any(), any());

        check(addressRequest, supplier, response);
        check(addressRequest, supplier, response);
    }

    void check(AddressRequestDto addressRequest, Supplier<List<Address>> supplier, List<Address> response){
        var result = cacheService.getAddressByLocation(addressRequest, supplier);
        assertEquals(response.size(), result.size());
        for (var i = 0; i < result.size(); i++) {
            var expected = response.get(i);
            var actual = (Address) result.get(i);

            assertEquals(expected.getLatitude(), actual.getLatitude());
            assertEquals(expected.getLongitude(), actual.getLongitude());
        }
    }
}
