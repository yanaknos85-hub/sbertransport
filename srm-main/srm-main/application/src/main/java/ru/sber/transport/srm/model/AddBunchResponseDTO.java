package ru.sber.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO совместной поездки magenta
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode
public class AddBunchResponseDTO {
   
    /**
     * Остановки
     */
    @Schema(description = "Список ошибочных заявок", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private List<AddBunchFaultDTO> faultList = new ArrayList<>();
    
    /**
     * KPI по отдельны заказам в поездке
     */
    @Schema(description = "Список совмещенных совместных поездок", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    private List<SrmSharedRideDTO> sharedRideList = new ArrayList<>();
}
