package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Сущность лимита подразделения, получаемая из сообщения
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "request", name = "dep_limit_message")
public class DepLimit {
    
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    /** ID подразделения */
    @Column(name = "department_id", nullable = false)
    private UUID departmentId;
    
    /** ID владельца лимита подразделения */
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
    
    /** Год */
    @Column(name = "year", nullable = false)
    private Integer year;
    
    /**
     * Признак активности лимита.
     */
    @Column(name = "active")
    private boolean active = true;
}
