package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table(name = "medic_request")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MedicRequest {
    
    /**
     * Идентификатор записи заявки телемедицины
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Номер заявки
     */
    @NotBlank
    private String humanReadableId;
    /**
     * Время создания заявки
     */
    @NotNull
    private LocalDateTime creationTime;
    /**
     * Статус заявки
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private TelemedicineStatus status;
    /**
     * Систолическое артериальное давление (мм рт. ст)
     */
    @Min(40)
    @Max(300)
    @Positive
    private Integer systPressure;
    /**
     * Диастолическое артериальное давление (мм рт. ст)
     */
    @Min(40)
    @Max(300)
    @Positive
    private Integer dyastPressure;
    /**
     * Пульс (уд./мин)
     */
    @Min(0)
    @Max(300)
    private Integer pulse;
    /**
     * Температура (°С)
     */
    @Min(30)
    @Max(47)
    private BigDecimal temperature;
    /**
     * Алкоголь в крови (Промилле)
     */
    @Min(0)
    @Max(1)
    private BigDecimal bloodAlcohol;
    /**
     * Комментарий заявки
     */
    private String comment;
    
    /**
     * Идентификатор записи об организации
     */
    private UUID organizationId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (MedicRequest) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
