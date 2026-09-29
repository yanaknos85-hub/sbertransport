package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Цель поездки
 */
@Entity
@Table(schema = "reports", name = "trip_purpose")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="TripPurposeDTO",
        classes= {
                @ConstructorResult(
                        targetClass = TripPurposeDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "label")
                        }
                )
        }
)
public class TripPurpose {
    
    /**
     * Id цели поездки интеграций
     */
    @Id
    @Column
    private UUID id;

    @Builder.Default
    private boolean active = true;
    
    /**
     * Текстовое описание.
     */
    @Column(name = "label")
    private String purpose;

    @Column
    private UUID organization;

    public TripPurpose(UUID id) {
        this.id = id;
    }
}
