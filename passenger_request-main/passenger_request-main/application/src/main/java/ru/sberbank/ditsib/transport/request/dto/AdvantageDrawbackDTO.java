package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(title = "Данные преимущества/недостатка", description = "Данные необходимые для отображения преимущества/недостатка")
@Data
@Builder
public class AdvantageDrawbackDTO {
    @Schema(description = "Порядковый номер", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer order;
    
    @Schema(description = "Код варианта ответа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
    
    @Schema(description = "Текст варианта ответа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    
    @Schema(description = "Код иконки ответа", requiredMode = Schema.RequiredMode.REQUIRED)
    private String image;
}
