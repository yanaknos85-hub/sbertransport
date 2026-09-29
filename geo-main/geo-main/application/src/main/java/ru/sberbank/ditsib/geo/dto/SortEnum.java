package ru.sberbank.ditsib.geo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 *  Возможные варианты сортировки данных ответа.
 */
@Schema(title = "Сортировка данных", description = "Возможные варианты сортировки данных" +
                                                   "`DISTANCE` - расстояние")
public enum SortEnum {
    
    /**
     * Расстояние.
     */
    DISTANCE
}
