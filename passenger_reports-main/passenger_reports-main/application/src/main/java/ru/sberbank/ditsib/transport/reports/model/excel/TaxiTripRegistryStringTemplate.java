package ru.sberbank.ditsib.transport.reports.model.excel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import org.springframework.core.annotation.Order;
import ru.sberbank.ditsib.transport.reports.service.impl.ImportColumn;

import java.time.LocalDateTime;

/**
 * Класс-шаблон для строки реестра поездок на такси от контрагента Дополнительно аннотирован с пом. {@link Order}: нумерация начиная с 0, в том
 * порядке, как столбцы в книге Excel
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxiTripRegistryStringTemplate {
    
    /**
     * Порядковый № строки
     */
    @ImportColumn(0)
    @Column(name = "ordinal")
    private Integer ordinal;
    
    /**
     * ID строки (поездки) в системе контрагента
     */
    @ImportColumn(1)
    @Column(name = "taxi_id"/*, nullable = false*/)
    private String taxiTripId;
    
    /**
     * Подразделение заказчика (код МВЗ)
     */
    @ImportColumn(2)
    @Column(name = "department_mvz"/*, nullable = false*/)
    private String departmentMVZ;
    
    /**
     * Желаемое время подачи автомобиля
     */
    @ImportColumn(3)
    @Column(name = "desired_time"/*, nullable = false*/)
    private LocalDateTime desiredTime; //todo уточнить про null
    
    /**
     * Желаемая дата подачи автомобиля (дубль колонки)
     */
    @ImportColumn(4)
    @Column(name = "desired_date"/*, nullable = false*/)
    private LocalDateTime desiredDate;  //todo уточнить про null
    
    /**
     * Вид тарифа
     */
    @ImportColumn(5)
    @Column(name = "taxi_class"/*, nullable = false*/)
    private String taxiClass;  //todo как это сопоставлять? и надо ли
    
    /**
     * Адрес подачи ТС
     */
    @ImportColumn(6)
    @Column(name = "start_address"/*, nullable = false*/, length = 10000)
    private String startAddress;
    
    /**
     * Адреса промежуточных точек маршрута
     */
    @ImportColumn(7)
    @Column(name = "intermediate_address", length = 10000)
    private String intermediateAddresses;
    
    /**
     * Адрес конечного пункта
     */
    @ImportColumn(8)
    @Column(name = "finish_address"/*, nullable = false*/, length = 10000)
    private String finishAddress;
    
    /**
     * Фактическое время подачи ТС
     */
    @ImportColumn(9)
    @Column(name = "fact_time"/*, nullable = false*/)
    private LocalDateTime factTime;
    
    /**
     * Фактическое время начала поездки
     */
    @ImportColumn(10)
    @Column(name = "fact_start_time"/*, nullable = false*/)
    private LocalDateTime factStartTime; //todo уточнить про null
    
    /**
     * Фактическое время окончания поездки
     */
    @ImportColumn(11)
    @Column(name = "fact_finish_time"/*, nullable = false*/)
    private LocalDateTime factFinishTime; //todo это может быть строкой (":отменен" приписка)
    
    /**
     * Фактическое время ожидания (простоя), мин
     */
    @ImportColumn(12)
    @Column(name = "fact_wait_time"/*, nullable = false*/)
    private Integer factWaitTimeMin;
    
    /**
     * Время ожидания в промежуточных точках, мин
     */
    @ImportColumn(13)
    @Column(name = "intermediate_wait_time"/*, nullable = false*/)
    private Integer intermediateWaitTimeMin;
    
    /**
     * Фактический километраж поездки, км
     */
    @ImportColumn(14)
    @Column(name = "fact_distance_km"/*, nullable = false*/)
    private Double factDistanceKm;
    
    /**
     * Стоимость минимальной поездки (без НДС), руб
     */
    @ImportColumn(15)
    @Column(name = "fact_min_cost_rub"/*, nullable = false*/)
    private Double factMinimalCostRub;
    
    /**
     * Стоимость ожидания (без НДС), руб/мин
     */
    @ImportColumn(16)
    @Column(name = "fact_wait_cost_rub_per_min"/*, nullable = false*/)
    private Double factWaitCostRub;
    
    /**
     * Тариф (без НДС), руб/км
     */
    @ImportColumn(17)
    @Column(name = "fact_price_rub_per_km"/*, nullable = false*/)
    private Double factPriceRub;
    
    /**
     * Итого (без НДС), руб
     */
    @ImportColumn(18)
    @Column(name = "sum_without_nds_rub"/*, nullable = false*/)
    private Double factSumWithoutNdsRub;
    
    /**
     * Итого НДC, руб
     */
    @ImportColumn(19)
    @Column(name = "nds_rub"/*, nullable = false*/)
    private Double factNdsRub;
    
    /**
     * Итого c НДC, руб
     */
    @ImportColumn(20)
    @Column(name = "sum_rub"/*, nullable = false*/)
    private Double factSumRub;  //todo уточнить про null
}
