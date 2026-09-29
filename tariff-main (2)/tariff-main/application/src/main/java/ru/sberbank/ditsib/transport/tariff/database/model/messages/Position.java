package ru.sberbank.ditsib.transport.tariff.database.model.messages;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = "tariff", name = "message_position")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Position {
    @Id
    @Setter
    private UUID id;
    
    @ElementCollection(targetClass = TaxiClass.class)
    @CollectionTable(schema = "tariff", name = "message_position_taxi_classes",
                     joinColumns = @JoinColumn(name = "position_id"))
    @Column(name = "taxi_class")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private final Set<TaxiClass> availableClasses = new HashSet<>();
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(name = "position_name")
    private String positionName;
    
    @Column(name = "self_approved")
    private boolean selfApproved;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = true;
    
    @PreRemove
    public void clear() {
        availableClasses.clear();
    }
}
