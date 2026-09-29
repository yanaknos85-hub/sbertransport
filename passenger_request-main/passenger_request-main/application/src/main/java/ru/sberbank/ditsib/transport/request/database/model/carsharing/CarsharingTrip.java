package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.request.database.model.RequestForCarsharing;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "request", name = "carsharing_trip")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingTrip {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    RequestForCarsharing request;
    
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
