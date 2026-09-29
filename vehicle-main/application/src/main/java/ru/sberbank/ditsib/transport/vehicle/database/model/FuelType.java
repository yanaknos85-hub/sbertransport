package ru.sberbank.ditsib.transport.vehicle.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Table(name = "fuel_type",
        uniqueConstraints = @UniqueConstraint(columnNames = "title"))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Getter
@Setter
@ToString(of = {"id", "title"})
@EqualsAndHashCode(of = "id")
public class FuelType {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(min = 1, max = 255)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "engine_type_id", referencedColumnName = "id", nullable = false)
    private EngineType engineType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "fuelTypeId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FuelTypeName> fuelTypeNames;

}
