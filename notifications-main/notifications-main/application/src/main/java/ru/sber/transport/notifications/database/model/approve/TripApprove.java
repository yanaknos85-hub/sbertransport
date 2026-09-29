package ru.sber.transport.notifications.database.model.approve;

import lombok.*;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.coprorate.Employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сущность согласования.
 */
@Getter
@Setter
@Entity
@Table(schema = "notifications_approve", name = "trip")
@NoArgsConstructor
@RequiredArgsConstructor
public class TripApprove {
    
    @Column(name = "approver_id")
    private UUID approverId;
    
    @Id
    @NonNull
    @Column(name = "request_id")
    private UUID requestId;
    
    @Column(name = "owner_request_id")
    private UUID ownerRequestId;
    
    @Column(name = "passenger_id")
    private UUID passengerId;
    
    @Column(name = "status")
    private Boolean status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approve_status")
    private ApproveStatus approveStatus;
    
    @Column(name = "desired_date")
    private LocalDateTime desiredDate;
    
    @Column
    private String message;
    
    @Transient
    private TripRequest request;
   
    @Transient
    private Employee approver;
   
    @Transient
    private Employee passenger;
    
    @Transient
    private LocalDateTime deadlineDateTime;
    
    @Transient
    private List<UUID> approverIds = new ArrayList<>();
    
    @Transient
    private String approveStatusDescription;
    
    
}
