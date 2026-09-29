package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Доверенность
 */
@Entity
@Table(name = "attorney")
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class Attorney {
    
    /**
     * Идентификатор записи о доверенности
     */
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Номер доверенности
     */
    @NotNull
    private UUID number;
    
    /**
     * Дата начала действия доверенности
     */
    @NotNull
    private LocalDate issueDate;
    
    /**
     * Дата окончания действия доверенности
     */
    @NotNull
    private LocalDate expiryDate;
    
    /**
     * Система хранящая данные о доверенности
     */
    @NotBlank
    @Size(max = 150)
    private String creationSystem;
}
