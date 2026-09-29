package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.ResultSetWaypointWithAddress;

import jakarta.persistence.*;
import java.time.Duration;
import java.util.UUID;

/**
 * Модель точки пути
 */
@Entity
@Table(schema = "reports", name = "waypoint")
@Getter
@Setter
@ToString(exclude = "request")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name = "ResultSetWaypointWithAddress",
        classes = {
                @ConstructorResult(
                        targetClass = ResultSetWaypointWithAddress.class,
                        columns = {
                                @ColumnResult(name = "request_id", type = UUID.class),
                                @ColumnResult(name = "ordering_index"),
                                @ColumnResult(name = "building"),
                                @ColumnResult(name = "city"),
                                @ColumnResult(name = "country"),
                                @ColumnResult(name = "house"),
                                @ColumnResult(name = "region"),
                                @ColumnResult(name = "street"),
                                @ColumnResult(name = "structure"),
                                @ColumnResult(name = "checkin_automatic"),
                                @ColumnResult(name = "checkin_manual"),
                                @ColumnResult(name = "wait_time", type = Duration.class),
                                @ColumnResult(name = "exist_in_vsp_tb_registry")
                        }
                )
        }
)
public class Waypoint {
    
    public Waypoint(UUID id) {
        this.id = id;
    }
    
    /**
     * Идентификатор
     */
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * Адрес
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
    
    /**
     * Связанная заявка, в которой задана данная точка
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    /**
     * Время ожидания
     */
    @Column(name = "wait_time", columnDefinition = "int8 (Types#BIGINT)")
    private Duration waitTime;
    
    /**
     * Порядок остановок
     */
    @Column(name = "ordering_index", updatable = false, insertable = false)
    private Integer orderingIndex;
    
    /**
     * Автоматический чекин
     */
    @Column(name = "checkin_automatic")
    private Boolean checkinAutomatic = false;
    
    /**
     * Ручной чекин
     */
    @Column(name = "checkin_manual")
    private Boolean checkinManual = false;
    
    @PreRemove
    private void tearDown() {
        this.request = null;
        this.address = null;
    }
}
