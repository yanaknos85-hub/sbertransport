package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Доступные типы запрашиваемых данных.
 */
@Schema(title = "Тип запрашиваемых данных", description = "Тип запрашиваемых данных. Возможные варианты:" +
                                                          "`BUILDING` - здания")
public enum RequestType {
    
    /**
     * Здания.
     */
    BUILDING,
    
    /**
     * Парковки.
     */
    PARKING
}
