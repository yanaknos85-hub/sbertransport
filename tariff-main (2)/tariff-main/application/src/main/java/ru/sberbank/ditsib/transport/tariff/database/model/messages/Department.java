package ru.sberbank.ditsib.transport.tariff.database.model.messages;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "tariff", name = "message_department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
/**
 * Модель департамента
 */
public class Department {
    
    /**
     * ID департамента
     */
    @Id
    private UUID id;
    
    /**
     * Id родительского департамента
     */
    @Column(name = "parent_id")
    private UUID parentId;
    
    /**
     * Id организации
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Человекочитаемый id
     */
    @Column(name = "human_readable_id")
    private String humanReadableId;
    
    /**
     * Имя департамента
     */
    @Column(name = "department_name")
    private String departmentName;
    
    /**
     * Код департамента
     */
    @Column(name = "code")
    private String code;
}
