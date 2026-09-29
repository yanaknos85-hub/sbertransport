package ru.sberbank.ditsib.transport.request.database.model.carsharing;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(schema = "request", name = "carsharing_external_data")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarsharingExternalData {
    
    @Id
    @GeneratedValue
    UUID id;
    
    @Column(nullable = false)
    UUID organizationId;
    
    @Column(nullable = false)
    UUID contractorId;
    
    @Column(nullable = false)
    Integer extOrganizationId;
    
    @Column(nullable = false)
    Integer extGroupId;
    
}
