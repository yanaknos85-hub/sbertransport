package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Schema(title = "Расчёт стоимости поездки", description = "Расчёт стоимости поездки")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDTO {
    
    @Schema(description = "Стоимость")
    private Long cost;
    
    @Schema(description = "Тариф")
    private UUID tariffId;
    
}
