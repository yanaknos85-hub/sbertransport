package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.utils.reflection.DoubleUtil;

import java.time.Duration;

/**
 * Object with data about waypoint.
 */
@Data
@Builder(toBuilder = true)
@Schema(title = "Точки поездки", description = "Узловые точки")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WaypointDTO {
    
    /**
     * Country.
     */
    @Schema(description = "Страна")
    private String country;
    
    /**
     * Region.
     */
    @Schema(description = "Регион")
    private String region;
    
    /**
     * City.
     */
    @Schema(description = "Город")
    private String city;
    
    /**
     * Street.
     */
    @Schema(description = "Улица")
    private String street;
    
    /**
     * House.
     */
    @Schema(description = "Дом")
    private String house;
    
    /**
     * Building.
     */
    @Schema(description = "Строение")
    private String building;
    
    /**
     * Structure.
     */
    @Schema(description = "Корпус")
    private String structure;
    
    /**
     * Latitude.
     */
    @Schema(description = "Широта")
    private double latitude;
    
    /**
     * Longitude.
     */
    @Schema(description = "Долгота")
    private double longitude;
    
    /**
     * Address from VSP/GOSB/TB registry
     */
    @Schema(description = "Адрес найден в реестре ВСП/ГОСБ/ТБ")
    @Builder.Default
    private boolean existInVspGosbTbRegistry = false;
    
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время ожидания")
    private Duration waitTime;
    
    /**
     * Автоматический чекин
     */
    @Schema(description = "Автоматический чекин")
    private boolean checkinAutomatic;
    
    /**
     * Ручной чекин
     */
    @Schema(description = "Ручной чекин")
    private boolean checkinManual;
    
    /**
     * Причина отсутствия
     */
    @Schema(description = "Причина отсутствия")
    private String absenceReason;
    
    /**
     * Режим чекина
     */
    @Schema(description = "Режим чекина: true это только ручной чекин, false ручной и автоматический")
    private boolean checkinOnlyManual;
    
    /**
     * Активность
     */
    @Schema(description = "Режим активности точки: true в маршруте, false стерта")
    @Builder.Default
    private boolean active = true;
    
    public boolean equalsByCoords(WaypointDTO toCompare) {
        if (toCompare == null) {
            return false;
        }
        if (toCompare.getLatitude() == 0 || toCompare.getLongitude() == 0) {
            return false;
        }
        return DoubleUtil.doubleEqualsWithPrecision(getLatitude(),
                                                    toCompare.getLatitude(),
                                                    DoubleUtil.EPSILON)
               && DoubleUtil.doubleEqualsWithPrecision(getLongitude(), toCompare.getLongitude(),
                                                       DoubleUtil.EPSILON);
    }
}
