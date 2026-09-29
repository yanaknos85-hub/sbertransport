package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Абстрактный класс объединяющий общие поля для запросов с типом транспорта такси (T), личный транспорт(P) и
 * каршеринг (C) (TnPnC).
 */
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractRequestForTnPnC extends Request {
    
    /**
     * Flag of coop/individual trip
     */
    @Column(name = "coop_trip")
    private boolean coopTrip;
    
    /**
     * Инициатор поездки
     */
    @Column(name = "shared_ride_owner")
    private boolean sharedRideOwner;
    
    /**
     * Id заявки в маджента
     */
    @Column(name = "ride_id")
    private UUID rideId;
    
    /**
     * Passenger count
     */
    @Column(name = "passenger_count")
    @Builder.Default
    private int passengerCount = 1;
    
    /**
     * Date and time or request finishing
     */
    @Column(name = "finished_time")
    private LocalDateTime finishedTime;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Column(name = "cost_share_part")
    private Double costSharePart;
    
    /**
     * Экономия в рублях для текущего заказа
     */
    @Column(name = "savings_cash")
    private Long savingsCash;
    
    /**
     * Экономия в процентах для текущего заказа
     */
    @Column(name = "savings_procents")
    private Long savingsProcents;
    
    /**
     * Предрасчитанная цена заявки.
     */
    @Transient
    private Long requestPrice;
}
