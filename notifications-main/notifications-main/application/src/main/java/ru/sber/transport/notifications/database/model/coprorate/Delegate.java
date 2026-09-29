package ru.sber.transport.notifications.database.model.coprorate;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "notifications_corporate", name = "delegate")
@Getter
@Setter
@Builder
@AllArgsConstructor
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
