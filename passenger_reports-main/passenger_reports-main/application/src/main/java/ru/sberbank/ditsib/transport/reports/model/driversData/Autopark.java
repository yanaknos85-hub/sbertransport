package ru.sberbank.ditsib.transport.reports.model.driversData;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Entity of autopark.
 */
@Entity
@Table(schema = "reports", name = "autopark")
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class Autopark {
    
    /**
     * Идентификатор автопарка
     */
    @Id
    private UUID id;
    
    /**
     * Название автопарка
     */
    @Column(name = "autopark_name")
    private String autoparkName;
    
}
