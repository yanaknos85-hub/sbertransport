package ru.sberbank.transport.oto.cargo.dto;

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
    
    
    /**
     * ID контрагента
     */
    @Schema(description = "ID контрагента")
    private UUID id;
    
    /**
     * Наименование контрагента
     */
    @Schema(description = "Наименование контрагента")
    private String name;
}
