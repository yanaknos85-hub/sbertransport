package ru.sber.transport.roles.check.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Объект обмена данных с ролями.
 */
@Getter
@Setter
public class UrlAllowDto {
    
    /**
     * Роль.
     */
    private String role;
    
    /**
     * Список допустимых URL.
     */
    private List<String> url = new ArrayList<>();
    
}
