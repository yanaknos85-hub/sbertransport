package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

/**
 * Показания одометра
 */
@Entity
@Table(name = "odometer_value")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class OdometerValue {
    
    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Показатель (в километрах)
     */
    @NotNull
    @Positive
    @Max(999999)
    private int value;
    
    /**
     * Ссылка на идентификатор транспортного средства
     */
    @NotNull
    private UUID transportId;
    
    /**
     * Год внесения показателей
     */
    @NotNull
    @Positive
    @Max(9999)
    private int year;
    
    /**
     * Месяц внесения показателей
     */
    @Enumerated
    private Month month;
    
    /**
     * Дата и время создания записи
     */
    @UpdateTimestamp
    private LocalDateTime creationDate;
    
    /**
     * Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись
     */
    private UUID creatorUserId;
    
}