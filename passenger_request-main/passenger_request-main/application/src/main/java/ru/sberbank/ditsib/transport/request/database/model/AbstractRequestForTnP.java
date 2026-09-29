package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Абстрактный класс объединяющий общие поля для запросов с типом транспорта такси (T) и личный транспорт(P) (TnP).
 */
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractRequestForTnP extends AbstractRequestForTnPnC {
    
    /**
     * Comment
     */
    @Column(name = "comment")
    private String commentForDriver;
}
