package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

/**
 * Department message.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentMessage implements Message<UUID> {
    
    /**
     * ID of department.
     */
    private UUID id;
    
    /**
     * Human readable Id.
     */
    private String humanReadableId;
    
    /**
     * id of parent organization
     */
    private UUID organizationId;
    
    /**
     * Unique code
     */
    private String code;
    
    /**
     * Name of department
     */
    private String departmentName;
    
    /**
     * Id of parent department
     */
    private UUID parentId;
    
    /**
     * Id of department head
     */
    private UUID departmentHeadId;
    
    /**
     * Location of department
     */
    private String location;
    
    
    /**
     * Flag of department deleted.
     */
    @Builder.Default
    private boolean deleted = false;
    
}
