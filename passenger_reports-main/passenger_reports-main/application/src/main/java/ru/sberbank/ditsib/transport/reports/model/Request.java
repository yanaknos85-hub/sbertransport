package ru.sberbank.ditsib.transport.reports.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.reports.model.driversData.Autopark;
import ru.sberbank.ditsib.transport.reports.model.driversData.Driver;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.mapping.CarsharingResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.mapping.GroupTransferResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.mapping.PersonalResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.mapping.PublicResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.mapping.TaxiResponseSqlResultSetMapping;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Модель поездки, совмещенная сущность заявки и поездки
 */
@Entity
@Table(schema = "reports", name = "request")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name = "PersonalResponseSqlResultSetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = PersonalResponseSqlResultSetMapping.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "author_id", type = UUID.class),
                                @ColumnResult(name = "passenger_id", type = UUID.class),
                                @ColumnResult(name = "personal_car", type = UUID.class),
                                @ColumnResult(name = "creation_time", type = LocalDateTime.class),
                                @ColumnResult(name = "desired_date", type = LocalDateTime.class),
                                @ColumnResult(name = "approval_date", type = LocalDateTime.class),
                                @ColumnResult(name = "order_payment_formation_start_date", type = LocalDateTime.class),
                                @ColumnResult(name = "transport_type"),
                                @ColumnResult(name = "request_status"),
                                @ColumnResult(name = "request_status_code"),
                                @ColumnResult(name = "purpose_id", type = UUID.class),
                                @ColumnResult(name = "coop_trip"),
                                @ColumnResult(name = "shared_ride_id", type = UUID.class),
                                @ColumnResult(name = "passenger_count"),
                                @ColumnResult(name = "expected_cost"),
                                @ColumnResult(name = "expected_distance"),
                                @ColumnResult(name = "expected_time", type = Duration.class),
                                @ColumnResult(name = "payment_price_insurance", type = Long.class),
                                @ColumnResult(name = "payment_price_main", type = Long.class),
                                @ColumnResult(name = "payment_price_optional", type = Long.class),
                                @ColumnResult(name = "payment_type_code_insurance"),
                                @ColumnResult(name = "payment_type_code_main"),
                                @ColumnResult(name = "payment_type_code_optional"),
                                @ColumnResult(name = "rating_mark"),
                                @ColumnResult(name = "rating_comment"),
                                @ColumnResult(name = "change_date", type = LocalDateTime.class),
                                @ColumnResult(name = "time_zone"),
                                @ColumnResult(name = "cost_share_part"),
                                @ColumnResult(name = "savings_cash", type = Long.class),
                                @ColumnResult(name = "savings_procents", type = Long.class),
                                @ColumnResult(name = "shared_ride_owner"),
                                @ColumnResult(name = "number_passengers_joined"),
                                @ColumnResult(name = "additional_sum", type = Long.class),
                                @ColumnResult(name = "tariff_id", type = UUID.class),
                                @ColumnResult(name = "employee_driver_id", type = UUID.class),
                                @ColumnResult(name = "passenger_department1"),
                                @ColumnResult(name = "passenger_department2"),
                                @ColumnResult(name = "passenger_department3"),
                                @ColumnResult(name = "passenger_department4"),
                                @ColumnResult(name = "passenger_department5"),
                                @ColumnResult(name = "passenger_department6"),
                                @ColumnResult(name = "departure_address"),
                                @ColumnResult(name = "intermediate_addresses"),
                                @ColumnResult(name = "destination_address"),
                                @ColumnResult(name = "cost_center"),
                                @ColumnResult(name = "joined_passengers"),
                                @ColumnResult(name = "source"),
                                @ColumnResult(name = "min_taxi_tariff_cost", type = Long.class),
                                @ColumnResult(name = "comment_for_purpose"),
                                @ColumnResult(name = "deadline_state"),
                                @ColumnResult(name = "request_closed_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "executor_group_id", type = UUID.class),
                                @ColumnResult(name = "executor_group_name"),
                                @ColumnResult(name = "trip_start_time", type = LocalDateTime.class),
                                @ColumnResult(name = "deadline", type = LocalDateTime.class),
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "PublicResponseSqlResultSetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = PublicResponseSqlResultSetMapping.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "author_id", type = UUID.class),
                                @ColumnResult(name = "passenger_id", type = UUID.class),
                                @ColumnResult(name = "creation_time", type = LocalDateTime.class),
                                @ColumnResult(name = "desired_date", type = LocalDateTime.class),
                                @ColumnResult(name = "approval_date", type = LocalDateTime.class),
                                @ColumnResult(name = "order_payment_formation_start_date", type = LocalDateTime.class),
                                @ColumnResult(name = "transport_type"),
                                @ColumnResult(name = "request_status"),
                                @ColumnResult(name = "request_status_code"),
                                @ColumnResult(name = "purpose_id", type = UUID.class),
                                @ColumnResult(name = "expected_cost"),
                                @ColumnResult(name = "expected_distance"),
                                @ColumnResult(name = "expected_time", type = Duration.class),
                                @ColumnResult(name = "payment_price_insurance", type = Long.class),
                                @ColumnResult(name = "payment_price_main", type = Long.class),
                                @ColumnResult(name = "payment_price_optional", type = Long.class),
                                @ColumnResult(name = "payment_type_code_insurance"),
                                @ColumnResult(name = "payment_type_code_main"),
                                @ColumnResult(name = "payment_type_code_optional"),
                                @ColumnResult(name = "rating_mark"),
                                @ColumnResult(name = "rating_comment"),
                                @ColumnResult(name = "change_date", type = LocalDateTime.class),
                                @ColumnResult(name = "time_zone"),
                                @ColumnResult(name = "tariff_id", type = UUID.class),
                                @ColumnResult(name = "passenger_department1"),
                                @ColumnResult(name = "passenger_department2"),
                                @ColumnResult(name = "passenger_department3"),
                                @ColumnResult(name = "passenger_department4"),
                                @ColumnResult(name = "passenger_department5"),
                                @ColumnResult(name = "passenger_department6"),
                                @ColumnResult(name = "departure_address"),
                                @ColumnResult(name = "intermediate_addresses"),
                                @ColumnResult(name = "destination_address"),
                                @ColumnResult(name = "public_compensation_document_exist"),
                                @ColumnResult(name = "cost_center"),
                                @ColumnResult(name = "source"),
                                @ColumnResult(name = "min_taxi_tariff_cost", type = Long.class),
                                @ColumnResult(name = "comment_for_purpose"),
                                @ColumnResult(name = "deadline_state"),
                                @ColumnResult(name = "deadline", type = LocalDateTime.class),
                                @ColumnResult(name = "savings_cash", type = Long.class),
                                @ColumnResult(name = "request_closed_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "executor_group_id", type = UUID.class),
                                @ColumnResult(name = "executor_group_name")
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "TaxiResponseSqlResultSetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = TaxiResponseSqlResultSetMapping.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "author_id", type = UUID.class),
                                @ColumnResult(name = "passenger_id", type = UUID.class),
                                @ColumnResult(name = "personal_car", type = UUID.class),
                                @ColumnResult(name = "creation_time", type = LocalDateTime.class),
                                @ColumnResult(name = "desired_date", type = LocalDateTime.class),
                                @ColumnResult(name = "approval_date", type = LocalDateTime.class),
                                @ColumnResult(name = "transport_type"),
                                @ColumnResult(name = "request_status"),
                                @ColumnResult(name = "request_status_code"),
                                @ColumnResult(name = "purpose_id", type = UUID.class),
                                @ColumnResult(name = "coop_trip"),
                                @ColumnResult(name = "ride_id", type = UUID.class),
                                @ColumnResult(name = "passenger_count"),
                                @ColumnResult(name = "expected_cost"),
                                @ColumnResult(name = "expected_distance"),
                                @ColumnResult(name = "expected_time", type = Duration.class),
                                @ColumnResult(name = "rating_mark"),
                                @ColumnResult(name = "rating_comment"),
                                @ColumnResult(name = "change_date", type = LocalDateTime.class),
                                @ColumnResult(name = "time_zone"),
                                @ColumnResult(name = "cost_share_part"),
                                @ColumnResult(name = "savings_cash", type = Long.class),
                                @ColumnResult(name = "savings_procents", type = Long.class),
                                @ColumnResult(name = "shared_ride_owner"),
                                @ColumnResult(name = "number_passengers_joined"),
                                @ColumnResult(name = "tariff_id", type = UUID.class),
                                @ColumnResult(name = "trip_class"),
                                @ColumnResult(name = "approval_state"),
                                @ColumnResult(name = "approved_by_id", type = UUID.class),
                                @ColumnResult(name = "contractor_id", type = UUID.class),
                                @ColumnResult(name = "comment_for_driver"),
                                @ColumnResult(name = "driver"),
                                @ColumnResult(name = "vehicle"),
                                @ColumnResult(name = "finished_time", type = LocalDateTime.class),
                                @ColumnResult(name = "passenger_department1"),
                                @ColumnResult(name = "passenger_department2"),
                                @ColumnResult(name = "passenger_department3"),
                                @ColumnResult(name = "passenger_department4"),
                                @ColumnResult(name = "passenger_department5"),
                                @ColumnResult(name = "passenger_department6"),
                                @ColumnResult(name = "departure_address"),
                                @ColumnResult(name = "intermediate_addresses"),
                                @ColumnResult(name = "destination_address"),
                                @ColumnResult(name = "resolution"),
                                @ColumnResult(name = "organization_id", type = UUID.class),
                                @ColumnResult(name = "limit_id", type = UUID.class),
                                @ColumnResult(name = "deadline", type = LocalDateTime.class),
                                @ColumnResult(name = "deadline_state"),
                                @ColumnResult(name = "driver_arrived_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "cost_center"),
                                @ColumnResult(name = "request_closed_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "joined_passengers"),
                                @ColumnResult(name = "source"),
                                @ColumnResult(name = "min_taxi_tariff_cost", type = Long.class),
                                @ColumnResult(name = "comment_for_purpose"),
                                @ColumnResult(name = "contract_number"),
                                @ColumnResult(name = "executor_group_id", type = UUID.class),
                                @ColumnResult(name = "executor_group_name")
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "CarsharingResponseSqlResultSetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = CarsharingResponseSqlResultSetMapping.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "cost_center"),
                                @ColumnResult(name = "passenger_id", type = UUID.class),
                                @ColumnResult(name = "creation_time", type = LocalDateTime.class),
                                @ColumnResult(name = "desired_date", type = LocalDateTime.class),
                                @ColumnResult(name = "approval_date", type = LocalDateTime.class),
                                @ColumnResult(name = "finished_time", type = LocalDateTime.class),
                                @ColumnResult(name = "transport_type"),
                                @ColumnResult(name = "request_status"),
                                @ColumnResult(name = "request_status_code"),
                                @ColumnResult(name = "purpose_id", type = UUID.class),
                                @ColumnResult(name = "expected_cost"),
                                @ColumnResult(name = "expected_distance"),
                                @ColumnResult(name = "expected_time", type = Duration.class),
                                @ColumnResult(name = "rating_mark"),
                                @ColumnResult(name = "rating_comment"),
                                @ColumnResult(name = "change_date", type = LocalDateTime.class),
                                @ColumnResult(name = "time_zone"),
                                @ColumnResult(name = "tariff_id", type = UUID.class),
                                @ColumnResult(name = "contractor_id", type = UUID.class),
                                @ColumnResult(name = "passenger_department1"),
                                @ColumnResult(name = "passenger_department2"),
                                @ColumnResult(name = "passenger_department3"),
                                @ColumnResult(name = "passenger_department4"),
                                @ColumnResult(name = "passenger_department5"),
                                @ColumnResult(name = "passenger_department6"),
                                @ColumnResult(name = "departure_address"),
                                @ColumnResult(name = "intermediate_addresses"),
                                @ColumnResult(name = "destination_address"),
                                @ColumnResult(name = "rent_id"),
                                @ColumnResult(name = "phone_number"),
                                @ColumnResult(name = "coop_trip"),
                                @ColumnResult(name = "passenger_count"),
                                @ColumnResult(name = "joined_passengers"),
                                @ColumnResult(name = "source"),
                                @ColumnResult(name = "min_taxi_tariff_cost", type = Long.class),
                                @ColumnResult(name = "comment_for_purpose"),
                                @ColumnResult(name = "deadline_state"),
                                @ColumnResult(name = "savings_cash", type = Long.class),
                                @ColumnResult(name = "contract_number"),
                                @ColumnResult(name = "request_closed_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "executor_group_id", type = UUID.class),
                                @ColumnResult(name = "executor_group_name")
                        }
                )
        }
)
@SqlResultSetMapping(
        name = "GroupTransferResponseSqlResultSetMapping",
        classes = {
                @ConstructorResult(
                        targetClass = GroupTransferResponseSqlResultSetMapping.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "author_id", type = UUID.class),
                                @ColumnResult(name = "passenger_id", type = UUID.class),
                                @ColumnResult(name = "personal_car", type = UUID.class),
                                @ColumnResult(name = "creation_time", type = LocalDateTime.class),
                                @ColumnResult(name = "desired_date", type = LocalDateTime.class),
                                @ColumnResult(name = "approval_date", type = LocalDateTime.class),
                                @ColumnResult(name = "transport_type"),
                                @ColumnResult(name = "request_status"),
                                @ColumnResult(name = "request_status_code"),
                                @ColumnResult(name = "purpose_id", type = UUID.class),
                                @ColumnResult(name = "passenger_count"),
                                @ColumnResult(name = "expected_cost"),
                                @ColumnResult(name = "expected_distance"),
                                @ColumnResult(name = "expected_time", type = Duration.class),
                                @ColumnResult(name = "rating_mark"),
                                @ColumnResult(name = "rating_comment"),
                                @ColumnResult(name = "change_date", type = LocalDateTime.class),
                                @ColumnResult(name = "time_zone"),
                                @ColumnResult(name = "tariff_id", type = UUID.class),
                                @ColumnResult(name = "trip_class"),
                                @ColumnResult(name = "approval_state"),
                                @ColumnResult(name = "approved_by_id", type = UUID.class),
                                @ColumnResult(name = "contractor_id", type = UUID.class),
                                @ColumnResult(name = "comment_for_driver"),
                                @ColumnResult(name = "finished_time", type = LocalDateTime.class),
                                @ColumnResult(name = "driver"),
                                @ColumnResult(name = "vehicle"),
                                @ColumnResult(name = "passenger_department1"),
                                @ColumnResult(name = "passenger_department2"),
                                @ColumnResult(name = "passenger_department3"),
                                @ColumnResult(name = "passenger_department4"),
                                @ColumnResult(name = "passenger_department5"),
                                @ColumnResult(name = "passenger_department6"),
                                @ColumnResult(name = "departure_address"),
                                @ColumnResult(name = "intermediate_addresses"),
                                @ColumnResult(name = "destination_address"),
                                @ColumnResult(name = "resolution"),
                                @ColumnResult(name = "organization_id", type = UUID.class),
                                @ColumnResult(name = "limit_id", type = UUID.class),
                                @ColumnResult(name = "deadline", type = LocalDateTime.class),
                                @ColumnResult(name = "deadline_state"),
                                @ColumnResult(name = "driver_arrived_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "cost_center"),
                                @ColumnResult(name = "request_closed_datetime", type = LocalDateTime.class),
                                @ColumnResult(name = "vip"),
                                @ColumnResult(name = "source"),
                                @ColumnResult(name = "comment_for_purpose"),
                                @ColumnResult(name = "savings_cash", type = Long.class),
                                @ColumnResult(name = "min_taxi_tariff_cost", type = Long.class),
                                @ColumnResult(name = "contract_number"),
                                @ColumnResult(name = "executor_group_id", type = UUID.class),
                                @ColumnResult(name = "executor_group_name")
                        }
                )
        }
)
public class Request {

    /**
     * Список точек маршрута
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "request")
    @OrderColumn(name = "ordering_index")
    @Builder.Default
    private final List<Waypoint> waypoints = new ArrayList<>();
    /**
     * Список компенсаций транспорта
     */
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private final List<TransportCompensation> transportCompensation = new ArrayList<>();
    /**
     * Id of request
     */
    @NotNull
    @Id
    private UUID id;
    @Column(name = "change_date")
    private LocalDateTime changeDate;
    /**
     * Human readable id
     */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    /**
     * Creator of request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Employee author;
    /**
     * Passenger
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id")
    private Employee passenger;
    /**
     * Driver for a personal car
     */
    @Column(name = "employee_driver_id")
    private UUID employeeDriverId;
    /**
     * Passenger
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatcher_id")
    private Employee dispatcher;
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    /**
     * Временная зона
     */
    @Column(name = "time_zone")
    private String timeZone;
    /**
     * Type of transport used for request
     */
    @Column(name = "transport_type")
    private String transportType;
    /**
     * Фактическая информация по индивидуальной поездке
     */
    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    private SingleTaxiTrip singleTaxiTrip;
    /**
     * Согласующий
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    /**
     * Дата и время согласования заявки
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    /**
     * Дата и время окончания формирования приказа на выплату
     */
    @Column(name = "order_payment_formation_finishing_date")
    private LocalDateTime orderPaymentFormationFinishingDate;

    /**
     * Дата и время начала формирования приказа на выплату
     */
    @Column(name = "order_payment_formation_start_date")
    private LocalDateTime orderPaymentFormationStartDate;

    /**
     * Идентификатор тарифа
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id")
    private BaseTariff tariff;

    /**
     * Статус заявки
     */
    @Column(name = "request_status")
    private String status;

    /**
     * Код статуса
     */
    @Column(name = "request_status_code")
    private Integer statusCode;

    /**
     * Статус согласования
     */
    @Column(name = "approval_state")
    @Builder.Default
    private String approvalState = ApprovalState.AWAITING_APPROVAL.name();

    /**
     * Предварительные расчетные данные по поездке
     */
    @Embedded
    private ExpectedData expected;

    /**
     * Цель поездки
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purpose_id")
    private TripPurpose purpose;

    /**
     * Ожидаемые дата и время поездки.
     */
    @Column(name = "desired_date")
    private LocalDateTime desiredDate;

    /**
     * Флаг корпоративной/индивидумальной поездки
     */
    @Column(name = "coop_trip")
    private boolean coopTrip;

    /**
     * Количество пассажиров
     */
    @Column(name = "passenger_count")
    @Builder.Default
    private int passengerCount = 1;

    /**
     * Комментарий
     */
    @Column(name = "comment_for_driver")
    private String commentForDriver;

    /**
     * Дата и время завершения поездки
     */
    @Column(name = "finished_time")
    private LocalDateTime finishedTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_ride_id")
    private SharedRide sharedRide;

    @Column
    private UUID rideId;

    /**
     * Класс транспорта
     */
    @Column(name = "trip_class")
    private String transportClass;

    /**
     * Geo and cost data
     */
    @Builder.Default
    @Embedded
    private RequestRating requestRating = new RequestRating();

    /**
     * Данные по оплате
     */
    @Builder.Default
    @Embedded
    private PaymentData paymentData = new PaymentData();

    /**
     * Класс каршеринга
     */
    @Column(name = "carsharing_class")
    private String carsharingClass;

    /**
     * Каршеринговая компания / Контрагент для такси
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_car")
    private PersonalCar personalCar;

    @Column(name = "public_compensation_document_exist")
    private Boolean publicCompensationDocumentExist;

    /**
     * Лимит
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "limit_id")
    private Limit limit;

    @Transient
    private Set<Employee> passengers;

    @Transient
    private Map<Integer, Department> departmentHierarchy;

    /**
     * Контрольный срок
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;

    /**
     * Экономия в рублях для текущего заказа
     */
    @Transient
    private Double kpiSavings;

    /**
     * Водитель
     */
    @Type(JsonBinaryType.class)
    @Column(name = "driver")
    private Driver driver;

    /**
     * Автомобиль
     */
    @Type(JsonBinaryType.class)
    @Column(name = "vehicle")
    private Vehicle vehicle;

    /**
     * Автопарк
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autopark_id")
    private Autopark autopark;

    /**
     * Решение.
     */
    @Column(name = "resolution")
    private String resolution;

    @Column(name = "deadline_state")
    @Enumerated(EnumType.STRING)
    private DeadlineState deadlineState;

    @Column(name = "is_sla_expired")
    private Boolean slaExpired;

    @Column(name = "organization_id")
    private UUID organizationId;

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
     * Инициатор поездки.
     */
    @Column(name = "shared_ride_owner")
    private Boolean sharedRideOwner;

    /**
     * Сумма доплаты за всех пассажиров в копейках
     */
    @Column(name = "additional_sum")
    private Long additionalSum;

    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    @Column(name = "number_passengers_joined")
    private Integer numberPassengersJoined;

    /**
     * БЕ (Балансовая единица)
     */
    @Column(name = "balance_unit")
    private Integer balanceUnit;

    /**
     * МВЗ (Место возникновения затрат)
     */
    @Column(name = "cost_center")
    private String costCenter;

    /**
     * Подразделение 1 уровня
     */
    @Column(name = "passenger_department1")
    private String passengerDepartment1;

    /**
     * Подразделение 2 уровня
     */
    @Column(name = "passenger_department2")
    private String passengerDepartment2;

    /**
     * Подразделение 3 уровня
     */
    @Column(name = "passenger_department3")
    private String passengerDepartment3;

    /**
     * Подразделение 4 уровня
     */
    @Column(name = "passenger_department4")
    private String passengerDepartment4;

    /**
     * Подразделение 5 уровня
     */
    @Column(name = "passenger_department5")
    private String passengerDepartment5;

    /**
     * Подразделение 6 уровня
     */
    @Column(name = "passenger_department6")
    private String passengerDepartment6;

    /**
     * Адрес отправления
     */
    @Column(name = "departure_address")
    private String departureAddress;

    /**
     * Промежуточные адреса
     */
    @Column(name = "intermediate_addresses")
    private String intermediateAddresses;

    /**
     * Адрес назначения
     */
    @Column(name = "destination_address")
    private String destinationAddress;

    /**
     * Время перехода заявки в статус Водитель ожидает в точке отправления TAXI_DRIVER_ARRIVED
     */
    @Column(name = "driver_arrived_datetime")
    private LocalDateTime driverArrivedDatetime;

    /**
     * Номер аренды (carsharing)
     */
    @Column(name = "rent_id")
    private Integer rentId;

    /**
     * Номер телефона, который использовался для создания заявки на каршеринг
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Дата закрытия обращения АС (в нашей системе)
     */
    @Column(name = "request_closed_datetime")
    private LocalDateTime requestClosedDatetime;

    /**
     * Признак vip
     */
    @Column
    private Boolean vip;

    /**
     * Источник создания заявки
     */
    @Column
    private String source;

    /**
     * Присоединённые пассажиры в формате Федулов Олег Федорович (7676878), Федулов Олег Федорович (7676878)
     */
    @Column(nullable = false)
    @Builder.Default
    private String joinedPassengers = "";

    @Column
    private Long minTaxiTariffCost;

    @Column
    private String commentForPurpose;

    @Column(name = "executor_group_id")
    private UUID executorGroupId;

    @Column(name = "executor_group_name")
    private String executorGroupName;

    /**
     * Дата и время начала поездки
     */
    @Column(name = "trip_start_time")
    private LocalDateTime tripStartTime;

    public static Integer getPeriodOfPayment(LocalDateTime orderPaymentFormationStartDate) {
        if (orderPaymentFormationStartDate != null) {
            int dayOfMonth = orderPaymentFormationStartDate.getDayOfMonth();

            if (dayOfMonth >= 1 && dayOfMonth <= 7) {
                return 1;
            } else if (dayOfMonth >= 8 && dayOfMonth <= 15) {
                return 2;
            } else if (dayOfMonth >= 16 && dayOfMonth <= 23) {
                return 3;
            } else {
                return 4;
            }
        }
        return null;
    }

    @PrePersist
    private void addLinks() {
        if (getWaypoints() != null) {
            getWaypoints().forEach(wp -> wp.setRequest(this));
        }
        if (getTransportCompensation() != null) {
            getTransportCompensation().forEach(tc -> tc.setRequest(this));
        }
    }

    public Integer getPeriodOfPayment() {
        return Request.getPeriodOfPayment(getOrderPaymentFormationStartDate());
    }
}
