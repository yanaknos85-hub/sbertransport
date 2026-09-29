package ru.sberbank.ditsib.transport.reports.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationShortDTO;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Data
@Builder
@Table(schema = "reports", name = "organization")
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="OrganizationShortDTO",
        classes={
                @ConstructorResult(
                        targetClass = OrganizationShortDTO.class,
                        columns= {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "official_name")
                        }
                )
        }
)
/**
 * Модель организации(корпоративные клиенты)
 */
public class Organization {

    public Organization(UUID id) {
        this.id = id;
    }

    /**
     * ID организации
     */
    @Id
    private UUID id;

    /**
     * Имя организации
     */
    @Column(name = "official_name")
    private String officialName;


    /**
     * Адрес организации
     */
    @Column
    private String address;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_group_id")
    private OrganizationGroup organizationGroup;
}
