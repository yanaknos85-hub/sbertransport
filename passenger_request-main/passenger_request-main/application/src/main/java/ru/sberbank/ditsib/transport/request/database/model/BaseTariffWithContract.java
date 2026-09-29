package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
    
    /**
     * Идентификатор договора
     */
    @Column(name = "contract_id")
    private UUID contractId;
    
}
