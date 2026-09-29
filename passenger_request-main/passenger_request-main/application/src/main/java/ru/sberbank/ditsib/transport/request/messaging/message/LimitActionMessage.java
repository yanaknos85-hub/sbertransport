package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @deprecated Используйте ru.sber.transport:limit-messaging
 */
@Deprecated(since = "2023-02-27")
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LimitActionMessage {
    
    /**
     * Identifier
     */
    private UUID organizationId;
    
    /**
     * Действие.
     */
    private String action;
    
    /**
     * Limit owner
     */
    private UUID departmentId;
    
    /**
     * Limit owner
     */
    private UUID employeeId;
    
    /**
     * Limit sharing type
     */
    private String transportType;
    
    /**
     * Sum
     */
    private Long sum;
    
    /**
     * Bonus sum
     */
    private Long bonusSum;
    
    /**
     * Money saved (coop trip)
     */
    private Integer moneySaved;
    
    /**
     * Date and time or request creation
     */
    private LocalDateTime plannedDate;
    
    /**
     * Limit limit
     */
    private UUID requestId;
    
    /**
     * Check limits for user;
     */
    private Boolean checkLimit;
    
    
    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;
    
    /**
     * Признак совместной поездки
     */
    @Builder.Default
    private boolean coop = false;
    
    /**
     * Признак водителя
     */
    @Builder.Default
    private boolean driver = false;
}
