package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entity of contractor.
 */
@Entity
@Table(schema = "oto_cargo", name = "contractor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Contractor {
    
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "active")
    private boolean active;
    
}
