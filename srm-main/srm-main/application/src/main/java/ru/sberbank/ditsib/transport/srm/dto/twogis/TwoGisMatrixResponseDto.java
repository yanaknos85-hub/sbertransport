package ru.sberbank.ditsib.transport.srm.dto.twogis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO для матрицы расстояний 2гис - ответ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoGisMatrixResponseDto {

    public Integer generation_time;
    public List<Route> routes = new ArrayList<>();

    public record Route(
            Integer distance,
            Integer duration,
            Integer source_id,
            String status,
            Integer target_id
    ) {
    }
}

