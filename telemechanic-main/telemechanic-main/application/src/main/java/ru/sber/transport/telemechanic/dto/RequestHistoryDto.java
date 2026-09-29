package ru.sber.transport.telemechanic.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;

/**
 * DTO история изменения заявок
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "История изменения заявок", description = "Данные по истории изменения заявок")
public class RequestHistoryDto {
    
    /**
     * Дата и время изменения заявки
     */
    @Schema(description = "Дата и время изменения заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime changeTime;
    
    /**
     * Статус заявки
     */
    @Schema(description = "Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 255)
    private String status;
    
    /**
     * Тип статуса.
     */
    @Schema(description = "Тип статуса (прошедший/текущий)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private RequestHistoryDto.TypeEnum type = TypeEnum.PAST;
    
    /**
     * ФИО сотрудника инициатора изменения
     */
    @Schema(description = "ФИО сотрудника инициатора изменения", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String initiatorName;
    
    public enum TypeEnum {
        PAST,
        NOW
    }
}
