package ru.sber.transport.notifications.database.model.request;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.constants.external.taxi.InboundTaxiTripStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "notifications_request", name = "taxi_trip")
@Builder
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Setter
@ToString
public class TaxiTrip {
    
    /**
     * Идентификатор поездки
     */
    @Id
    private UUID id;
    
    /**
     * Тип поездки
     */
    @Column(name = "trip_type")
    @Enumerated(EnumType.STRING)
    private TripType tripType;
    
    /**
     * ID в системе Исполнителя
     */
    @Column(name = "taxi_id")
    private String taxiId;
    
    /**
     * Идентификатор организации
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Идентификатор тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
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
     * статус из системы исполнителя
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InboundTaxiTripStatus status;
    
    /**
     * Километраж
     */
    @Column(name = "trip_fact_distance")
    private Double tripFactDistance;
    
    /**
     * Длительность поездки
     */
    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "trip_fact_duration")
    private Duration tripFactDuration;
    
    /**
     * Стоимость заявки
     */
    @Column(name = "trip_fact_price")
    private Integer tripFactPrice;
    
    /**
     * Время простоя ТС
     */
    @JdbcTypeCode(SqlTypes.BIGINT)
    @Column(name = "trip_fact_wait_time")
    private Duration tripFactWaitTime;
    
    /**
     * ID связанной заявки
     */
    @Column(name = "request_id")
    private UUID requestId;
    
    /**
     * ID связанной совместной поездки
     */
    @Column(name = "shared_ride_id")
    private UUID sharedRideId;
    
    /**
     * Произвольное описание субъектов работ . Например: [#ЗАКАЗА], [АВТО МАРКА], [АВТО ЦВЕТ], [АВТО РЕГ НОМЕР], [ФИО
     * ВОДИТЕЛЯ], [КОНТАКТНЫЙ ТЕЛЕФОН]
     */
    @Column
    private String resolution;

    @Type(JsonBinaryType.class)
    @Column
    private Driver driver;

    @Type(JsonBinaryType.class)
    @Column
    private Vehicle vehicle;
}
