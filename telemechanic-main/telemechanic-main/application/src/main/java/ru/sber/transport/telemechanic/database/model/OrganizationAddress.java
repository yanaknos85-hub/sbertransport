package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Table(name = "organization_address")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationAddress {
    
    /**
     * Идентификатор организации
     */
    @Id
    private UUID organizationId;
    
    /**
     * Индекс адреса организации
     */
    @NotNull
    private String zip;
    
    /**
     * Регион организации
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", referencedColumnName = "id")
    private Region region;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (OrganizationAddress) o;
        return organizationId != null && Objects.equals(organizationId, that.organizationId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
