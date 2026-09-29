package ru.sber.transport.notifications.database.model.coprorate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "notifications_corporate", name = "department")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Department {
    
    @Id
    private UUID id;
    
    @Column(name = "department_head_id")
    private UUID departmentHeadId;
  
    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;
}
