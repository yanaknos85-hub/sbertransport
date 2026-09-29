package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Контрагент и выбор сотрудника", description = "DTO для создания / редактирования заявки")
public class ContractorAndJoinStatusDTO {
    
    @NotNull @Valid
    @Schema(description = "Контрагент", requiredMode = Schema.RequiredMode.REQUIRED)
    private ContractorDTO contractor;
    
    @NotBlank
    @Schema(description = "Регион", requiredMode = Schema.RequiredMode.REQUIRED)
    private String region;
    
    @NotNull
    @Schema(description = "Выбор сотрудником каршеринга, к которому необходимо подключение", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean employeeChoice;
}
