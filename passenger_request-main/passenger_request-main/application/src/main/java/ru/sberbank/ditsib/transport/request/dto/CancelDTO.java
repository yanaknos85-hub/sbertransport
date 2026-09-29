package ru.sberbank.ditsib.transport.request.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO with cancel reason
 */
@Getter
@Setter
@Schema(title = "Отмена", description = "Данные с причинами отмены заявки")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelDTO {
    
    /**
     * Reason.
     */
    @NotBlank
    @Schema(description = "Причина отмены")
    private String reason;
    
    @Builder.Default
    @Schema(description = "Код завершения")
    private Integer code = 0;
}
