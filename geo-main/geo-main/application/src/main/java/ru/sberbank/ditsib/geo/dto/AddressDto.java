package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Информация об адресе", description = "Данные адреса")
public class AddressDto {
    
    @Schema(description = "Страна")
    private String country;
    
    /**
     * Region.
     */
    @Schema(description = "Регион")
    private String region;
    
    @Schema(description = "Район")
    private String district;
    
    @Schema(description = "Город")
    private String city;

    @Schema(description = "Село")
    private String settlement;

    @Schema(description = "Жилой район")
    private String livingArea;

    @Schema(description = "Площадной объект")
    private String place;
    
    @Schema(description = "Улица")
    private String street;
    
    @Schema(description = "Дом")
    private String house;
    
    @Schema(description = "Широта")
    private Double latitude;
    
    @Schema(description = "Долгота")
    private Double longitude;

    @Schema(description = "Атрибуты")
    private Object attributeGroups;

    @Schema(description = "Наименование объекта")
    private Object nameEx;

    @Schema(description = "Параметр оплачиваемости парковки")
    private Boolean isPaid;

    @Schema(description = "Точка")
    private Object point;

    @Schema(description = "Полное имя объекта")
    private String fullName;

    @Schema(description = "Имя объекта")
    private String name;

    @Schema(description = "Тип объекта")
    private String type;

    @Schema(description = "Данные для отрисовки")
    private Object geometry;

    @Schema(description = "ID объекта")
    private Object objectId;

    @Schema(description = "Назначение")
    private String purposeName;

}
