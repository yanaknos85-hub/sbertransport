package ru.sberbank.ditsib.transport.request.messaging.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatsMessage implements Message<UUID> {
    
    /**
     * ID..
     */
    private UUID id;
    
    /**
     * Идентификатор организации.
     */
    @Schema(description = "ID заявки")
    private UUID organizationId;
    
    /**
     * Год
     */
    @Schema(description = "ID заявки")
    private Integer year;
    
    /**
     * Месяц
     */
    @Schema(description = "ID заявки")
    private Integer month;
    
    /**
     * Тип обслуживания
     */
    @Schema(description = "ID заявки")
    private TransportServiceType serviceType;
    
    /**
     * Тип транспорта
     */
    @Schema(description = "ID заявки")
    private TransportTypeEnum transportType;
    
    /**
     * Выполненные успешно заявки количество
     */
    @Schema(description = "ID заявки")
    private Long totalExecuted;
    
    /**
     * Отмененные заявки количество
     */
    @Schema(description = "ID заявки")
    private Long totalCanceled;
    
    /**
     * Не выполенные заявки с промежуточными статусами количество
     */
    @Schema(description = "ID заявки")
    private Long totalNotExecuted;
    
    /**
     * Сумма по всем заявкам кроме заявок со статусом canceled в рублях
     */
    @Schema(description = "ID заявки")
    private Long totalSum;
    
    /**
     * Заявки без нарушений контрольных сроков количество
     */
    @Schema(description = "ID заявки")
    private Long slaWithoutViolation;
    
    /**
     * Заявки с нарушениями контрольных сроков количество
     */
    @Schema(description = "ID заявки")
    private Long slaWithViolation;
    
    /**
     * Заявки с 4-5 звезд количество
     */
    @Schema(description = "ID заявки")
    private Long csiStarPositive;
    
    /**
     * Заявки с 1-3 звезд количество
     */
    @Schema(description = "ID заявки")
    private Long csiStarNegative;

}
