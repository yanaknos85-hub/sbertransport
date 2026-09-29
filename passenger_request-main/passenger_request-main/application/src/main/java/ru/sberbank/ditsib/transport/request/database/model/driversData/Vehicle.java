package ru.sberbank.ditsib.transport.request.database.model.driversData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * Entity of transport.
 */

@Entity
@Table(schema = "request", name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Vehicle {
    
    /**
     * ID.
     */
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    
    /**
     * Brand.
     */
    @Column
    private String brand;
    
    /**
     * Model.
     */
    @Column
    private String model;
    
    /**
     * State number.
     */
    @Column(name = "state_number")
    private String stateNumber;
    
    /**
     * Color.
     */
    @Column
    private String color;
    
    /**
     * ID of autopark;
     */
    @Column(name = "autopark_id")
    private UUID autoparkId;
    
    @Column
    @Builder.Default
    private boolean active = true;
    
}
