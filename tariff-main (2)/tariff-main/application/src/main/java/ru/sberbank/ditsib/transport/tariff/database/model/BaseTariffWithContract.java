package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public abstract class BaseTariffWithContract extends BaseTariff {
    
    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    @Column
    private UUID contractorId;
    
}
