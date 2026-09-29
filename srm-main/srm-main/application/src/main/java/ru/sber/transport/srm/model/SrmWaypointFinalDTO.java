package ru.sber.transport.srm.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.converters.MagentaTimeDeserializer;
import ru.sberbank.ditsib.converters.ZonedDateTimeToUTCSerializer;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO Адреса маршрута, результат публикации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
@Schema(title = "Точка маршрута", description = "Точка маршрута")
public class SrmWaypointFinalDTO {
    
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    @NotNull
    private UUID id;
    
    /**
     * тип остановки
     */
    @NotBlank
    private String eventType;
    
    /**
     * Список заказов сходящихся в точке
     */
    @Builder.Default
    private List<RequestData> requestDataList = new ArrayList<>();
    
    /**
     * Рассчитанное время остановки для начала поездки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @NotNull
    private ZonedDateTime startTime;
    
    /**
     * Рассчитанное время остановки для окончания поездки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @NotNull
    private ZonedDateTime endTime;
    
    /**
     * Время ожидания
     */
    private Integer waitingTime;
    
    /**
     * Широта
     */
    @NotNull
    private Double latitude;
    
    /**
     * Долгота
     */
    @NotNull
    private Double longitude;

    /**
     * Адрес точки
     */
    @NotNull
    private String address;
    
    /**
     * Порядковый номер
     */
    @NotNull
    private Integer orderingIndex;
    
    /**
     * Количество грузчиков
     */
    @Builder.Default
    private Integer loaderNumber = 0;
    
    /**
     * Расстояние от предыдушей точки в км
     */
    private Double distanceFromPrevWaypoint;
    
    @AllArgsConstructor
    @Data
    public static class RequestData {
        /**
         * ID заказа
         */
        @NotNull
        private UUID requestKpiId;
    
        /**
         * тип остановки
         */
        @NotBlank
        private String eventType;
    }
}
