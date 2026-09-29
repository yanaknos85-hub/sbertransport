package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Table(name = "telematics",
        uniqueConstraints = @UniqueConstraint(columnNames = "imei"))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "imei")
public class Telematics {
    @Id
    @GeneratedValue
    private UUID id;

    @Size(min = 1, max = 20)
    @Pattern(regexp = "\\d+")
    @NotBlank
    private String imei;

    @NotBlank
    @Size(min = 1, max = 255)
    private String title;
}
