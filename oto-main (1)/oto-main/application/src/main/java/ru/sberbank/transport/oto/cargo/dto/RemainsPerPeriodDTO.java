package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Остатки за период", description = "Остатки за период")
public class RemainsPerPeriodDTO {
    
    /**
     * Request
     */
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;
    
    /**
     * Limit
     */
    @Schema(description = "Идентификатор лимита")
    private UUID limitId;

    /**
     * Limit
     */
    @Schema(description = "Тип лимита")
    private String limitType;

    /**
     * Limit
     */
    @Schema(description = "Человекочитаемый идентификатор лимита")
    private String limitHumanId;
    
    /**
     * Department.
     */
    @Schema(description = "Подразделение", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID departmentId;
    
    /**
     * Year
     */
    @Schema(description = "Период", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer period;
    
    /**
     * Sum
     */
    @Schema(description = "Остатки за период", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long balancePerPeriod;
}

    

    

