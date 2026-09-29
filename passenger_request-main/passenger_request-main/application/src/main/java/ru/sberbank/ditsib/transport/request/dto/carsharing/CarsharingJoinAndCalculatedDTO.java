package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.tariff.model.CalculatedDto;

/**
 * DTO для отображения подключения к корп.каршерингам, исходя из тарифов каршеринга
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(title = "DTO для отображения подключения к корп.каршерингу",
        description = "Содержит связь тарифа и признак подключения к нему сотрудника")
public class CarsharingJoinAndCalculatedDTO {
    
    @Schema(description = "Контрагент")
    private ContractorDTO contractor;
    
    // todo выпилить регион, когда будет реализовано региональное деление
    @Schema(description = "Регион")
    private String region;
    
    @Schema(description = "Расчет по тарифу, для которого необходимо указать признак подключения сотрудника")
    private CalculatedDto calculatedData;
    
    @Schema(description = "Признак подключения сотрудника")
    private boolean joined;
}
