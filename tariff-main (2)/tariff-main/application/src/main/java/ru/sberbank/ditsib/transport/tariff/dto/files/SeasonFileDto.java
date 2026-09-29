package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Объект с данными сезонного коэффициента.
 */
@Getter
@Setter
@Builder
@ToString
public class SeasonFileDto {
    
    /**
     * Значение коэффициента.
     */
    @Builder.Default
    private double value = 1;
    
    /**
     * Дата начала действия.
     */
    private LocalDate start;
    
    /**
     * Дата окончания действия.
     */
    private LocalDate end;
    
}
