package ru.sberbank.ditsib.transport.reports.model.magenta;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.SharedRideKpiResultSet;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Сущность совместной поездки
 */
@Entity
@Table(schema = "reports", name = "shared_ride")
@Data
@EqualsAndHashCode(of = "magentaId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="SharedRideKpiResultSet",
        classes= {
                @ConstructorResult(
                        targetClass = SharedRideKpiResultSet.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "total_cost"),
                                @ColumnResult(name = "total_distance_km"),
                                @ColumnResult(name = "total_time_min")
                        }
                )
        }
)
public class SharedRide {
    
    /**
     * Идентификатор, формируется на стороне Magenta
     */
    @Id
    @Column(name = "magenta_id")
    private UUID id;
    
    /**
     * Количество пассажиров
     */
    @Column(name = "passengers")
    private int passengers;
    
    /**
     * Идентификатор тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Флаг активности поездки. false -  удалена/отменена
     */
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    /**
     * Расчётный KPI
     */
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "kpi")
    private SharedRideKPI kpi;

    /**
     * Фактическая информация по совмещенной поездке
     */
    @OneToOne(mappedBy = "sharedRide", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private CoopTaxiTrip coopTaxiTrip;
}
