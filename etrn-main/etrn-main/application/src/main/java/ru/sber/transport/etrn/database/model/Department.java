package ru.sber.transport.etrn.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "department", schema = "etrn_cargo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    private UUID id;

    @Column(name = "department_name", length = 255)
    private String departmentName;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "humanreadableid", length = 128)
    private String humanReadableId;

    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Column
    private boolean active = true;
}