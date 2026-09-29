package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Регион", description = "Информация по региону")
public class RegionDto {
    
    @Schema(title = "Идентификатор", description = "Идентификатор региона")
    private UUID id;
    
    @Schema(title = "Код", description = "Код региона")
    private String code;
    
    @Schema(title = "Название", description = "Наименование региона")
    private String name;
    
    @Schema(title = "Идентификатор родителя", description = "Идентификатор родительского региона")
    private UUID parentId;
    
    @Schema(title = "Временная зона", description = "Временная зона региона")
    private String timeZone;
}