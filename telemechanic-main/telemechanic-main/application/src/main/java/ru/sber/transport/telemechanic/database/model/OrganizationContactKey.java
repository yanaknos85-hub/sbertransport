package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationContactKey implements Serializable {
    
    /**
     * Идентификатор записи об организации
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Идентификатор записи о контактных данных
     */
    @NotNull
    private UUID contactId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (OrganizationContactKey) o;
        return Objects.equals(organizationId, that.organizationId) && Objects.equals(contactId, that.contactId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
