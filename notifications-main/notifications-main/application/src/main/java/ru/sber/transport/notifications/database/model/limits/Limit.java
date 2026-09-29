package ru.sber.transport.notifications.database.model.limits;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(schema = "notifications_limits", name = "limit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Limit implements Persistable<UUID> {
    
    @Id
    private UUID id;
    
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    @Column
    @Builder.Default
    private Long sum = 0L;
    
    @Column
    @Builder.Default
    private Long balance = 0L;
    
    @Column(name = "owner_id")
    private UUID ownerId;
    
    @Column(name = "author_id")
    private UUID authorId;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
    @Column(name = "limit_id")
    private UUID limitId;
    
    @Column(name = "transport_type")
    private String transportType;
    
    @Column(name = "limit_sharing_type")
    private String limitSharingType;
    
    @Column(name = "limit_type")
    private String limitType;
    
    @Column(name = "limit_status")
    private String limitStatus;
    
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    @Column
    private Integer year;
    
    @Column(name = "period_number")
    private Integer periodNumber;
    
    @Column(name = "department_id")
    private UUID departmentId;
    
    @Transient
    private boolean isNew;
    
    @Column
    private boolean distributed;
    
}
