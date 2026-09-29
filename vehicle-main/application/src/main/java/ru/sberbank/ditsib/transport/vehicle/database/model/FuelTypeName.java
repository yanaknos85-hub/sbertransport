package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Table(name = "fuel_type_name")
@Entity
@IdClass(FuelTypeName.FuelTypeNameId.class)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString(of = {"fuelTypeId", "name"})
@EqualsAndHashCode(of = {"fuelTypeId", "name"})
public class FuelTypeName {

    @Id
    private UUID fuelTypeId;

    @Id
    @NotBlank(message = "Наименования вида топлива не может быть пустым")
    @Size(min = 1, max = 255)
    private String name;

    @Getter
    @Setter
    @EqualsAndHashCode(of = {"fuelTypeId", "name"})
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuelTypeNameId implements Serializable {
        private UUID fuelTypeId;
        private String name;
    }
}
