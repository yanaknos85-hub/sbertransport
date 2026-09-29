package ru.sberbank.transport.oto.cargo.database.model;

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
@Table(schema = "oto_cargo", name = "department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
/**
 * Модель департамента
 */
public class Department {

    public Department(UUID id) {
        this.id = id;
    }

    /**
     * ID департамента
     */
    @Id
    private UUID id;

    /**
     * Имя департамента
     */
    @Column(name = "parent_id")
    private UUID parentId;

    /**
     * Имя департамента
     */
    @Column(name = "organization_id")
    private UUID organizationId;

    /**
     * Имя департамента
     */
    @Column(name = "human_readable_id")
    private String humanReadableId;

    /**
     * Имя департамента
     */
    @Column(name = "department_name")
    private String departmentName;
}
