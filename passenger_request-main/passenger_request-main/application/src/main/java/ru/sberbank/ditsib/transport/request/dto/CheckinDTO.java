package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * Object with data about waypoint.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@Schema(title = "Данные для чекин", description = "Данные для чекин")
@AllArgsConstructor
@NoArgsConstructor
public class CheckinDTO {
    
    /**
     * Identifier of request
     */
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;
    
    /**
     * Latitude.
     */
    @Schema(description = "Широта")
    private double latitude;
    
    /**
     * Longitude.
     */
    @Schema(description = "Долгота")
    private double longitude;
    
    /**
     * Decline reason.
     */
    @Schema(description = "Причина отсутствия")
    private String absenceReason;
    
    /**
     * Порядковый индекс (нумерация с 0).
     */
    private Integer orderingIndex;
}
