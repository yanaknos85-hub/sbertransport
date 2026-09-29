package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;

/**
 * Условия
 */
@Getter
@Setter
public class ConditionsFileDto {
    /**
     * Минимальное время формирования заказа, мин
     */
    private long minCreateTime;
    
    /**
     * Минимальное время отмены заявки, мин
     */
    private long minCancelTime;
    
    /**
     * Триггерное время, мин
     */
    private long triggerTime;
}
