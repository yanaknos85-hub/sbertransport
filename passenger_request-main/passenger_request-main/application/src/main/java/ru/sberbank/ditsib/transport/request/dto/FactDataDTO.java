package ru.sberbank.ditsib.transport.request.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(title = "Фактические данные по маршруту")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FactDataDTO {
    
    /**
     * Время начала поездки
      */
    @Schema(description = "Время начала поездки")
    private LocalDateTime tripStartTime;
    
    /**
     * Стоимость заявки
      */
    @Schema(description = "Стоимость заявки")
    private Integer tripFactPrice;
    
    /**
     * Время простоя ТС
      */
    @Schema(description = "Время простоя ТС")
    private Long tripFactWaitTime;
    
    /**
     * Километраж
      */
    @Schema(description = "Километраж")
    private Double tripFactDistance;
    
    /**
     * Длительность поездки
      */
    @Schema(description = "Длительность поездки")
    private Long tripFactDuration;
    
    /**
     * Перевозчик
      */
    @Schema(description = "Перевозчик")
    private UUID organizationId;
}

