package ru.sberbank.ditsib.transport.vehicle.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Table(name = "drive",
       uniqueConstraints = @UniqueConstraint(columnNames = "title"))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = { "id", "title" })
@EqualsAndHashCode(of = "id")
public class Drive {
    @Id
    @GeneratedValue
    private UUID id;
    
    @NotBlank
    @Size(min = 1, max = 255)
    private String title;
}
