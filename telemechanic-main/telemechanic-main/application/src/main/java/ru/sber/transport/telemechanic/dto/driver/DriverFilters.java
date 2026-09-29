package ru.sber.transport.telemechanic.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.UUID;

@Getter
@Setter
@Schema(title = "Фильтры для поиска водителей")
public class DriverFilters extends PageSortFilterParameters<DriverSearchFilterParameters> {
    
    public DriverFilters() {
        super(DriverSearchFilterParameters.SEARCH_TEXT);
    }
    
    @Schema(description = "Строка поискового запроса")
    private String searchText;
    
    @Schema(description = "Идентификатор транспортного средства", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID transportId;
    
}
