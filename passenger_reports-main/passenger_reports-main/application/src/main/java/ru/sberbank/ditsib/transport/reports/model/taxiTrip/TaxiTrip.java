package ru.sberbank.ditsib.transport.reports.model.taxiTrip;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.reports.dto.taxi.TaxiTripResultSet;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность поездки на такси
 */
@Entity
@Table(schema = "reports", name = "taxi_trip")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "trip_type", discriminatorType = DiscriminatorType.STRING)
@DynamicUpdate
@DynamicInsert

@SqlResultSetMapping(
        name = "TaxiTripResultSet",
        classes = {
                @ConstructorResult(
                        targetClass = TaxiTripResultSet.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "human_readable_id"),
                                @ColumnResult(name = "trip_fact_wait_time", type = Duration.class),
                                @ColumnResult(name = "trip_fact_price"),
                                @ColumnResult(name = "trip_fact_distance"),
                                @ColumnResult(name = "trip_fact_duration", type = Duration.class),
                                @ColumnResult(name = "trip_start_time", type = LocalDateTime.class),
                                @ColumnResult(name = "fact_parameters_setting_time", type = LocalDateTime.class),
                                @ColumnResult(name = "ride_id", type = UUID.class),
                                @ColumnResult(name = "request_id", type = UUID.class),
                                @ColumnResult(name = "registry_fact_waiting_time"),
                                @ColumnResult(name = "registry_human_readable_id"),
                                @ColumnResult(name = "registry_fact_cost"),
                                @ColumnResult(name = "registry_fact_distance"),
                                @ColumnResult(name = "registry_fact_payment")
                        }
                )
        }
)

public class TaxiTrip {
    
    @Column(name = "trip_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TripType tripType;
    
    /**
     * Идентификатор поездки
     */
    @Id
    private UUID id;
    
    /**
     * ID в системе Исполнителя
     */
    @Column(name = "taxi_id")
    private String taxiId;
    
    /**
     * Человекочитаемый ID поездки
     */
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    /**
     * Идентификатор организации
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Идентификатор тарифа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id")
    private TaxiTariff tariff;
    
    /**
     * Дата и время назначения водителя на заявку
     */
    @Column(name = "trip_assignment_date_time")
    private LocalDateTime tripAssignmentDateTime;
    
    /**
     * Время регистрации в системе Исполнителя
     */
    @Column(name = "date_time_registered")
    private LocalDateTime dateTimeRegistered;
    
    /**
     * Время начала работ
     */
    @Column(name = "trip_start_time")
    private LocalDateTime tripStartTime;
    
    /**
     * Время завершения поездки
     */
    @Column(name = "trip_finish_time")
    private LocalDateTime tripFinishTime;
    
    /**
     * Дата внесения фактических параметров поездки
     */
    @Column(name = "fact_parameters_setting_time")
    private LocalDateTime factParametersSettingTime;
    
    /**
     * статус из системы исполнителя
     */
    @Column(name = "status")
    private String status;
    
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
     * Время ожидания в промежуточных точках в Waypoint
     */
    @Column(name = "intermediate_wait_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration intermediateWaitTime;
    
    /**
     * Время получения последнего сообщения из интеграции
     */
    @Column(name = "last_xml_received_date_time")
    private LocalDateTime lastXmlReceivedDateTime;
    
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
    
    /**
     * Назначеная машина
     */
    @Embedded
    private CarInfo assignedCar;
    
}
