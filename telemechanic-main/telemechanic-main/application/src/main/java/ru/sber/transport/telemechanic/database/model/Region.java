package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Регион
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "region")
public class Region {
    
    /**
     * Идентификатор региона
     */
    @Id
    @GeneratedValue
    private UUID id;
    
    /**
     * Код региона
     */
    @NotBlank
    @Size(max = 3)
    private String code;
    
    /**
     * Название региона
     */
    @NotBlank
    @Size(max = 255)
    private String name;
    
}
