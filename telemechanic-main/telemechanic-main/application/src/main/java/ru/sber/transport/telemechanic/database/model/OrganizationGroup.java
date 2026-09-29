package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "organization_group")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationGroup {
    
    /**
     * Идентификатор записи о группе организаций
     */
    @Id
    private UUID id;
    
    /**
     * Наименование группы организаций
     */
    @NotBlank
    @Size(max = 255)
    private String name;
    
    /**
     * Принадлежность к внутренней группе компаний
     */
    private boolean internal;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (OrganizationGroup) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
