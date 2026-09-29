package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.List;

@Schema(title = "Настройки для оценки", description = "Настройки для оценки сервисов пассажирских перевозок")
@Builder
@Data
public class PassengerEvalSettingsDTO {
    
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    @Schema(description = "Преимущества", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvantageDrawbackDTO> advantages;
    
    @Schema(description = "Недостатки", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AdvantageDrawbackDTO> drawbacks;
}
