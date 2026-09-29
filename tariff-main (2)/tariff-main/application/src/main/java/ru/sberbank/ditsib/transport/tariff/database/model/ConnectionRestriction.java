package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Сущность связи исключения работы заказчик-исполнитель
 */
@Entity
@Table(schema = "tariff", name = "connection_restriction")
@SuperBuilder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionRestriction {
    
    /**
     * Идентификатор записи.
     */
    @Id
    @GeneratedValue
    @Column
    UUID id;
    
    /**
     * Заказчик
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    Organization organization;
    
    /**
     * Исполнитель
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    Contractor contractor;
    
    /**
     * Источник ограничения
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_source")
    RestrictionSource restrictionSource;
}
