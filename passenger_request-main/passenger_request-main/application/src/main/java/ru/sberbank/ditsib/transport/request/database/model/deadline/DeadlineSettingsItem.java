package ru.sberbank.ditsib.transport.request.database.model.deadline;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Сущность-суперкласс - настройка контрольного срока для конкретного статуса заявки */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "item_type")
@Table(schema = "request", name = "deadline_settings_item_message")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineSettingsItem {
    
    /** ID элемента настройки */
    @Id
    private UUID id;
    
    /** Единица измерения времени */
    @Enumerated(EnumType.STRING)
    @Column
    private ChronoUnit unit;
    
    /** Значение контрольного срока (в указанных ед.изменрения) */
    @Column(columnDefinition = "int2 (Types#SMALLINT)")
    private Integer value;
    
    /** Получить итоговую длительность в виде Duration */
    public Duration getTotalDuration() {
        return Duration.of(value, unit);
    }
}
