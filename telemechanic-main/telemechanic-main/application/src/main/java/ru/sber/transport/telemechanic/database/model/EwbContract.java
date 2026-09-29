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
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Договор ЭПЛ
 */
@Entity
@Getter
@Setter
@Table(name = "ewb_contract")
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class EwbContract {
    
    /**
     * Идентификатор записи о договоре
     */
    @Id
    @NotNull
    private UUID contractId;
    
    /**
     * Идентификатор записи об организации контрагента
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Вид осмотра
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(updatable = false)
    private InspectionType inspectionType;
    
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
     * Идентификатор записи о медицинской лицензии организации
     */
    private UUID organizationMedicalLicenseId;
    
    /**
     * Флаг активности
     */
    private boolean active;
    
    /**
     * Дата начала действия
     */
    @NotNull
    @Column(updatable = false)
    private LocalDate start;
    
    /**
     * Дата окончания действия
     */
    @NotNull
    @Column(name = "\"end\"", updatable = false)
    private LocalDate end;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (EwbContract) o;
        return contractId != null && Objects.equals(contractId, that.contractId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
