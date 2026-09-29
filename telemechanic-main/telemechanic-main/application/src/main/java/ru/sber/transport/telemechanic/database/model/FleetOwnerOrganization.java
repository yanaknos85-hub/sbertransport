package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Организация владельца автопарка
 */
@Entity
@Table(name = "fleet_owner_organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FleetOwnerOrganization {
    
    /**
     * Идентификатор записи об организации
     */
    @Id
    private UUID organizationId;
    
    /**
     * Организация
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    /**
     * Идентификатор записи об операторе ЭДО
     */
    @NotBlank
    @Size(min = 1, max = 10)
    private String edfOperatorId;
    
    /**
     * Код участника
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String edfCode;
    
    /**
     * Флаг активности
     */
    @Setter
    private boolean active;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (FleetOwnerOrganization) o;
        return organizationId != null && Objects.equals(organizationId, that.organizationId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
