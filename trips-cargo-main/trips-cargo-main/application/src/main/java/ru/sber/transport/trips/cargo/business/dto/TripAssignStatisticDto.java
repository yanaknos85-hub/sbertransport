package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Дто статистики по назначению трипов.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripAssignStatisticDto {

    /**
     * Общее количество.
     */
    private int totalCount;

    /**
     * Назначенное количество.
     */
    private int assignCount;

    /**
     * Не назначенное количество.
     */
    private int notAssignCount;
}
