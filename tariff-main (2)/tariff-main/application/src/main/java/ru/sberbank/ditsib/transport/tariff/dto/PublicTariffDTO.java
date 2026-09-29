package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO с данными по существующему тарифу на компенсацию общественного транспорта
 **/
@Getter
@SuperBuilder
@NoArgsConstructor
@JsonPropertyOrder({ "id", "humanReadableId", "active" })
@Schema(title = "Данные по тарифу на компенсацию общественного транспорта", description = "Данные по тарифу")
public class PublicTariffDTO extends NewPublicTariffDTO {
    /**
     * ID
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * ID (human readable).
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * Активный ли тариф или удален
     */
    @Schema(description = "Активный ли тариф или удален")
    private boolean active;
}
