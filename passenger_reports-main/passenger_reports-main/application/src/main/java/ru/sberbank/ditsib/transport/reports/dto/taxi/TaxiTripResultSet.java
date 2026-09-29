package ru.sberbank.ditsib.transport.reports.dto.taxi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxiTripResultSet {
    private UUID id;
    private String humanReadableId;
    
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    private Duration tripFactWaitTime;
    
    private Integer tripFactPrice;
    
    private Double tripFactDistance;
    
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    private Duration tripFactDuration;
    
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime tripStartTime;
    
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime factParametersSettingTime;
    
    private UUID rideId;
    
    private UUID requestId;
    
    /**
     * Фактическое время ожидания в минутах
     */
    private Double registryFactWaitingTime;
    
    /**
     * Человекочитаемый идентификатор реестра
     */
    private String registryHumanReadableId;
    
    /**
     * Фактическая стоимость
     */
    private Double registryFactCost;
    
    /**
     * Фактическое расстояние
     */
    private Double registryFactDistance;
    
    /**
     * Факт оплаты
     */
    private Boolean registryFactPayment;
}
