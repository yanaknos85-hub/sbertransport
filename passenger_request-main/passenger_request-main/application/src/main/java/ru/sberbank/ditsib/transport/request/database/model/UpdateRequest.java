package ru.sberbank.ditsib.transport.request.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.request.dto.RouteSegmentDTO;
import ru.sberbank.ditsib.transport.request.dto.WaypointDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity describing request
 */
@Entity
@Table(schema = "request", name = "update_request")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRequest {
    /**
     * Id of request
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Изменяемая заявка
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    /**
     * Список точек заявки
     */
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "waypoints")
    private final List<WaypointDTO> waypoints = new ArrayList<>();
    
    //Список точек маршрута
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "segments")
    private final List<RouteSegmentDTO> segmentsJSON = new ArrayList<>();
    
    /**
     * Предварительные расчетные данные по поездке
     */
    @Embedded
    private ExpectedData expected;
    
}
