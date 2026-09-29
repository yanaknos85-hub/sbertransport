package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO для контрагента
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Контрагент", description = "Контрагент")
public class ContractorDTO {
    
    @NotNull
    @Schema(description = "ID контрагента", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    
    @Schema(description = "Наименование контрагента")
    private String name;
    
    @Schema(description = "ОГРН")
    private String msrn;
    
    @Schema(description = "ИНН")
    private String tin;
}
