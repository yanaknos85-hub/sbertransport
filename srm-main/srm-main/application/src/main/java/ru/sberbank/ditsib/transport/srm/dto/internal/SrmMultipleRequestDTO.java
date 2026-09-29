package ru.sberbank.ditsib.transport.srm.dto.internal;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class SrmMultipleRequestDTO {
    
    /**
     * ID заявки
     */
    @Schema(description = "ID заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID multipleRequestId;
    
    /**
     * Идентификатор тарифа в мадженте, UUID->positive Long
     */
    @Schema(description = "ID тарифа", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID tariffId;
    
    /**
     * Время начала поездки
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @Schema(description = "Желаемое время отъезда", requiredMode = Schema.RequiredMode.REQUIRED)
    private ZonedDateTime pickupTime;
    
    /**
     * Время прибытия во последнюю точку маршрута
     */
    @JsonSerialize(using = ZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @Schema(description = "Желаемое время прибытия", requiredMode = Schema.RequiredMode.REQUIRED)
    private ZonedDateTime dropTime;
    
    /**
     * Временная зона.
     */
    @Schema(description = "Временная зона", requiredMode = Schema.RequiredMode.REQUIRED)
    private String timeZone;
    
    /**
     * Тип совмещения точек.
     */
    @Schema(description = "Тип совмещения точек", requiredMode = Schema.RequiredMode.REQUIRED)
    private PointMatchingType pointMatchingType;
    
    /**
     * Тип транспорта.
     */
    @NotNull
    @Schema(description = "Тип транспорта", requiredMode = Schema.RequiredMode.REQUIRED)
    private TransportTypeEnum transportType;
    
    /**
     * Предрасчитанная цена заявки.
     */
    @Schema(description = "Предрасчитанная цена заявки", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long requestPrice;
    
    /**
     * Список заявок.
     */
    @Builder.Default
    List<SrmSingleRequestDTO> requestDTOList = new ArrayList<>();
}
