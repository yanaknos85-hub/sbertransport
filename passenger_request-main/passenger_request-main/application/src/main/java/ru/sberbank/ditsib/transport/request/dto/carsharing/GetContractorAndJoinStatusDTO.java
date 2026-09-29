package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharingJoinStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "Контрагент, статус подключения и выбор сотрудника", description = "DTO для получения данных")
public class GetContractorAndJoinStatusDTO {
    
    @Schema(description = "Контрагент")
    private ContractorDTO contractor;
    
    @Schema(description = "Регион")
    private String region;
    
    @Schema(description = "Статус подключения")
    private CorporateCarsharingJoinStatus joinStatus;
    
    @Schema(description = "Выбор сотрудником каршеринга, к которому необходимо подключение")
    private boolean employeeChoice;
}
