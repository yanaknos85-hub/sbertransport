package ru.sberbank.ditsib.transport.vehicle.database.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

/**
 * Размер колеса ТС
 */
@Table(name = "wheel_size",
        uniqueConstraints = @UniqueConstraint(columnNames = {"title"}))
@Entity
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(of = {"id", "title"})
@EqualsAndHashCode(of = "id")
public class WheelSize {
    /**
     * Идентификатор Размера колеса ТС
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Наименование Размера колеса ТС
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String title;

}