package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
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