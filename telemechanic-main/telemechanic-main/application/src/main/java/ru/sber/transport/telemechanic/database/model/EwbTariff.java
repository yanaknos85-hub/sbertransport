package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;

/**
 * Тариф ЭПЛ
 */
@Entity
@Getter
@Table(name = "ewb_tariff")
@NoArgsConstructor
@AllArgsConstructor
public class EwbTariff {
    
    /**
     * Идентификатор записи о тарифе
     */
    @Id
    @NotNull
    private UUID tariffId;
    
    /**
     * Договор
     */
    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", referencedColumnName = "contractId", nullable = false)
    private EwbContract contract;
    
    /**
     * Идентификатор записи об организации контрагента
     */
    @NotNull
    private UUID organizationId;
    
    /**
     * Идентификатор записи о подразделении контрагента
     */
    @NotNull
    private UUID departmentId;
    
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
        var that = (EwbTariff) o;
        return contract != null && Objects.equals(contract, that.contract);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
