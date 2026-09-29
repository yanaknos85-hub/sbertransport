package ru.sberbank.ditsib.geo.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Объект обмена данными по адресу.
 */
@Builder
@Getter
public class AddressRequestDto {

    /**
     * Широта.
     */
    private final Double latitude;

    /**
     * Долгота.
     */
    private final Double longitude;

    /**
     * Сортировка данных.
     */
    @Builder.Default
    private final SortEnum sort = SortEnum.DISTANCE;

    /**
     * Тип запроса.
     */
    @Builder.Default
    private final RequestType requestType = RequestType.BUILDING;

    /**
     * Радиус поиска.
     */
    private final Integer radius;

    /**
     * Строка для поиска.
     */
    private final String location;

    /**
     * Широта центральной точки для поиска местоположения без указания города.
     */
    private final Double centerLatitude;

    /**
     * Долгота центральной точки для поиска местоположения без указания города.
     */
    private final Double centerLongitude;
}
