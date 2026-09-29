package ru.sberbank.ditsib.transport.reports.model.excel;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TripType;

import jakarta.persistence.*;
import java.util.UUID;

/** Строка реестра поездок на такси от контрагента за месяц.
    Содержит данные импортрованной строки + результаты проверок */
@Entity
@Table(schema = "reports", name = "taxi_trip_registry_string")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxiTripRegistryString {
    
    /** ID строки */
    @Id
    @GeneratedValue
    private UUID id;
    
    /** Вычисленный при проверке реестра статус поездки */
    @Column(name = "calc_trip_status")
    @Enumerated(EnumType.STRING)
    private CalculatedTripStatus calculatedTripStatus;
    
    /** Вычисленный при проверке реестра тип поездки */
    @Column(name = "calc_trip_type")
    @Enumerated(EnumType.STRING)
    private TripType calculatedTripType;
    
    /** Импортированная строк реестра */
    @Embedded
    private TaxiTripRegistryStringTemplate parsedString;
    
    /** Проверка 0 - Проверка совпадения ID поездки */
    @Column(name = "valid_taxi_id", nullable = false)
    @Builder.Default
    private Boolean validTaxiId0 = false;
    
    /** Проверка 1 - Проверка статуса поездки */
    @Column(name = "valid_trip_status", nullable = false)
    @Builder.Default
    private Boolean validTripStatus1 = false;
    
    /** Проверка 2 - Проверка даты поездки */
    @Column(name = "valid_trip_date", nullable = false)
    @Builder.Default
    private Boolean validTripDate2 = false;
    
    /** Проверка 3 - Проверка стоимость = 0 для отмененной поездки */
    @Column(name = "valid_cancelled_trip_cost", nullable = false)
    @Builder.Default
    private Boolean validCancelledTripCost3 = false;
    
    /** Проверка 4 - Сравнение протяженности маршрута с расчетной */
    @Column(name = "valid_calc_distance", nullable = false)
    @Builder.Default
    private Boolean validCalcDistance4 = false;
    
    /** Проверка 4a - Сравнение протяженности маршрута с фактической */
    @Column(name = "valid_fact_distance", nullable = false)
    @Builder.Default
    private Boolean validFactDistance4a = false;
    
    /** Проверка 5 - Проверка тарифа */
    @Column(name = "valid_tariff", nullable = false)
    @Builder.Default
    private Boolean validTariff5 = false;
    
    /** Проверка 6 - Сравнение стоимости поездки с расчетной */
    @Column(name = "valid_calc_cost", nullable = false)
    @Builder.Default
    private Boolean validCalcCost6 = false;
    
    /** Проверка 7 - Сравнение стоимости поездки с фактической */
    @Column(name = "valid_fact_cost", nullable = false)
    @Builder.Default
    private Boolean validFactCost7 = false;
    
    /** Проверка 8 - Проверка времени ожидания */
    @Column(name = "valid_wait_time", nullable = false)
    @Builder.Default
    private Boolean validWaitTime8 = false;
}
