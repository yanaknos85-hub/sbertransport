package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Контрагент и статус подключения
 */
@Entity
@Table(schema = "request", name = "contractor_status")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractorAndJoinStatus {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    /** ID контрагента */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;
    
    /** Регион */
    @Column(nullable = false)
    private String region;
    
    /** Статус подключения. Проставляется инженером ОТО (в ручном режиме) или по результату подключения по интеграции */
    @Enumerated(EnumType.STRING)
    @Column(name = "join_status")
    private CorporateCarsharingJoinStatus joinStatus;
    
    /** Выбор сотрудником каршеринга, к которому необходимо подключение */
    @Column(name = "employee_choice")
    @Builder.Default
    private boolean employeeChoice = false;
}
