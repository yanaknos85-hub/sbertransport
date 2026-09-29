package ru.sberbank.transport.oto.cargo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@Schema(title = "Фактические данные по маршруту")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FactDataDTO {
    
    @Schema(description = "Время простоя ТС")
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    private Duration tripFactWaitTime;

    @Schema(description = "Стоимость заявки")
    private Integer tripFactPrice;
    
    @Schema(description = "Фактическое время поиска автомобиля, мин")
    private Long factSearchTime;

    @Schema(description = "Километраж")
    private Double tripFactDistance;

    @Schema(description = "Длительность поездки")
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    private Duration tripFactDuration;

    @Schema(description = "Время начала работ")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private LocalDateTime tripStartTime;

}