package ru.sberbank.ditsib.transport.srm.dto.twogis;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для матрицы расстояний 2гис - ответ.
 */
public record TwoGisMatrixAsyncCheckStatusDto(
        @JsonProperty("task_id") String taskId,
        String status,
        Integer code,
        String message,
        @JsonProperty("result_link") String resultLink
) {
}

