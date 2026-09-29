package ru.sberbank.ditsib.transport.srm.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * DTO с KPI заказа из совместной поездки
 */
@Entity
@Table(schema = "srm", name = "srm_request_kpi")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString(exclude = "sharedRide")
@Builder
@Schema(title = "KPI заказа из совместной поездки", description = "Рассчитанные параметры заказа на поездку")
public class SrmRequestKpi {
    
    /**
     * Уникальный ключ записи и ID первичного заказа пришедшего из вне
     */
    @Id
    private UUID id;
    
    /**
     * Уникальный ключ записи и ID первичного заказа пришедшего из вне
     */
    @Column(name = "org_request_id")
    private UUID orgRequestId;
    
    /**
     * Уникальный ключ записи и ID первичного заказа пришедшего из вне
     */
    @Column(name = "old_id")
    private Integer oldId;
    
    /**
     * Ссылка на совместную поездку
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_ride_id", columnDefinition = "int8")
    private SrmSharedRide sharedRide;
    
    /**
     * Время начала поездки
     */
    @Column(name = "pickup_time")
    private ZonedDateTime pickupTime;
    
    /**
     * Время завершения поездки
     */
    @Column(name = "drop_time")
    private ZonedDateTime dropTime;
    
    /**
     * Количество пассажиров
     */
    @Column(name = "required_passengers")
    private Integer requiredPassengers;
    
    /**
     * Объем груза
     */
    @Column(name = "required_volume")
    private Double requiredVolume;
    
    /**
     * Вес груза
     */
    @Column(name = "required_weight")
    private Double requiredWeight;
    
    /**
     * Рассчитанный километраж поездки для текущего заказа в км
     */
    @Min(0)
    @Column(name = "request_distance")
    private Double requestDistance;
    
    /**
     * рассчитанное время поездки для текущего заказа в секундах
     */
    @Positive(message = "Ride time must be greater than 0")
    @Column(name = "request_time")
    private Integer requestTime;
    
    /**
     * Рассчитанная цена поездки по тарифу в копейках
     */
    @Min(0)
    @Column(name = "request_price")
    private Long requestPrice;
    
    /**
     * Рассчитанная цена поездки по тарифу в копейках
     */
    @Transient
    private Long requestFullPrice;
    
    /**
     * рассчитанный коэффициент части оплаты поездки для каждого заказа поездки
     */
    @Positive(message = "Part must be greater than 0")
    @Column(name = "cost_share_part")
    private Double costSharePart;

    /**
     * экономия в копейках для текущего заказа в копейках
     */
    @Column(name = "savings_cash")
    private Long savingsCash;
    
    /**
     * экономия в процентах для текущего заказа
     */
    @Column(name = "savings_procents")
    private Double savingsProcents;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    /**
     * Порядок остановок
     */
    @Column(name = "ordering_index")
    private Integer orderingIndex;
    
    /**
     * Заявка активна
     */
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    /**
     * Заявка активна
     */
    @Column(name = "cargo_express")
    @Builder.Default
    private Boolean cargoExpress = Boolean.FALSE;
    
    /**
     * Заявка-кандидат.
     */
    @Transient
    @Builder.Default
    private boolean candidate = false;
}
