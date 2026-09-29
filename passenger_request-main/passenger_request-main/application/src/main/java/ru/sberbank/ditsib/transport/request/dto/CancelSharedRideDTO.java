package ru.sberbank.ditsib.transport.request.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * DTO with cancel reason
 */
@Getter
@Setter
@Schema(title = "Отмена", description = "Данные с причинами отмены заявки")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelSharedRideDTO {
    
    @Builder.Default
    @Schema(description = "Код завершения")
    boolean result = false;
    
    /**
     * Reason.
     */
    @Schema(description = "Владелец поездки")
    private UUID ownerRequest;
    
    /**
     * Reason.
     */
    @Schema(description = "Причина отмены")
    private String errorDescription;
}
