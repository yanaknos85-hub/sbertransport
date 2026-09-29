package ru.sber.transport.trip.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Дто статистики по назначению трипов.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(title = "Данные по статистике", description = "Данные по статистике назначенных поездок")
public class TripAssignStatisticDto {

    /**
     * Общее количество.
     */
    @Schema(title = "Число", description = "Общее количество")
    private int totalCount;

    /**
     * Назначенное количество.
     */
    @Schema(title = "Число", description = "Назначенное количество")
    private int assignCount;

    /**
     * Не назначенное количество.
     */
    @Schema(title = "Число", description = "Не назначенное количество")
    private int notAssignCount;
}
