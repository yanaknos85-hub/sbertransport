package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Message with data about limits.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LimitMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private UUID id;
    
    /**
     * creationTime
     */
    private LocalDateTime creationTime;
    
    /**
     * ID Readable
     */
    private String humanReadableId;
    
    /**
     * Author of limit
     */
    private UUID limitAuthorId;
    
    /**
     * Author of sharing
     */
    private UUID sharingAuthorId;
    
    /**
     * Owner of limit
     */
    private UUID ownerId;
    
    /**
     * Organization of limit
     */
    private UUID organizationId;
    
    /**
     * ID of department.
     */
    private UUID departmentId;

    /**
     * ID of employee.
     */
    private UUID employeeId;
    
    /**
     * Human readable id department
     */
    private String departmentLimitHumanReadableId;
    
    /**
     * sum.
     */
    private Long sum;
    
    /**
     * Balance
     */
    private Long balance;
    
    /**
     * Reserve
     */
    private Long reserve;
    
    /**
     * Limit transport type
     */
     private String transportType;
    
    /**
     * Limit  type
     */
    private String limitType;
    
    /**
     * Limit  status
     */
    private String limitStatus;
    
    /**
     * Year
     */
    private Integer year;
    
    /**
     * periodNumber
     */
    private Integer periodNumber;
    
    /**
     * Limit sharing type
     */
    private String limitSharingType;
    
    /**
     * Limit service type
     */
    private String limitServiceType;
    
    /**
     * Limit message type
     */
    private String limitMessageType;
    
    /**
     * Use my limit flag
     */
    private boolean useThisLimit;
    
    /**
     * Parent department - for searching by distributing department without joining.
     */
    private UUID parentDepartmentId;
    
    /**
     * Parent limit
     */
    private UUID parentLimitId;

    /**
     * Идентификатор лимита департамента
     */
    private UUID limitId;
    
    /**
     * Флаг распределенности
     */
    private boolean distributed;
    
    /**
     * Entity deleted.
     */
    @Builder.Default
    private boolean deleted = false;
}
