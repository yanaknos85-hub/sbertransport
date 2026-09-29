package ru.sberbank.ditsib.transport.vehicle.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Table(name = "category",
       uniqueConstraints = @UniqueConstraint(columnNames = {"title", "category"}))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = { "id", "title", "category" })
@EqualsAndHashCode(of = "id")
public class Category {
    @Id
    @GeneratedValue
    private UUID id;
    
    @NotBlank
    @Size(min = 1, max = 5)
    @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String category;
    
    @NotBlank
    @Size(min = 1, max = 255)
    private String title;
}
