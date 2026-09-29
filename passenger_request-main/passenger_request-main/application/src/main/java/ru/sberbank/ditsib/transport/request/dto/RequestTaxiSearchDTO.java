package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "Поиск заявки", description = "Фильтры для поиска заявок")
public class RequestTaxiSearchDTO extends RequestSearchDTO{

    @Schema(description = "Идентификатор лимита")
    private UUID limitId;

    @Schema(description = "Тип поездки")
    private Boolean coopTrip;

    @Schema(description = "Идентификатор совместной поездки")
    private Integer sharedRideId;
    
    @Schema(description = "ID совместной поездки")
    private UUID rideId;
}
