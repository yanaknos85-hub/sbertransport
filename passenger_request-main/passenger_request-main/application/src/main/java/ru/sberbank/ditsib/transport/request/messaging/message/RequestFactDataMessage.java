package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Сообщение с фактическими данными о заявке, полученными от контрагента
 */
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestFactDataMessage implements Message<UUID> {
    
    /**
     * Идентификатор заявки
     */
    private UUID id;
    
    /**
     * Человекочитаемый идентификатор заявки
     */
    private String hrId;
    
    /**
     * Фактическая стоимость
     */
    private Double factCost;
    
    /**
     * Фактическое расстояние
     */
    private Double factDistance;
    
    /**
     * Фактическое время ожидания в минутах
     */
    private Double factTotalWaitingTime;
    
    /**
     * Человекочитаемый идентификатор реестра
     */
    private String registryHrId;
    
    /**
     * Факт оплаты
     */
    private Boolean isPaid;
    
    @Override
    public UUID getId() {
        return id;
    }
}
