package ru.sberbank.ditsib.transport.srm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import ru.sber.transport.constants.PointMatchingType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.model.tariff.CoopTariffParams;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Сущность совместной поездки
 */
@Entity
@Table(schema = "srm", name = "srm_shared_ride")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SrmSharedRide {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Идентификатор
     */
    @Generated
    @Column(name = "old_id", columnDefinition = "serial", updatable = false, insertable = false)
    private Integer oldId;
    
    /**
     * Идентификатор тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Временная зона.
     */
    @Column(name = "time_zone")
    private String timeZone;
    
    /**
     * Тип совмещения точек.
     */
    @Column(name = "point_matching_type")
    @Enumerated(EnumType.STRING)
    private PointMatchingType pointMatchingType;
    
    /**
     * Тип транспорта
     */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * Полная стоимость
     */
    @Column(name = "ride_cost")
    private Long rideCost;
    
    /**
     * Полное расстояние в км
     */
    @Column(name = "ride_distance")
    private Double rideDistance;
    
    /**
     * Рассчитанное время поездки в секундах
     */
    @Column(name = "ride_time")
    private Integer rideTime;
    
    /**
     * Список остановок
     */
    @OrderColumn(name = "ordering_index")
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "sharedRide", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SrmWaypoint> waypoints = new ArrayList<>();
    
    /**
     * KPI отдельных заказов
     */
    @OrderBy("orderingIndex ASC")
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "sharedRide", fetch = FetchType.LAZY)
    @Builder.Default
    private List<SrmRequestKpi> requestKpiList = new ArrayList<>();
    
    /**
     * Флаг активности поездки. false -  удалена/отменена
     */
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    /**
     * Date and time or request creation
     */
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    /**
     * Идентификатор связки
     */
    @Column(name = "bunch_id")
    private UUID bunchId;
    
    /**
     * Идентификатор связки
     */
    @Column(name = "bunch_number")
    @Builder.Default
    private Integer bunchNumber = 0;
    
    //Параметры для подбора совместной поездки
    @Embedded
    @Builder.Default
    private final CoopTariffParams coopTariffParams = new CoopTariffParams();
    
    @PrePersist
    @PreUpdate
    private void linkChildren() {
        requestKpiList.removeIf(Objects::isNull);
        waypoints.removeIf(Objects::isNull);
        requestKpiList.forEach(req -> req.setSharedRide(this));
        waypoints.forEach(waypoint -> waypoint.setSharedRide(this));
    }
    
    public List<SrmRequestKpi> getActiveRequestKpiList() {
        return getRequestKpiList().stream()
                                  .filter(SrmRequestKpi::isActive)
                                  .toList();
    }
}
