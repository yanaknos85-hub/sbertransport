package ru.sberbank.ditsib.transport.tariff.database.model;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сущность тарифа группового трансфера
 */
@Getter
@Entity
@Setter
@DiscriminatorValue(value = TransportTypeEnum.Constants.GROUP_TRANSFER_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupTransferTariff extends BaseTariffWithContract {
    
    
    @Column(name = "contractor_tariff_id")
    private String contractorTariffId;
    
    //Класс группового трансфера
    @NotNull
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    private GroupTransferClass groupTransferClass;
    
    //Вип тариф
    @Column
    private boolean vip;
    
    //Дата начала действия тарифа
    @Column(name = "tariff_start_date")
    private LocalDateTime tariffStartDate;
    
    //Дата окончания действия тарифа
    @Column(name = "tariff_end_date")
    private LocalDateTime tariffEndDate;
    
    //Цена за км, коп
    @NotNull
    @Min(0)
    @Column(name = "ride_cost_per_km")
    private Integer rideCostPerKm;
    
    //Стоимость за минуту, коп
    @NotNull
    @Min(0)
    @Column(name = "ride_cost_per_min")
    private Integer rideCostPerMin;
    
    //Минимальное время поездки в минутах
    @Column(name = "min_min")
    @Builder.Default
    private Integer minMin = 0;
    
    //Минимальная протяженность маршрута в км
    @Column(name = "min_km")
    @Builder.Default
    private Integer minKm = 0;
    
    //Стоимость минимальной поездки, коп
    @Min(0)
    @Builder.Default
    @Column(name = "min_ride_cost")
    private Integer minRideCost = 0;
    
    //Бесплатных минут ожидания, включенных в тариф
    @Min(0)
    @Builder.Default
    @Column(name = "free_waiting_time")
    private Integer freeWaitingTime = 0;
    
    //Цена за км в городе, коп
    @Min(0)
    @Builder.Default
    @Column(name = "cost_per_km_city")
    private Integer costPerKmCity = 0;
    
    //Цена за минуту в городе, коп
    @Min(0)
    @Builder.Default
    @Column(name = "cost_per_min_city")
    private Integer costPerMinCity = 0;
    
    //Цена за км за чертой города, коп
    @Min(0)
    @Builder.Default
    @Column(name = "cost_per_km_suburb")
    private Integer costPerKmSuburb = 0;
    
    //Цена за минуту за чертой города, коп
    @Min(0)
    @Builder.Default
    @Column(name = "cost_per_min_suburb")
    private Integer costPerMinSuburb = 0;
    
    //Стоимость минуты ожидания, коп
    @NotNull
    @Min(0)
    @Column(name = "wait_cost_per_min")
    private Integer waitCostPerMin;
    
    //Стоимость за минуту ожидания в промежуточной точке, коп.
    @Min(0)
    @Max(1000_00)
    @Builder.Default
    @Column(name = "wait_cost_per_min_intermediate")
    private Integer waitCostPerMinIntermediate = 0;
    
    //Детское кресло
    @Column(name = "child_seat")
    private Boolean childSeat;
    
    //Негабаритный багаж
    @Column(name = "bug_oversized")
    private Boolean bugOversized;
    
    //Животные
    @Column
    private Boolean animal;
    
    //Минимальное время для формирования заказа в минутах.
    @Column(name = "min_create_time", columnDefinition = "int4 (Types#INTEGER)")
    @Builder.Default
    private Long minCreateTime = 0L;
    
    //Минимальное время отмены поездки в минутах
    @Builder.Default
    @Column(name = "min_cancel_time", columnDefinition = "int4 (Types#INTEGER)")
    private Long minCancelTime = 0L;
    
    //Триггерное время
    @Column(name = "trigger_time", columnDefinition = "int4 (Types#INTEGER)")
    @Builder.Default
    private Long triggerTime = 60L;
    
    @Column(name = "work_group")
    private String workGroup;
    
    @Type(ListArrayType.class)
    @Column(columnDefinition = "_uuid")
    private List<UUID> transportIds;
    
    @Type(ListArrayType.class)
    @Column(columnDefinition = "_uuid", nullable = false)
    private List<UUID> regionIds;

}
