package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.util.Objects;

/**
 * Связь контактных данных и организаций
 */
@Entity
@Table(name = "organization_contact")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationContact {
    
    /**
     * Первичный ключ
     */
    @EmbeddedId
    private OrganizationContactKey organizationContactKey;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (OrganizationContact) o;
        return organizationContactKey != null && Objects.equals(organizationContactKey, that.organizationContactKey);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
