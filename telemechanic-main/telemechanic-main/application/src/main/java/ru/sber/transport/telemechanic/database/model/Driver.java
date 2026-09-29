package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "driver")
@NamedEntityGraph(name = "search-driver", attributeNodes = {
        @NamedAttributeNode(value = Driver_.DRIVING_LICENSE),
        @NamedAttributeNode(value = Driver_.EMPLOYEE, subgraph = Driver_.EMPLOYEE)
}, subgraphs = {
        @NamedSubgraph(name = Driver_.EMPLOYEE, attributeNodes = {
                @NamedAttributeNode(value = Employee_.ORGANIZATION),
                @NamedAttributeNode(value = Employee_.DEPARTMENT)
        })
})
public class Driver {
    
    /**
     * Идентификатор водителя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Идентификатор сотрудника
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    private Employee employee;
    
    /**
     * Идентификатор водительского удостоверения
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = false)
    @JoinColumn(name = "driving_license_id", referencedColumnName = "id", nullable = false)
    private DrivingLicense drivingLicense;
    
    /**
     * ИНН
     */
    @NotBlank
    private String tin;
    
    /**
     * СНИЛС
     */
    private String snils;
    
    /**
     * Идентификатор автопарка
     */
    private UUID autoparkId;
    
    /**
     * Идентификатор контрагента
     */
    private UUID contractorId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Driver) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
