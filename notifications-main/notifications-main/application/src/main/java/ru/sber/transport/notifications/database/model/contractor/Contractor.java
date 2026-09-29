package ru.sber.transport.notifications.database.model.contractor;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "notifications_contractor", name = "contractor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contractor {
    
    /**
     * Идентификатор контрагента
     */
    @Id
    private UUID id;
    
    /**
     * Имя контрагента
     */
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @OneToMany(mappedBy = "contractor", cascade = CascadeType.REMOVE)
    @Builder.Default
    private List<Autopark> autoparks = new ArrayList<>();
}
