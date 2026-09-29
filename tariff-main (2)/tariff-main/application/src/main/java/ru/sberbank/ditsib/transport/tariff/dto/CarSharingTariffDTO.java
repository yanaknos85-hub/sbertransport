package ru.sberbank.ditsib.transport.tariff.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO с данными по существующему тарифу каршеринг
 **/
@Getter
@SuperBuilder(toBuilder = true)
@Schema(title = "Данные по тарифу каршеринга", description = "Данные по тарифу")
@NoArgsConstructor
@JsonPropertyOrder({ "id" })
public class CarSharingTariffDTO extends NewCarSharingTariffDTO {
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
    
    /**
     * Номер контракта
     */
    @Schema(description = "Номер контракта")
    private String contractNumber;
    
    /**
     * ID контрагента
     */
    @Schema(description = "ID контрагента")
    private String contractorId;
}
