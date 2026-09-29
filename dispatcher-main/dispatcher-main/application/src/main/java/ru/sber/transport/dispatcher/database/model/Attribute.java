package ru.sber.transport.dispatcher.database.model;

import lombok.*;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Справочник - Признаки водителя
 */
@Entity
@Table(schema = "dispatcher", name = "attribute",
       uniqueConstraints = @UniqueConstraint(columnNames = {"contractor_id", "name"}))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = "id")
public class Attribute {
    
    /** ID признака */
    @Id
    @GeneratedValue
    private UUID id;
    
    /** Контрагент, для которого составляется справочник */
    @ManyToOne
    @JoinColumn(name = "contractor_id", referencedColumnName = "id", nullable = false)
    private Contractor contractor;
    
    /** Наименование признака */
    @Column(nullable = false, name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;

}
