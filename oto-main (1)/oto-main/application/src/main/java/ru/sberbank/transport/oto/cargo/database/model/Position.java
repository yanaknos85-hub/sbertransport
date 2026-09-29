package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "oto_cargo", name = "position")
@Builder(toBuilder = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Position {

    /**
     * Id позиции
     */
    @Id
    private UUID id;
    
    @Column(name = "position_name", nullable = false)
    private String name;

}
