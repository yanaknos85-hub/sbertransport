package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentShortDTO;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "department")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="DepartmentShortDTO",
        classes= {
                @ConstructorResult(
                        targetClass = DepartmentShortDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "code"),
                                @ColumnResult(name = "department_name")
                        }
                )
        }
)
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
    
    @Column(name = "code")
    private String code;
}
