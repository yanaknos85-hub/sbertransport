package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Транспортное средство
 */
@Entity
@Table(name = "transport")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Accessors(chain = true)
public class Transport {
    
    /**
     * Идентификатор записи о транспортном средстве
     */
    @Id
    @NotNull
    private UUID id;
    
    /**
     * Автомобильный номер
     */
    @NotBlank
    private String stateNumber;
    
    /**
     * Марка
     */
    @NotBlank
    private String brand;
    
    /**
     * Модель
     */
    @NotBlank
    private String model;
    
    @Positive
    private Integer mileage;
    
    /**
     * Статус
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private TransportStatus status = TransportStatus.IN_USE;
    
    /**
     * Тип ТС
     */
    @NotBlank
    @Size(max = 255)
    private String type;
    
    /**
     * Подтип ТС
     */
    @NotBlank
    @Size(max = 255)
    private String subtype;
    
    @NotNull
    @Column(name = "fuel_tank_volume")
    private Integer fuelTankVolume;
    
    /**
     * Организации
     */
    @ManyToMany
    @JoinTable(
            name = "transport_organization",
            joinColumns = @JoinColumn(name = "transport_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private Set<Organization> organizations = new HashSet<>();
    
    /**
     * Остаток топлива
     */
    private Integer fuelLitreage;
    
    /**
     * Идентификатор автопарка
     */
    private UUID contractorId;
    
    /**
     * Идентификатор филиала автопарка
     */
    private UUID autoparkId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Transport) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
