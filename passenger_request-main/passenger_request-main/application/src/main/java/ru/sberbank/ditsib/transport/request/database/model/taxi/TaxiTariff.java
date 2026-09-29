package ru.sberbank.ditsib.transport.request.database.model.taxi;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.*;

import java.util.UUID;

/**
 * Cущность тарифа, получаемая из сообщения
 */
@Getter
@Entity
@Setter
@DiscriminatorValue(value = TransportTypeEnum.Constants.TAXI_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TaxiTariff extends BaseTariffWithContract {
    
    /**
     * Контрагент
     */
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    /**
     * Идентификатор тарифа контрагента.
     */
    @Column(name="contractor_tariff_id")
    private String contractorTariffId;
    
    /**
     * Класс такси.
     */
    @NotNull
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    /**
     * Цена за км, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_km")
    private int rideCostPerKm;
    
    /**
     * Бесплатных километров пути, включенных в тариф.
     */
    @Min(0)
    @Max(100)
    @Builder.Default
    @Column(name = "distance_included")
    private double distanceIncluded = 0d;
    
    /**
     * Стоимость минимальной поездки с включенным расстоянием, коп.
     */
    @Min(0)
    @Builder.Default
    @Column(name = "min_ride_distance_cost")
    private Integer minRideDistanceCost = 0;
    
    /**
     * Стоимость за минуту, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "ride_cost_per_min")
    private int rideCostPerMin;
    
    /**
     * Бесплатных минут пути, включенных в тариф.
     */
    @Min(0)
    @Builder.Default
    @Column(name = "minutes_included")
    private int timeIncluded = 0;
    
    /**
     * Стоимость минимальной поездки с включенным временем, коп.
     */
    @Min(0)
    @Builder.Default
    @Column(name = "min_ride_time_cost")
    private int minRideTimeCost = 0;
    
    /**
     * Стоимость за минуту ожидания в стартовой точке, коп.
     */
    @NotNull
    @Min(0)
    @Max(1000_00)
    @Column(name = "wait_cost_per_min")
    private int waitCostPerMin;
    
    /**
     * Стоимость за минуту ожидания в промежуточной точке, коп.
     */
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "wait_cost_per_min_intermediate")
    private int waitCostPerMinIntermediate = 0;
    
    /**
     * Бесплатных минут ожидания, включенных в тариф.
     */
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "free_waiting_time")
    private int freeWaitingTime = 0;
    
    /**
     * Стоимость подачи такси, коп.
     */
    @Column(name = "car_service_cost")
    private int carServiceCost;
    
    /**
     * Временные параметры.
     */
    @Embedded
    @Builder.Default
    private TimedTariffParams timedTariffParams = new TimedTariffParams();
    
    /**
     * Параметры для подбора совместной поездки.
     */
    @Embedded
    @Builder.Default
    private CoopTariffParams coopTariffParams = new CoopTariffParams();
    
    /**
     * Параметры тарифа за чертой города.
     */
    @Embedded
    @Builder.Default
    private SuburbTariffParams suburbTariffParams = new SuburbTariffParams();
    
    /**
     * Параметры тарифа по допустимым отклонениям ряда параметров от реестра контрагента.
     */
    @Embedded
    @Builder.Default
    private ContractorDeviationsTariffParams contractorDeviationParams = new ContractorDeviationsTariffParams();
    
    /**
     * Коэффициент доплаты за пробки на дорогах (баллы Яндекс, прогнозные) Более traffic.threshold  баллов (
     * каждый балл больше traffic.threshold увеличивает на x%).
     */
    @Min(0)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_traffic")
    private double coefTraffic = 0d;
    
    /**
     * Коэффициент доплаты за детское кресло.
     */
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_child_seat")
    private double coefChildSeat = 1d;
    
    /**
     * Коэффициент доплаты за перевозку животного.
     */
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_pet_transport")
    private double coefPetTransport = 1d;
    
    /**
     * Коэффициент доплаты за лыжи/сноуборд/велосипед.
     */
    @Min(1)
    @Max(10)
    @Builder.Default
    @Column(name = "coef_bicycle")
    private double coefBicycle = 1d;
    
    /**
     * Коэффициент организации.
     */
    @Builder.Default
    @Positive
    @Max(10)
    @Column(name = "coef_org")
    private double coefOrg = 1d;
    
    /**
     * Рабочая группа
     */
    @Column(name = "work_group")
    private String workGroup;
    
    /**
     * Триггерное время
     */
    @Column(name = "trigger_time")
    @PositiveOrZero
    @Builder.Default
    private int triggerTime = 60;
    
    /**
     * Признак - является ночным тарифом
     */
    @Column(name = "is_night_tariff")
    private Boolean isNightTariff;
}
