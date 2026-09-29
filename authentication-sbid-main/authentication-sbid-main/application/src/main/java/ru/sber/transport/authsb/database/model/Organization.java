package ru.sber.transport.authsb.database.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organizations", schema = "authorization_sbid", indexes = {
        @Index(name = "idx_organizations_inn", columnList = "inn"),
        @Index(name = "idx_organizations_ogrn", columnList = "ogrn"),
        @Index(name = "idx_organizations_kpp", columnList = "kpp")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "inn", nullable = false, unique = true, length = 12)
    private String inn;

    @Column(name = "ogrn", length = 15)
    private String ogrn;

    @Column(name = "kpp", length = 9)
    private String kpp;

    @Column(name = "oktmo", length = 11)
    private String oktmo;

    @Column(name = "legal_form_short", length = 10)
    private String legalFormShort;

    @Column(name = "full_name", columnDefinition = "TEXT", nullable = false)
    private String fullName;

    @Column(name = "juridical_address", columnDefinition = "TEXT")
    private String juridicalAddress;

    @Column(name = "actual_address", columnDefinition = "TEXT")
    private String actualAddress;

    @Column(name = "territorial_bank", length = 255)
    private String territorialBank;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "individual_executive_agency", columnDefinition = "TEXT")
    private Integer individualExecutiveAgency;

    @Column(name = "offer_expiration_date")
    private LocalDateTime offerExpirationDate;

    @Column(name = "org_law_form")
    private String orgLawForm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}