package ru.sber.transport.telemechanic.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "medic_contractor")
public class MedicContractor {
    
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * ФИО
     */
    @NotNull
    @Size(max = 255)
    private String fullName;
    
    /**
     * Табельный номер
     */
    @NotNull
    @Size(max = 255)
    private String personnelNumber;
    
    /**
     * Наименование организации
     */
    @NotNull
    @Size(max = 255)
    private String organization;
    
    /**
     * Наименование подразделения
     */
    @NotNull
    @Size(max = 255)
    private String department;
    
    /**
     * Наименование должности
     */
    @NotNull
    @Size(max = 255)
    private String position;
    
    /**
     * Номер ключа электронной подписи
     */
    @NotNull
    @Size(max = 255)
    private String signKeyNumber;
    
    /**
     * Дата окончания действия ключа электронной подписи
     */
    @NotNull
    private LocalDateTime signKeyEndDateTime;
}
