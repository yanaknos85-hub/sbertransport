package ru.sberbank.ditsib.transport.vehicle.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sberbank.ditsib.transport.vehicle.helper.UserAuthorizationHelper;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

/**
 * Расход топлива
 */
@Entity
@Table(name = "fuel_consumption")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder(toBuilder = true)
public class FuelConsumption {
    
    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Расход (в литрах)
     */
    @NotNull
    @Positive
    @Max(9999)
    private int consumption;
    
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
    private LocalDateTime creationDate;
    
    /**
     * Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись
     */
    private UUID creatorUserId;
    
    @PrePersist
    private void onCreation() {
        this.creationDate = LocalDateTime.now();
        this.creatorUserId = UserAuthorizationHelper.getUserId(SecurityContextHolder.getContext().getAuthentication());
    }
}