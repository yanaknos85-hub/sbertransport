package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.NotAudited;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.database.model.magenta.CoopRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NamedEntityGraph(
        name = "requestTaxiForDispatcher",
        attributeNodes = {
                @NamedAttributeNode(value = "author", subgraph = "employee"),
                @NamedAttributeNode(value = "approvedBy", subgraph = "employee"),
                @NamedAttributeNode(value = "passenger", subgraph = "employee"),
                @NamedAttributeNode(value = "waypoints"),
                @NamedAttributeNode(value = "driver")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "employee",
                        attributeNodes = {
                                @NamedAttributeNode("department")
                        }
                )
        }
)
@EqualsAndHashCode(exclude = "requestRating", callSuper = true)
@Entity
@DynamicUpdate
@Table(schema = "request", name = "request_for_taxi")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SQLDelete(sql = "UPDATE request_for_taxi SET active = false WHERE id = ?")
@SQLRestriction("active=true")
public class RequestForTaxi extends AbstractRequestForTnP implements CoopRequest, DriverInfo, DriverArrivedInfo, DeadlineInfo {
    
    /**
     * Class of taxi used for request
     */
    @Column(name = "trip_class")
    @Enumerated(EnumType.STRING)
    private TaxiClass taxiClass;
    
    /**
     * Geo and cost data
     */
    @Embedded
    private RequestRating requestRating;
    
    /**
     * Компания перевозчик
     */
    @NotNull
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    @Builder.Default
    @Column(name = "auto_cancel_deadline_min")
    private Integer autoCancelDeadlineMin = 60;
    
    @Builder.Default
    @Column(name = "sent_to_contractor")
    private boolean sentToContractor = false;
    
    /**
     * Контрольный срок (для диспетчерской)
     */
    @Column(name = "driver_assignment_deadline")
    private LocalDateTime driverAssignmentDeadline;
    
    /**
     * Дата и время старта поиска водителя на такси
     */
    @Column(name = "taxi_awaiting_search_start_date")
    private LocalDateTime taxiAwaitingSearchStartDate;
    
    /**
     * Фактическая информация по индивидуальной поездке
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_trip_id")
    private TaxiTrip taxiTrip;
    
    /**
     * Driver
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "driverId")
    @NotAudited
    private Driver driver;
    
    @Column(name = "resolution")
    private String resolution;
    
    /**
     * History of status changes
     */
    @OneToMany(mappedBy = "requestForTaxi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private final List<RequestHistoryElementForTaxi> historyItemsForTaxi = new ArrayList<>();
    
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_state")
    private DeadlineState deadlineState = DeadlineState.NONE;
    
    /**
     * Триггерное время
     */
    @Column(name = "trigger_time")
    @PositiveOrZero
    @Builder.Default
    private Integer triggerTime = 60;
    
    @Column(name = "bus_rent_duration")
    private Integer busRentDuration;
    
    @Column(name = "bus_count")
    private Integer busCount;
    
    @Column(name = "fact_waiting_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration factWaitingTime;
    
    @Column(name = "fact_distance")
    private Double factDistance;
    
    /**
     * время перехода заявки в статус Водитель ожидает в точке отправления TAXI_DRIVER_ARRIVED
     */
    @Column(name = "driver_arrived_datetime")
    private LocalDateTime driverArrivedDatetime;
    
    /**
     * максимальная дата прибытия водителя, без нарушения SLA (расчетный параметр approval_date +60 минут +15 минут для срочных, для не срочных
     * desired_date +15 минут)
     */
    @Column(name = "driver_arrived_deadline")
    private LocalDateTime driverArrivedDeadline;
    
    /**
     * Количество присоединившихся пассажиров (заявок)
     */
    @Column(name = "number_passengers_joined")
    private Integer numberPassengersJoined;
    
    /**
     * Дата получение всей информации по поездке или принудительное закрытие системой
     */
    @Column(name = "request_closed_datetime")
    private LocalDateTime requestClosedDatetime;
    
    @PrePersist
    private void addLinks() {
        if (getWaypoints() != null) {
            getWaypoints().forEach(wp -> wp.setRequest(this));
        }
        if (getHistoryItemsForTaxi() != null) {
            getHistoryItemsForTaxi().forEach(item -> item.setRequestForTaxi(this));
        }
    }
    
    @PreRemove
    private void removeLinkedEntities() {
        this.historyItemsForTaxi.clear();
    }
    
    @Transient
    public CarInfo getVehicle() {
        return taxiTrip == null ? null : taxiTrip.getAssignedCar();
    }
}
