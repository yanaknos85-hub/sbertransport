package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharingJoinStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Контрагент и статус подключения и выбор сотрудника", description = "DTO для обработки Инженером ОТО")
public class ProcessedContractorAndJoinStatusDTO {
    
    @NotNull @Valid
    @Schema(description = "Контрагент", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContractorDTO contractor;
    
    @NotBlank
    @Schema(description = "Регион", requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;
    
    @NotNull
    @Schema(description = "Статус подключения", requiredMode = Schema.RequiredMode.REQUIRED)
    private CorporateCarsharingJoinStatus joinStatus;
}
