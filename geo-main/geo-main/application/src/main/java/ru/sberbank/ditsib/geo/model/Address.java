package ru.sberbank.ditsib.geo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Объект данных адреса.
 */
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Address {
    
    /**
     * Идентификатор.
     */
    private AddressKey id;
    
    /**
     * Страна.
     */
    private String country;
    
    /**
     * Регион.
     */
    private String region;
    
    /**
     * Район.
     */
    private String district;
    
    /**
     * Город.
     */
    private String city;

    /**
     * Село.
     */
    private String settlement;

    /**
     * Жилой район.
     */
    private String livingArea;

    /**
     * Какой-либо площадный объект.
     */
    private String place;

    
    /**
     * Улица.
     */
    private String street;
    
    /**
     * Дом.]
     */
    private String house;


    /**
     * Широта.
     */
    private Double latitude;

    /**
     * Долгота
     */
    private Double longitude;

    /**
     * Аттрибуты объекта парковки.
     */
    private Object attributeGroups;

    /**
     * Набор наименований объекта.
     */
    private Object nameEx;

    /**
     * Показатель оплачиваемости парковки.
     */
    private Boolean isPaid;

    /**
     * Точка координат
     */
    private Object point;

    /**
     * Полное наименование объекта
     */
    private String fullName;

    /**
     * Наименование объекта
     */
    private String name;

    /**
     * Тип объекта
     */
    private String type;

    /**
     * Данные для отрисовки
     */
    private Object geometry;

    /**
     * Идентификатор объекта
     */
    private Object objectId;

    /**
     * Назначение объекта
     */
    private String purposeName;

    
    @Override
    public String toString() {
        return region+", "+city+", "+street+", "+house;
    }
}
