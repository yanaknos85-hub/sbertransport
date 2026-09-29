package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Message-entity with information about contractor.
 */
@Entity
@Getter
@Setter
@Table(schema = "tariff", name = "contractor")
public class Contractor {
    
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "tin")
    private String tin;
    
    @Column(name = "msrn")
    private String msrn;
    
    @ElementCollection
    @CollectionTable(schema = "tariff", name = "contractor_region", joinColumns = @JoinColumn(name = "contractor_id"))
    @Column(name = "region_id")
    private List<UUID> regionIds = new ArrayList<>();
    
    @Column(name = "integration_email")
    private String integrationEmail;
    
    @NotNull
    @Column(name = "integration_type")
    private String integrationType;
    
    @Column(name = "active")
    private boolean active;
    
    @Column
    private String login;
    
    @Column
    private String password;
}
