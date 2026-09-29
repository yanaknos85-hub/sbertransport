package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;

/**
 * Настройка режима обработки заявок на подключение к корп.каршерингу
 */
@Entity
@Table(schema = "request", name = "join_request_processing_mode")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingJoinRequestProcessingModeSetting {
    
    /** Режим обработки заявок на подключение к корп.каршерингу */
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_mode")
    private CarsharingJoinRequestProcessingMode processingMode;
}
