package ru.sberbank.ditsib.transport.request.database.model.driversData;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Справочник - Признаки водителя
 */
@Entity
@Table(schema = "request", name = "driver_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contractor_id", "name"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DriverTag {

    /** ID признака */
    @Id
    private UUID id;

    /** Контрагент, для которого составляется справочник */
    @Column(name = "contractor_id")
    private UUID contractor;

    /** Наименование признака */
    @Size(max = 128)
    @Column(nullable = false, name = "name")
    private String name;
    
}
