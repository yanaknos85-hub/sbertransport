package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(
        title = "Краткая информация об импортированном реестре поездок на такси от контрагента",
        description = "Краткая информация об импортированном реестре поездок на такси от контрагента"
)
public class TaxiTripRegistryShortDTO extends TaxiTripRegistryShortWithoutContractorDTO {
    
    @Schema(description = "Контрагент")
    private ContractorDTO contractor;
}