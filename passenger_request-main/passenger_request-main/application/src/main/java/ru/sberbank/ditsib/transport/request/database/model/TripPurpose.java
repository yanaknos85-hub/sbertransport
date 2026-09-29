package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * Entity describing business trip purposes
 */
@Entity
@Table(schema = "request", name = "trip_purpose")
@Data
@Getter
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TripPurpose {
    
    /**
     * Identifier
     */
    @Id
    @Column(name = "id_uuid")
    private UUID id;

    @Builder.Default
    @Column
    private boolean active = true;
    
    /**
     * Purpose string description.
     */
    @Column(nullable = false, unique = true)
    private String purpose;
    
    @Column
    private UUID organization;
}
