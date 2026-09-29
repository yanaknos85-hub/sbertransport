package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Модель статистических данных
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LimiitsStatsResponseDTO {
    
    /**
     * Идентификатор организации.
     */
    @Schema
    private UUID organizationId;
    
    /**
     * Год
     */
    @Schema
    private Integer year;
    
    /**
     * Месяц
     */
    @Schema
    private Integer month;
    
    /**
     * Тип обслуживания
     */
    @Schema
    private TransportServiceType serviceType;
    
    /**
     * Тип транспорта
     */
    @Schema
    private TransportTypeEnum transportType;
    
    /**
     * Общий лимит
     */
    @Schema
    private Long budgetTotal;
    
    /**
     * Потраченный лимит
     */
    @Schema
    private Long budgetSpent;
}
