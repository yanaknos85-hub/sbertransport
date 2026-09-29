package ru.sberbank.ditsib.transport.srm.dto.twogis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * DTO для матрицы расстояний 2гис - ответ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TwoGisMatrixAsyncCreateDto {
    
    public String task_id;
    public String message;
    public String status;
    
}

