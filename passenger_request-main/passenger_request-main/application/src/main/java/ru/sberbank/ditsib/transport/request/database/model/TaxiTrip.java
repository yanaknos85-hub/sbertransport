package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import ru.sberbank.ditsib.transport.constants.TaxiTripDecisionCode;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность поездки на такси
 */
@SQLRestriction("active=true")
@SQLDelete(sql = "UPDATE taxi_trip SET active = false WHERE id = ?")
@Entity
@Table(schema = "request", name = "taxi_trip")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "trip_type", discriminatorType = DiscriminatorType.STRING)
@ToString(exclude = { "requests", "waypointsInfo" })
@EqualsAndHashCode(of = "id")
public class TaxiTrip {

    @Column(name = "trip_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TripType tripType;
    
    /**
     * Идентификатор поездки
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "human_readable_id")
    @NotBlank
    private String humanReadableId;
    
    /**
     * ID в системе Исполнителя
     */
    @Column(name = "taxi_id")
    private String taxiId;
    
    /**
     * Идентификатор организации
     */
    @NonNull
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Заявки в поездке
     */
    @OneToMany(mappedBy = "taxiTrip")
    private List<RequestForTaxi> requests;
    
    /**
     * Произвольное описание работ . Например: [#ЗАКАЗА], [АВТО МАРКА], [АВТО ЦВЕТ], [АВТО РЕГ НОМЕР], [ФИО ВОДИТЕЛЯ],
     * [КОНТАКТНЫЙ ТЕЛЕФОН]
     */
    @Column(name = "resolution")
    private String resolution;
    
    /**
     * Комментарий исполнителя
     */
    @Column(name = "contractor_comment")
    private String contractorComment;
    
    /**
     * Идентификатор тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Идентификатор расходного тарифа
     */
    @Column(name = "outcome_tariff_id")
    private UUID outcomeTariffId;
    
    /**
     * Дата внесения фактических параметров поездки
     */
    @Column(name = "fact_parameters_setting_time")
    private LocalDateTime factParametersSettingTime;
    
    /**
     * Время регистрации в системе Исполнителя
     */
    @Column(name = "date_time_registered")
    private LocalDateTime dateTimeRegistered;
    
    @Column(name = "time_work_start")
    private LocalDateTime timeWorkStart;
    
    @Column(name = "time_work_finish")
    private LocalDateTime timeWorkFinish;
    
    /**
     * Время начала поездки
     */
    @Column(name = "trip_start_time")
    private LocalDateTime tripStartTime;
    
    /**
     * Время завершения поездки
     */
    @Column(name = "trip_finish_time")
    private LocalDateTime tripFinishTime;
    
    /**
     * Текущий статус заказа из системы исполнителя
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InboundTaxiTripStatus status;
    
    /**
     * Код закрытия
     */
    @Enumerated(value = EnumType.STRING)
    @Column(name = "decision_code")
    private TaxiTripDecisionCode decisionCode;
    
    /**
     * Фактическая информация по точкам маршрута
     */
    @OrderColumn(name = "waypoint_id")
    @Builder.Default
    @OneToMany(mappedBy = "taxiTrip", cascade = CascadeType.ALL)
    private final List<RouteInfo> waypointsInfo = new ArrayList<>();
    
    /**
     * Километраж
     */
    @Column(name = "trip_fact_distance")
    private Double tripFactDistance;
    
    /**
     * Длительность поездки
     */
    @Column(name = "trip_fact_duration", columnDefinition = "int8 (Types#BIGINT)")
    private Duration tripFactDuration;
    
    /**
     * Стоимость заявки
     */
    @Column(name = "trip_fact_price")
    private Integer tripFactPrice;
    
    /**
     * Время простоя ТС
     */
    @Column(name = "trip_fact_wait_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration tripFactWaitTime;
    
    /**
     * Дата и время назначения водителя на заявку
     */
    @Column(name = "trip_assignment_date_time")
    private LocalDateTime tripAssignmentDateTime;
    
    /**
     * Время получения последнего сообщения из интеграции
     */
    @Column(name = "last_xml_received_date_time")
    private LocalDateTime lastXmlReceivedDateTime;
    
    /**
     * Водитель.
     */
    @Column
    private String driver;
    
    /**
     * Назначеная машина
     */
    @Embedded
    private CarInfo assignedCar;

    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "last_known_position_id", nullable = false)
    private RouteHistoryElement lastKnownPosition;

    @Builder.Default
    @Column(name = "active")
    private boolean active = true;
    
    /**
     * Фактическое время ожидания в минутах
     */
    @Column(name = "registry_fact_waiting_time")
    private Double registryFactWaitingTime;
    
    /**
     * Человекочитаемый идентификатор реестра
     */
    @Column(name = "registry_human_readable_id")
    private String registryHumanReadableId;
    
    /**
     * Фактическая стоимость
     */
    @Column(name = "registry_fact_cost")
    private Double registryFactCost;
    
    /**
     * Фактическое расстояние
     */
    @Column(name = "registry_fact_distance")
    private Double registryFactDistance;
    
    /**
     * Факт оплаты
     */
    @Column(name = "registry_fact_payment")
    private Boolean registryFactPayment;
    
    @PreRemove
    private void clear() {
        lastKnownPosition=null;
        for (final var request : getRequests()) {
            request.setTaxiTrip(null);
        }
    }
    
    @PrePersist
    private void prePersist() {
        if (lastKnownPosition != null) {
            lastKnownPosition.setTaxiTrip(this);
        }
    }

}
