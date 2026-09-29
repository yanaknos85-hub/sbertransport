package ru.sber.transport.telemechanic.dto.transport;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.UUID;

@Setter
@Getter
@Schema(title = "Поиск транспортных средств", description = "Фильтры для транспортного средства")
public class TransportSearchDto extends PageSortFilterParameters<TransportSearchParameters> {
    
    public TransportSearchDto() {
        super(TransportSearchParameters.STATE_NUMBER);
    }
    
    @Schema(description = "Гос номер")
    private String stateNumber;
    
    @Schema(description="Идентификатор организации")
    private UUID organizationId;
    
    @Schema(description ="Идентификатор подразделения")
    private UUID departmentId;
    
}
