package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "История заявки", description = "Элемент истории изменений заявки")
@Builder
public class RequestHistoryElementDTO {
    
    @Schema(description = "Время регистрации действия", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime changeDate;
    
    
    @Schema(description = "Установленный статус", requiredMode = Schema.RequiredMode.REQUIRED)
    private TripRequestStatus status;
    
    
    @Schema(description = "Код статуса (Используется только для части статусов)")
    @Builder.Default
    private Integer code = 0;
    
    @Schema(description = "ФИО инициатора", requiredMode = Schema.RequiredMode.REQUIRED)
    private String initiator;
}
