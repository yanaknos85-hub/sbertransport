package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
public class RequestStatsResponseDTO {
    
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
     * Выполненные успешно заявки количество
     */
    @Schema
    private Integer totalExecuted;
    
    /**
     * Отмененные заявки количество
     */
    @Schema
    private Integer totalCanceled;
    
    /**
     * Не выполенные заявки с промежуточными статусами количество
     */
    @Schema
    private Integer totalNotExecuted;
    
    /**
     * Сумма по всем заявкам кроме заявок со статусом canceled в рублях
     */
    @Schema
    private Long totalSum;
    
    /**
     * Заявки без нарушений контрольных сроков количество
     */
    @Schema
    private Integer slaWithoutViolation;
    
    /**
     * Заявки с нарушениями контрольных сроков количество
     */
    @Schema
    private Integer slaWithViolation;
    
    /**
     * Заявки с 4-5 звезд количество
     */
    @Schema
    private Integer csiStarPositive;
    
    /**
     * Заявки с 1-3 звезд количество
     */
    @Schema
    private Integer csiStarNegative;
}
