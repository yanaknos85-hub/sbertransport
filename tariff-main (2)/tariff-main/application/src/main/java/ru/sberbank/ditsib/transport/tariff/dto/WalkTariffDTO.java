package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO с данными по существующему тарифу пешком
 **/
@Getter
@SuperBuilder
@JsonPropertyOrder({ "id", "humanReadableId", "active" })
@Schema(title = "Данные по тарифу пешком", description = "Данные по тарифу")
public class WalkTariffDTO extends NewWalkTariffDTO {
    /**
     * ID
     */
    @Schema(description = "Идентификатор")
    private final UUID id;
    
    /**
     * ID (human readable).
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private final String humanReadableId;
    
    /**
     * Активный ли тариф или удален
     */
    @Schema(description = "Активный ли тариф или удален")
    private boolean active;
}
