package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@Table(schema = "request", name = "checkin_settings")
@Data
public class CheckinSettings {
    
    /**
     * Id of request
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Service type
     */
    @Column(name = "service_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportServiceType serviceType;
    
    /**
     * Transport type
     */
    @Column(name = "transport_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * region
     */
    @Column(name = "region_id")
    private UUID region;
    
    /**
     * radius
     */
    @Column
    private Integer radius;
    
    /**
     * Режим чекина
     */
    @Column(name = "checkin_only_manual")
    @Builder.Default
    private Boolean checkinOnlyManual = false;
    
}
