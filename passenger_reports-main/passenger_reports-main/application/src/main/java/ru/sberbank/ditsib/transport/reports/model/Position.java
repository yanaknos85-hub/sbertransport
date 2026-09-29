package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.domain.Persistable;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "position")
@Builder(toBuilder = true)
@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class Position {

    /**
     * Id позиции
     */
    @Id
    private UUID id;
    
    @Column(name = "position_name", nullable = false)
    private String name;

}
