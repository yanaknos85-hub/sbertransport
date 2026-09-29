package ru.sber.transport.contractor.database.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Entity of cargo package.
 */
@Entity
@Table(schema = "contractors", name = "cargo_package")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CargoPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank
    @Size(max = 128)
    private String label;
    
    @NotNull
    private Double cost;
    
    @NotBlank
    @Size(max = 128)
    private String unit;
    
    @Builder.Default
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "contractor", nullable = false)
    private Contractor contractor;
}
