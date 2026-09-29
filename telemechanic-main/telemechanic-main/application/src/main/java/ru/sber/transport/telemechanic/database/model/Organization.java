package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Организация
 */
@With
@Entity
@Table(name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {
    
    /**
     * Идентификатор записи об организации
     */
    @Id
    @Setter
    private UUID id;
    
    /**
     * Уникальный идентификатор (числовой)
     */
    @Column(columnDefinition = "numeric")
    private Long digitId;
    
    /**
     * Служебное название
     */
    private String officialName;
    
    /**
     * ОГРН
     */
    @NotBlank
    private String msrn;
    
    /**
     * ИНН
     */
    @NotBlank
    private String tin;
    
    /**
     * Идентификатор записи о группе организаций
     */
    private UUID organizationGroupId;
    
    /**
     * Флаг активности
     */
    @Setter
    @Builder.Default
    private boolean active = true;
    
    /**
     * Контактная информация
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "organization_contact",
               joinColumns = @JoinColumn(name = "organizationId"),
               inverseJoinColumns = @JoinColumn(name = "contactId"))
    private Set<Contact> contacts = new HashSet<>();
    
    /**
     * Адрес
     */
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "organization_id")
    private OrganizationAddress address;
    
    /**
     * Внешний идентификатор контрагента
     */
    @Setter
    private UUID contractorExternalId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Organization) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
