package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Schema(title = "ID контрагента и реестры", description = "Данные контрагента и его реестры")
public class RegistryPerContractorDTO {
    
    /**
     * Контрагент
     */
    @Schema(description = "Контрагент")
    ContractorDTO contractor;
    
    /**
     * Список реестров для контрагента
     */
    @Schema(description = "Список реестров для контрагента")
    List<TaxiTripRegistryShortWithoutContractorDTO> taxiTripRegisters;
}
