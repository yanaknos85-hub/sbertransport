package ru.sberbank.ditsib.transport.srm.model;

import jakarta.persistence.*;
import lombok.*;
import ru.sber.transport.constants.EventType;
import ru.sber.transport.srm.model.Waypoint;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(schema = "srm", name = "srm_waypoint")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "sharedRide")
@Builder(toBuilder = true)
public class SrmWaypoint implements Waypoint {
    
    /**
     * Id точки маршрута
     */
    @EqualsAndHashCode.Include
    @Id
    private UUID id;
    
    /**
     * Связанная заявка в которой задана данная точка
     */
    @Column(name = "request_kpi_id")
    private UUID requestKpiId;
    
    /**
     * Ссылка на совместную поездку содержащую данную точку
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_ride_id", columnDefinition = "int8")
    private SrmSharedRide sharedRide;
    
    /**
     * Группа точек, находящихся внутри минимального радиуса включения в поездку
     */
    @Column(name = "group_id")
    private UUID groupId;
    
    /**
     * Тип события (посадка, высадка, ожидание)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private EventType eventType;
    
    /**
     * Время начала остановки
     */
    @Column(name = "start_time")
    private ZonedDateTime startTime;
    
    /**
     * Время завершения остановки
     */
    @Column(name = "end_time")
    private ZonedDateTime endTime;
    
    /**
     * Время ожидания в секундах
     */
    @Builder.Default
    @Column(name = "waiting_time")
    private Integer waitingTime = 0;
    
    /**
     * Широта
     */
    @Column
    private Double latitude;
    
    /**
     * Долгота
     */
    @Column
    private Double longitude;
    
    /**
     * Наименование
     */
    @Column
    private String address;
    
    /**
     * Порядок остановок в изначальном запросе
     */
    @Column(name = "org_ordering_index")
    @Builder.Default
    private Integer orgOrderingIndex = 0;
    
    /**
     * Порядок остановок в совместной поездке
     */
    @Column(name = "ordering_index")
    @Builder.Default
    private Integer orderingIndex = 0;
    
    /**
     * Количество грузчиков
     */
    @Column(name = "loader_number")
    @Builder.Default
    private Integer loaderNumber = 0;
    
    /**
     * Расстояние от предыдушей точки в км
     */
    @Column(name = "distance_from_prev_waypoint")
    @Builder.Default
    private Double distanceFromPrevWaypoint = 0d;
    
    /**
     * Состояние точки маршрута. true - активна false - удалена
     */
    @Builder.Default
    @Column(name = "active")
    private boolean active = true;
}
