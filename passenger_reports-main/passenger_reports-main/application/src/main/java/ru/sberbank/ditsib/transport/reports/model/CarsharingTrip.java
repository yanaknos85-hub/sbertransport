package ru.sberbank.ditsib.transport.reports.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "carsharing_trip")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingTrip {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @NotNull
    @Column
    private Integer rentId;
    
    @Column
    private String carModel;
    
    @Column
    private String carNumber;
    
    @Type(JsonBinaryType.class)
    @Column
    private CarsharingDataMessage.Point startPoint;
    
    @Column
    private LocalDateTime rentCreatedAt;
    
    @Column
    private Double totalCost;
    
    @Column
    private Integer drivingTime;
    
    @Column
    private Double drivingTimeCost;
    
    @Column
    private Integer drivingLength;
    
    @Column
    private Double drivingLengthCost;
    
    @Column
    private Integer parkingTime;
    
    @Column
    private Double parkingTimeCost;
    
    @Column
    private Integer reserveTime;
    
    @Column
    private Double reserveTimeCost;
    
    @Type(JsonBinaryType.class)
    @Column
    private CarsharingDataMessage.Point finishPoint;
    
    @Column
    private LocalDateTime rentFinishedAt;
    
    @Column
    private String startAddress;
    
    @Column
    private String finishAddress;
    
    @Column
    private LocalDateTime created;
    
    @Column
    private LocalDateTime updated;
    
    @PrePersist
    protected void onSave() {
        created = updated = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
}
