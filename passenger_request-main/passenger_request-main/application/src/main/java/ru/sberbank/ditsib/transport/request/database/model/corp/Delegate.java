package ru.sberbank.ditsib.transport.request.database.model.corp;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сущность делегата.
 */
@Entity
@Table(schema = "request", name = "corp_delegate")
@Getter
@Setter
@NoArgsConstructor
public class Delegate {
    
    @Id
    private UUID id;
    
    @Column(name = "delegate_id")
    private UUID delegateId;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    @Column(name = "supervisor_id")
    private UUID supervisorId;
    
}
