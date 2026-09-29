package ru.sberbank.transport.oto.cargo.database.model.tariff;

import lombok.*;
import ru.sberbank.transport.oto.cargo.database.model.Contractor;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Базовая сущность контракта
 */
@Entity
@Table(schema = "oto_cargo", name = "contract")
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    
    /**
     * идентификатор
     */
    @Id
    private UUID id;
    
    /**
     * Контрагент
     */
    @ManyToOne
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;
    
    /**
     * Флаг активности.
     */
    @Column
    @Builder.Default
    private Boolean active = true;
  
}
