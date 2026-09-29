package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Водительское удостоверение
 */
@Entity
@Table(name = "driving_license")
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DrivingLicense {
    
    /**
     * Идентификатор записи о водительских удостоверениях
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Серия
     */
    @NotBlank
    @Size(min = 1, max = 20)
    private String series;
    
    /**
     * Номер
     */
    @NotBlank
    @Size(min = 1, max = 20)
    private String number;
    
    /**
     * Дата выдачи
     */
    @NotNull
    private LocalDate issueDate;
    
    /**
     * Дата истечения
     */
    @NotNull
    private LocalDate expiryDate;
    
    /**
     * Идентификатор предыдущего водительского удостоверения
     */
    private UUID previousId;
    
    /**
     * Флаг активности
     */
    private boolean active;
    
    /**
     * Категории
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "driving_license_category",
               joinColumns = @JoinColumn(name = "driving_license_id", nullable = false),
               inverseJoinColumns = @JoinColumn(name = "category_id", nullable = false))
    private Set<Category> categories = new HashSet<>();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (DrivingLicense) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
