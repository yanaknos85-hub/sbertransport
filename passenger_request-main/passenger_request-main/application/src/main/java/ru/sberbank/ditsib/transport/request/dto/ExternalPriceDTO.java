package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * DTO for external price
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Данные для получения информации о ценах внешних провайдеров",
        description = "Данные для получения информации о ценах внешних провайдеров")
public class ExternalPriceDTO {
    
    /**
     * Data and calculations about waypoints
     */
    @NotNull
    @Valid
    private ExpectedDataDTO expected;
    
}