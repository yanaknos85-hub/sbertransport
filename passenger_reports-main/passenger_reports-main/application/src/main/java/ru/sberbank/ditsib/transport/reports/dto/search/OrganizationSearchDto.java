package ru.sberbank.ditsib.transport.reports.dto.search;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationShortDTO;

import java.util.List;

@Data
@Builder
@Schema(title = "Данные по организациям", description = "Данные по организациям для полнотекстового поиска")
public class OrganizationSearchDto {
    
    @Schema(title = "Краткие данные об организации", description = "Данные организации")
    private List<OrganizationShortDTO> organizations;
}
