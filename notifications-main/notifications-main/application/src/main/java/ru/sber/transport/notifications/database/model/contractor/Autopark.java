package ru.sber.transport.notifications.database.model.contractor;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder(toBuilder = true)
@Table(schema = "notifications_contractor", name = "autopark")
public class Autopark {
    
    /**
     * Идентификатор автопарка
     */
    @Id
    private UUID id;
    
    /**
     * Название автопарка
     */
    @Column(name = "autopark_name", nullable = false)
    private String autoparkName;
    
    /**
     * Контрагент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;
    
}
