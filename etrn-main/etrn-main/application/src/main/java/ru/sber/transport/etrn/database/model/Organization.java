package ru.sber.transport.etrn.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(schema = "etrn_cargo", name = "organization")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Organization {

    @Id
    @Setter
    private UUID id;

    @Column(name = "digit_id", columnDefinition = "numeric")
    private Long digitId;

    @Column(name = "official_name")
    private String officialName;

    @Column
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_group_id")
    private OrganizationGroup organizationGroup;

    /**
     * Флаг активности(false - удален, true - активен)
     */
    @Builder.Default
    @Setter
    @Column
    private boolean active = true;
}