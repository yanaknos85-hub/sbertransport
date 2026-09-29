package ru.sberbank.transport.oto.cargo.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.transport.oto.cargo.database.model.drivers_data.Driver;
import ru.sberbank.transport.oto.cargo.database.model.tariff.BaseTariff;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Модель поездки, совмещенная сущность заявки и поездки
 */
@NamedEntityGraph(
        name = "Request.requestForCargo",
        attributeNodes = {
                @NamedAttributeNode("evaluation"),
                @NamedAttributeNode("routelist"),
                @NamedAttributeNode(value = "waypoints", subgraph = "Request.requestForCargo.waypoints"),
                @NamedAttributeNode("passenger"),
                @NamedAttributeNode("author"),
                @NamedAttributeNode("sender"),
                @NamedAttributeNode("recipient"),
                @NamedAttributeNode("approvedBy"),
                @NamedAttributeNode("contractor"),
        },
        subgraphs = @NamedSubgraph(name = "Request.requestForCargo.waypoints", attributeNodes = {
                @NamedAttributeNode("address")
        })
)
@DynamicUpdate
@Entity
@Table(schema = "oto_cargo", name = "request")
@EqualsAndHashCode(of = "id")
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Request {
    
    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Evaluation evaluation;
    
    /**
     * Id of request
     */
    @NotNull
    @Id
    private UUID id;
    
    /**
     * Human readable id
     */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /**
     * Creator of request
     */
    @ManyToOne
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private Employee author;
    
    /**
     * Passenger
     */
    @ManyToOne
    @JoinColumn(name = "passenger_id")
    @ToString.Exclude
    private Employee passenger;
    
    /**
     * Passenger
     */
    @ManyToOne
    @JoinColumn(name = "dispatcher_id")
    @ToString.Exclude
    private Employee dispatcher;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    /**
     * Type of transport used for request
     */
    @Column(name = "transport_type")
    private String transportType;
    
    /**
     * Список точек маршрута
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "request", fetch = FetchType.EAGER)
    @OrderColumn(name = "ordering_index")
    @Builder.Default
    @ToString.Exclude
    private final List<Waypoint> waypoints = new ArrayList<>();

    /**
     * Employee authorized to approve request
     */
    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    @ToString.Exclude
    private Employee approvedBy;
    
    /**
     * Date and time or request approval
     */
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    /**
     * Tariff id
     */
    @Column(name = "tariff_id")
    private UUID tariff;
    
    /**
     * Status of request
     */
    @Column(name = "request_status")
    private String status;
    
    /**
     * Код статуса
     */
    @Column(name = "request_status_code")
    private Integer statusCode;
    
    /**
     * Status of approval
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
     * Desired date and time of trip.
     */
    @Column(name = "desired_date")
    private LocalDateTime desiredDate;

    /**
     * Comment
     */
    @Column(name = "comment_for_driver")
    private String commentForDriver;
    
    /**
     * Date and time or request finishing
     */
    @Column(name = "finished_time")
    private LocalDateTime finishedTime;
    
    /**
     * Id заявки в SRM
     */
    @Column(name = "ride_id")
    private UUID rideId;
    
    
    @ManyToOne
    @JoinColumn(name = "start_waypoint_id")
    @ToString.Exclude
    private Waypoint startWaypoint;
    
    @ManyToOne
    @JoinColumn(name = "end_waypoint_id")
    @ToString.Exclude
    private Waypoint endWaypoint;
    
    /**
     * Geo and cost data
     */
    @Embedded
    private RequestRating requestRating;
    
    /**
     * Данные по оплате
     */
    @Embedded
    private PaymentData paymentData;
    
    /**
     * Каршеринговая компания / Контрагент для такси
     */
    @ManyToOne
    @JoinColumn(name = "contractor_id")
    @ToString.Exclude
    private Contractor contractor;

    @Transient
    private Set<Employee> passengers;
    
    /**
     * Контрольный срок
     */
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    /**
     * Состояние контрольного срока
     */
    @Column(name = "deadline_state")
    @Builder.Default
    private DeadlineState deadlineState = DeadlineState.NONE;
    
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
    @ToString.Exclude
    private Driver driver;
    
    /**
     * Водитель
     */
    @Type(JsonBinaryType.class)
    @Column(name = "vehicle")
    @ToString.Exclude
    private Vehicle vehicle;
    
    /**
     * Отправитель заявки на грузоперевозку
     */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    private Employee sender;
    
    /**
     * Получатель заявки на грузоперевозку
     */
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    @ToString.Exclude
    private Employee recipient;
    
    /**
     * Date and time of cargo transfer ending
     */
    @Column(name = "transfer_time")
    private LocalDateTime transferTime;
    
    /**
     * Date and time of cargo shipment ending
     */
    @Column(name = "shipment_time")
    private LocalDateTime shipmentTime;
    
    /**
     * Телефон отправителя
     */
    @Column(name = "sender_phone")
    private String senderPhone;
    
    /**
     * Имя отправителя
     */
    @Column(name = "sender_name")
    private String senderName;
    
    /**
     * Имя получателя
     */
    @Column(name = "recipient_name")
    private String recipientName;
    
    /**
     * Телефон получателя
     */
    @Column(name = "recipient_phone")
    private String recipientPhone;
    
    /**
     * Организация отправитель
     */
    @Column(name = "sender_organization")
    private String senderOrganization;
    
    /**
     * Организация получатель
     */
    @Column(name = "recipient_organization")
    private String recipientOrganization;
    
    /**
     * Имя автора доставки
     */
    @Column(name = "author_name")
    private String authorName;
    
    /**
     * Телефон автора доставки
     */
    @Column(name = "author_phone")
    private String authorPhone;
    
    /**
     * Организация автора доставки
     */
    @Column(name = "author_organization")
    private String authorOrganization;
    
    /**
     * Общий объем груза
     */
    @Column(name = "volume")
    private Double volume;
    
    /**
     * Общий вес груза
     */
    @Column(name = "weight")
    private Double weight;
    
    /**
     * Источник создания заявки
     */
    @Column
    @Builder.Default
    private String source = "UNDEFINED";
    
    /**
     * ID маршрута
     */
    @Column(name = "trip_id")
    private UUID tripId;
    
    /**
     * Маршрут
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Routelist routelist;
    
    /**
     * Человекочитаемый ID маршрута
     */
    @Column(name = "trip_humanreadableid")
    private String cargoTripHumanReadableId;
    
    /**
     * Перечисление типов груза в заявке
     */
    @Column(name = "cargo_types")
    private String cargoTypes;
    
    /**
     * Контрольная дата с учетом выходных дней
     */
    @Column(name = "control_date")
    private LocalDateTime controlDate;
    
    /**
     * Запрос созданный по расписанию (регулярная перевозка)
     */
    @Column(name = "is_template")
    @Builder.Default
    private Boolean template = false;
    
    /**
     * Количество грузчиков
     */
    @Column(name = "loaders")
    private int loaders;
    
    /**
     * Organization id
     */
    @ManyToOne
    @JoinColumn(name = "Organization_id")
    private Organization organization;
    
    @Column(name = "add_contact_phone")
    private String addContactPhone;
    
    @Column(name = "add_contact_fio")
    private String addContactFIO;
    
    @Column(name = "executor_group_id")
    private UUID executorGroupId;
    
    @Column(name = "executor_group_name")
    private String executorGroupName;
    
    /**
     * Таймзона заявки
     */
    @Column
    private String timeZone;

    /**
     * Тип заявки
     */
    @Column(length = 100)
    private String requestType;

    @Column
    private String fraudMessage;
    
    @PrePersist
    private void addLinks() {
        if (getWaypoints() != null) {
            getWaypoints().forEach(wp -> wp.setRequest(this));
        }
    }
    
    public void setTariff(BaseTariff baseTariff) {
        this.tariff = baseTariff.getId();
    }
}
