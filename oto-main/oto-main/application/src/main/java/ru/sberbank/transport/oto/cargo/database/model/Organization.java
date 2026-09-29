package ru.sberbank.transport.oto.cargo.database.model;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@Table(schema = "oto_cargo", name = "organization")
@NoArgsConstructor
@AllArgsConstructor
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
