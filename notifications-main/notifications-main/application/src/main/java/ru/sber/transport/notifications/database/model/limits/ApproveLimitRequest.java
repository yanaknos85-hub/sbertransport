package ru.sber.transport.notifications.database.model.limits;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "notifications_limits", name = "approve_limit_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLimitRequest{
    @Id
    private UUID id;
    
    @Column(name = "department_id")
    private UUID departmentId;
   
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(name = "employee_id")
    private UUID employeeId;
    
    @Column(name = "limit_request_id")
    private UUID limitRequestId;
    
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    @Column(name = "author_id")
    private UUID authorId;
    
    @Column(name = "creation_time")
    private LocalDateTime creationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private TransportTypeEnum transportType;
    
    @Column
    private Integer year;
    
    @Column
    private Integer period;
    
    @Column
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(name = "limit_type")
    private LimitType limitType;
    
    @Column
    private Long sum;
    
    @Column
    private String description;
    
    @Column(name = "decline_reason")
    private String declineReason;
    
    @Column(name = "sum_limit")
    private Long sumLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_state")
    private ApprovalState approvalState;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
    
    @Column
    private boolean deleted;
    
   
 }
