package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;
import ru.sber.transport.telemechanic.enumerate.ContactType;

import java.util.Objects;
import java.util.UUID;

/**
 * Контактные данные
 */
@Entity
@Table(name = "contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Contact {
    
    /**
     * Идентификатор записи о контактных данных
     */
    @Id
    private UUID id;
    
    /**
     * Тип
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ContactType type;
    
    /**
     * Значение
     */
    @NotBlank
    private String value;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Contact) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
