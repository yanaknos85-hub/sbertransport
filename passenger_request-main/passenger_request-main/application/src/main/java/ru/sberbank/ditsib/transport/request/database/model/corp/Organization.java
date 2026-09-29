package ru.sberbank.ditsib.transport.request.database.model.corp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(schema = "request", name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {
    
    @Id
    @Setter
    private UUID id;
    
    @Column(name = "digit_id", columnDefinition = "numeric")
    private Long digitId;
    
    @Column(name = "official_name")
    private String officialName;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = true;
}
