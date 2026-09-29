package ru.sberbank.ditsib.transport.request.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.CalculateDTO;
import ru.sberbank.ditsib.transport.request.dto.RouteSegmentDTO;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Базовая сущность описывающая заявку
 */
@NamedEntityGraph(
        name = "request",
        attributeNodes = {
                @NamedAttributeNode(value = "author", subgraph = "employee"),
                @NamedAttributeNode(value = "approvedBy", subgraph = "employee"),
                @NamedAttributeNode(value = "passenger", subgraph = "employee"),
                @NamedAttributeNode(value = "waypoints", subgraph = "waypointGraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "employee",
                        attributeNodes = {
                                @NamedAttributeNode("department")
                        }
                ),
                @NamedSubgraph(
                        name = "waypointGraph",
                        attributeNodes = {
                                @NamedAttributeNode("address")
                        }
                )
        }
)
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EqualsAndHashCode(of = "id")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Audited
@SQLRestriction("active=true")
public abstract class Request implements HasHumanReadableId {
    
    /**
     * Идентификатор заявки
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Человекочитаемый идентификатор
     */
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    /**
     * Создатель заявки
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "authorId", nullable = false)
    @NotAudited
    private Employee author;
    
    /**
     * Пассажир
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "passengerId", nullable = false)
    @NotAudited
    private Employee passenger;
    
    /**
     * Дата и время создания заявки
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    /**
     * Временная зона
     */
    @Column(name = "time_zone")
    private String timeZone;
    
    /**
     * Тип транспорта запроса
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false, updatable = false)
    private TransportTypeEnum transportType;

    /**
     * Список точек маршрута
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "request", fetch = FetchType.EAGER)
    @OrderColumn(name = "ordering_index")
    @Builder.Default
    @NotAudited
    private List<Waypoint> waypoints = new ArrayList<>();
    
    /**
     * Согласующий
     */
    @ManyToOne
    @JoinColumn(name = "approvedById")
    @NotAudited
    private Employee approvedBy;
    
    /**
     * Дата и время согласования заявки
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    /**
     * Список точек маршрута
     */
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "segments")
    private List<RouteSegmentDTO> segmentsJSON = new ArrayList<>();
    
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
     * Тариф.
     */
    @Type(JsonBinaryType.class)
    @Column
    @NotAudited
    private BaseTariff tariff;
    
    /**
     * Расходный тариф.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "outcome_tariff")
    @NotAudited
    private BaseTariff outcomeTariff;
    
    /**
     * Статус заявки
     */
    @NotNull
    @Column(name = "request_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TripRequestStatus status;
    
    /**
     * Код статуса
     */
    @Builder.Default
    @Column(name = "status_code")
    private Integer statusCode = 0;
    
    /**
     * Статус согласования
     */
    @Column(name = "approval_state")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalState approvalState = ApprovalState.AWAITING_APPROVAL;
    
    /**
     * Предварительные расчетные данные по поездке
     */
    @Embedded
    private ExpectedData expected;
    
    /**
     * Цель поездки
     */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_purpose")
    @NotAudited
    private TripPurpose purpose;
    
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "request_options")
    private Set<RequestOptions> requestOptions = new HashSet<>();
    
    /**
     * Планируемая дата и время поездки.
     */
    @Column(name = "desired_date", nullable = false)
    private LocalDateTime desiredDate;
    
    @Transient
    private Set<Employee> passengersFromCoop;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(name = "employee_device_time_zone")
    private String employeeDeviceTimeZone;
    
    /**
     * максимальная дата согласования заявки, без нарушения контрольного срока рассчитывается так: для ЛТ/ОТ/Каршеринг = creation_time +8 часов для
     * Такси - для заявок созданных менее чем за 2 часа до поездки = creation_time +15 для срочных, для заявок созданных за 2 часа и более до поездки
     * = desired_date -1 час
     */
    @Column(name = "approval_deadline")
    private LocalDateTime approvalDeadline;
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "approval_deadline_state")
    private DeadlineState approvalDeadlineState = DeadlineState.NONE;
    
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "joined_passenger_ids")
    private Set<UUID> joinedPassengerIds = new HashSet<>();
    
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RequestSourceEnum source = RequestSourceEnum.UNDEFINED;
    
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "min_tariff_taxi")
    private CalculateDTO minTariffTaxi;
    
    @Column
    private String commentForPurpose;

    @Column(name = "executor_group_id")
    private UUID executorGroupId;

    @Column(name = "executor_group_name")
    private String executorGroupName;

    @OneToMany(mappedBy = "request")
    @NotAudited
    @Builder.Default
    private List<FraudData> fraudData = new ArrayList<>();
    
    @NotAudited
    @NotNull
    @Column
    @Builder.Default
    private boolean active = true;
}
