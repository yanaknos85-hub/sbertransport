package ru.sber.transport.authentication.business.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Бизнеи-сущность роли.
 */
@Getter
@Builder
public class RoleDto {
    
    /**
     * Код роли.
     */
    private final String code;
    
    /**
     * Название роли.
     */
    private final String name;
    
    /**
     * Описание роли.
     */
    private final String description;
    
}
