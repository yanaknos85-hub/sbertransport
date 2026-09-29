package ru.sberbank.ditsib.transport.request.dto.constant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "Класс транспортного средства")
public class TransportClassDTO {
    
    @Schema(description = "Значение константы")
    private final String value;
    
    @Schema(description = "Название на русском")
    private final String rusName;
    
}
