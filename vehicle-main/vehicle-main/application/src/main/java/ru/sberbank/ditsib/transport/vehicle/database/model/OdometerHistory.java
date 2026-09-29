package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "odometer_history")
public class OdometerHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private LocalDateTime creationTime;
    
    private UUID transportId;
    
    private UUID creatorUserId;
    
    private int value;

    /**
     * Различные данные об источнике и причинах изменения показаний одометра.
     * Потенциальные кандидаты на включение в таблицу как отдельное поле
     * */
    private String metaAttributes;
}
