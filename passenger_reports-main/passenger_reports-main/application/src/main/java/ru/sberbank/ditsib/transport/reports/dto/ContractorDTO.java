package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Информация о контрагенте", description = "Информация о контрагенте")
@Builder
public class ContractorDTO {
    
    @Schema(description = "ID контрагента")
    private UUID id;
    
    @Schema(description = "Наименование контрагента")
    private String name;
}
