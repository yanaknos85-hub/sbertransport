package ru.sber.transport.authentication.web.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Object with data about roles.
 */
@Getter
@Builder
@AllArgsConstructor
public class Role {
    
    /**
     * Code.
     */
    private final String code;
    
    /**
     * Code.
     */
    private final String name;
    
    /**
     * Code.
     */
    private final String description;
    
    /**
     * Flag of default role.
     */
    @Builder.Default
    private final boolean isDefault = false;
    
}
