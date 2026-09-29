package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO с данными по существующему тарифу группового трансфера
 */
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@JsonPropertyOrder({ "id", "tariffId", "humanReadableId", "active" })
@Schema(title = "Данные по тарифу группового трансфера", description = "Данные по тарифу")
public class GroupTransferTariffDTO extends NewGroupTransferTariffDTO {
    /**
     * ID
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * ID тарифа из системы Банка
     */
    @Schema(description = "Копия идентификатора")
    private String tariffId;
    
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

    /**
     * Номер контракта.
     */
    @Schema(description = "Номер контракта")
    private String contractNumber;
}
