package ru.sber.transport.tariff.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.RequestOptions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Данные для расчёта стоимости поездки
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Поездка", description = "Данные о поездке для расчета стоимости")
public class TripDto {
    
    @NotNull
    @Schema(description = "Идентификатор организации текущего пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID organizationId;
    
    @Schema(description = "Идентификатор текущего пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID employeeId;
    
    @Min(0)
    @NotNull
    @Schema(description = "Пробег поездки в км", requiredMode = Schema.RequiredMode.REQUIRED)
    private double distance;
    
    @NotNull
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время в пути", requiredMode = Schema.RequiredMode.REQUIRED)
    private Duration time;
    
    
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Builder.Default
    @Schema(description = "Время ожидания в первой точке подачи", defaultValue = "Без ожидания")
    private final Duration waitingTime = Duration.ZERO;
    
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Builder.Default
    @Schema(description = "Суммарное время ожидания в промежуточных точках", defaultValue = "Без ожидания")
    private final Duration intermediateWaitingTime = Duration.ZERO;
    
    @Builder.Default
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    @Schema(description = "Дата поездки", defaultValue = "Текущее время")
    private LocalDateTime tripDate = LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(5);
    
    @Builder.Default
    @Schema(description = "Временная зона")
    private String timeZone = "GMT+03";
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Прогнозные баллы по пробкам", defaultValue = "0")
    private final double trafficJamScore = 0d;
    
    @Builder.Default
    @Schema(description = "Список опций, влияющих на расчет стоимости")
    private final Set<RequestOptions> options = new HashSet<>();
    
    @Builder.Default
    @Schema(description = "Параметры пробега за чертой города")
    private final SuburbTripDataDTO suburbTripData = new SuburbTripDataDTO();
    
    @Min(0)
    @Builder.Default
    @Schema(description = "Объем двигателя используемого личного ТС, см^3", defaultValue = "1000")
    private final int engineVolume = 1000;
    
    /**
     * Координаты начала маршрута.
     */
    @Schema(description = "Координаты начала маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
    private WaypointDTO startPoint;
    
    @Builder.Default
    @Schema(description = "Количество билетов на общественном транспорте")
    @Valid
    private final PublicTripDTO publicTripData = new PublicTripDTO();
    
    @Builder.Default
    @Schema(description = "Количество точек маршрута для оплаты грузчиков")
    @Valid
    private int countPoint = 0;
    
    @Builder.Default
    @Schema(description = "Экспресс доставка")
    @Valid
    private boolean express = false;
    
    /**
     * Координаты конечной точки маршрута.
     */
    @Schema(description = "Грузы. Координаты конечной точки маршрута")
    private WaypointDTO stopPoint;
    
    @Schema(description = "Грузы. Вес груза в кг")
    private double weight;
    
    @Schema(description = "Дополнительная информация о заявке")
    @Builder.Default
    private InformationDTO information = new InformationDTO();
    
    @Schema(description = "Вип тариф", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private boolean vip;
    
    @Schema(description = "Список точек маршрута")
    private List<WaypointDTO> waypoints;
    
    /**
     * Таймзона клиента
     */
    @JsonIgnore
    private String clientTimeZone;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InformationDTO {
        @Schema(description = "Детское кресло", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
        private boolean childSeat;
        
        @Schema(description = "Негабаритный багаж", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
        private boolean bugOversized;
        
        @Schema(description = "Животные", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
        private boolean animal;
    }
}
