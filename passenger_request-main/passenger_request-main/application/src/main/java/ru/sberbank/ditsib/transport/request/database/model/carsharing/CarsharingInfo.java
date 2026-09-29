package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Дополнительная информация по пользователю каршеринга
 */
@Entity
@Table(schema = "request", name = "carsharing_info")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;
    
    /**
     * Согласие на обработку перс данных
     */
    @Column
    private boolean consent;
    
    /**
     * Пользовался ли клиент уже каршерингом через банк
     */
    @Column
    private boolean previouslyUsed;
    
    @Column
    private LocalDateTime created;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
    }
}
