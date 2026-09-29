package ru.sber.transport.srm.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sber.transport.constants.PointMatchingType;
import ru.sberbank.ditsib.converters.MagentaTimeDeserializer;
import ru.sberbank.ditsib.converters.ZonedDateTimeToUTCSerializer;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO точки маршрута для публикации
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder
@Schema(title = "Запрос SRM", description = "Запрос SRM")
public class SrmRequestDTO {
    
    /**
     * ID заявки
     */
    @Schema(description = "ID заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID requestId;
    
    /**
     * Количество пассажиров
     */
    @Schema(description = "Число пассажиров")
    private Integer requiredPassengers;
    
    /**
     * Объем груза
     */
    @Schema(description = "Объем груза")
    private Double requiredVolume;
    
    /**
     * Вес груза
     */
    @Schema(description = "Вес груза")
    private Double requiredWeight;
    
    /**
     * Идентификатор тарифа в мадженте, UUID->positive Long
     */
    @NotNull(message = "Tariff count cannot be null")
    @Schema(description = "ID тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Время начала поездки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @Schema(description = "Желаемое время отъезда")
    private ZonedDateTime pickupTime;
    
    /**
     * Время прибытия во последнюю точку маршрута
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @Schema(description = "Желаемое время прибытия")
    private ZonedDateTime dropTime;
    
    /**
     * Временная зона.
     */
    @Schema(description = "Временная зона")
    private String timeZone;
    
    /**
     * Тип совмещения точек.
     */
    @Schema(description = "Тип совмещения точек")
    private PointMatchingType pointMatchingType;
    
    /**
     * Оптимизация маршрута.
     */
    @Schema(description = "Режим ЭКСПРЕСС для грузов")
    @Builder.Default
    private Boolean cargoExpress = Boolean.FALSE;
    
    /**
     * Предрасчитанная цена заявки.
     */
    @Schema(description = "Предрасчитанная цена заявки")
    private Long requestPrice;
    
    /**
     * Тип транспорта
     */
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    /**
     * точки маршрута
     */
    @Size(min = 2, message = "Minimum of 2 waypoints is required")
    @Builder.Default
    @Schema(description = "Точки маршрута", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SrmWaypointPostDTO> waypoints = new ArrayList<>();
}
