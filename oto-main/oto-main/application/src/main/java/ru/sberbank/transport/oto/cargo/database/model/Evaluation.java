package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;
import ru.sberbank.transport.oto.cargo.enums.EvaluationReason;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity of Evaluation
 */
@Entity
@Table(schema = "oto_cargo", name = "evaluation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evaluation implements HasId {
    
    @Id
    private UUID id;
    
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    @ElementCollection(fetch = FetchType.EAGER, targetClass = EvaluationReason.class)
    @CollectionTable(schema = "oto_cargo", name = "evaluation_reasons",
                     joinColumns = @JoinColumn(name = "evaluation_id"))
    @Column(name = "reason")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<EvaluationReason> reasons = new HashSet<>();
    
    @Column(name = "rating", nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;
    
    @Column(name = "comment")
    @Size(max = 255)
    private String comment;
    
}
