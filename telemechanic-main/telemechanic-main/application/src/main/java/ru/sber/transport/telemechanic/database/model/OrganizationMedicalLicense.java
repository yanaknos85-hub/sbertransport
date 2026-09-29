package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Медицинская лицензия организации
 */
@Entity
@Table(name = "organization_medical_license")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMedicalLicense {
    
    /**
     * Идентификатор записи о медицинской лицензии организации
     */
    @Id
    @NotNull
    private UUID id;
    
    /**
     * Серия
     */
    @NotBlank
    @Size(min = 1, max = 60)
    private String series;
    
    /**
     * Номер
     */
    @NotBlank
    @Size(min = 1, max = 60)
    private String number;
    
    /**
     * Дата выдачи
     */
    @NotNull
    private LocalDate issueDate;
    
    /**
     * Дата окончания срока действия
     */
    @NotNull
    private LocalDate expiryDate;
    
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
        var that = (OrganizationMedicalLicense) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
