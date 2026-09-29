package ru.sberbank.ditsib.transport.reports.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EconomyDataDTO {
    
    /**
     * Кол-во активных заявок в совместной поездке
     */
    private final int requestCount;
    
    /**
     * Сумма всех попутчиков в поездке
     */
    private final int joinedPassengerCount;
}
