package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "attorney",
        uniqueConstraints = @UniqueConstraint(columnNames = { "attorney_id", "telemechanic_id" }))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = { "attorneyId", "id" })
@EqualsAndHashCode(of = { "id" })

@NamedEntityGraph(name = "attorney-full",
        attributeNodes = {
        @NamedAttributeNode(value = Attorney_.TELEMECHANIC, subgraph = Attorney_.TELEMECHANIC)
},
        subgraphs = {
        @NamedSubgraph(name = Attorney_.TELEMECHANIC, attributeNodes = {
                @NamedAttributeNode(Employee_.DEPARTMENT),
                @NamedAttributeNode(Employee_.ORGANIZATION)
        })
})
public class Attorney {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "telemechanic_id")
    private Employee telemechanic;

    @NotNull
    private UUID attorneyId;

    @NotNull
    private LocalDateTime issueDate;

    @NotNull
    private LocalDateTime expiryDate;

    @NotBlank
    @Size(max = 150)
    private String creationSystem;

}
