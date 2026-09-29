package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

/**
 * Data transfer object with data about existing trip purpose.
 */
@Jacksonized
@Data
@Builder
@Schema(title = "Цель поездки", description = "Описание цели поездки по заявке")
public class TripPurposeDTO {
    
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    /**
     * Trip purpose
     */
    @NotNull
    @Schema(description = "Описание цели поездки", maxLength = 128)
    private String label;

}
