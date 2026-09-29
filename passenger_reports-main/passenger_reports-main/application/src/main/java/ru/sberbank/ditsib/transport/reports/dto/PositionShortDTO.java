package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Data transfer object with data about position.
 */
@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткие данные должности", description = "Данные должности сотрудника")
public class PositionShortDTO {
    /**
     * Identifier
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Position name
     */
    @Schema(description = "Название")
    private String positionName;
    
    /**
     * Organization id
     */
    @Schema(description = "Идентификатор организации")
    private UUID organizationId;
}
