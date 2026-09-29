package ru.sberbank.ditsib.transport.request.database.model.corp;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.request.database.model.Approver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(schema = "request", name = "department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Department {
    
    @Id
    private UUID id;
    
    @Column(name = "humanreadableid", nullable = false)
    private String humanReadableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @Column(name = "parent_id")
    private UUID parent;
    
    @Column(nullable = false, name = "department_name")
    private String departmentName;
    
    @Column(name = "department_head")
    private UUID departmentHead;
    
    @Column
    private String location;
    
    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Column
    private boolean active = true;
    
    /**
     * Список согласующих, которые могут согласовать заявки данного подразделения
     */
    @ElementCollection
    @CollectionTable(schema = "request", name = "approvers",
                     joinColumns = @JoinColumn(name = "department_id"))
    @Builder.Default
    private Collection<Approver> approvers = new ArrayList<>();
    
}
