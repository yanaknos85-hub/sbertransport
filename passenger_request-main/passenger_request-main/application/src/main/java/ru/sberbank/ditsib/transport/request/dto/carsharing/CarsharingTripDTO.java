package ru.sberbank.ditsib.transport.request.dto.carsharing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO для Информации по каршерингу запрос
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Фактическая информация о поездке", description = "Фактическая информация о поездке")
public class CarsharingTripDTO {
    
    @Schema(description = "Идентификатор")
    private UUID id;
    
    @Schema(description = "Идентификатор аренды во внешней системе")
    private Integer rentId;
    
    @Schema(description = "Марка и модель машины")
    private String carModel;
    
    @Schema(description = "Госномер машины")
    private String carNumber;
    
    @Schema(description = "Дата и время начала аренды")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime rentCreatedAt;
    
    @Schema(description = "Полная стоимость (руб)")
    private Double totalCost;
    
    @Schema(description = "Общее время поездки (мин)")
    private Integer drivingTime;
    
    @Schema(description = "Общая стоимость поездки (руб)")
    private Double drivingTimeCost;
    
    @Schema(description = "Общий пробег (км)")
    private Integer drivingLength;
    
    @Schema(description = "Дополнительная стоимость за пробег (руб)")
    private Double drivingLengthCost;
    
    @Schema(description = "Общее время ожидания (мин)")
    private Integer parkingTime;
    
    @Schema(description = "Общая стоимость ожидания (руб)")
    private Double parkingTimeCost;
    
    @Schema(description = "Общее время брони (мин)")
    private Integer reserveTime;
    
    @Schema(description = "Общая стоимость брони (руб)")
    private Double reserveTimeCost;
    
    @Schema(description = "Дата и время завершения аренды")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime rentFinishedAt;
    
    @Schema(description = "Адрес начала аренды")
    private String startAddress;
    
    @Schema(description = "Адрес окончания аренды")
    private String finishAddress;
    
}
