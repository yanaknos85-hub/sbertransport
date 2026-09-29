package ru.sberbank.ditsib.transport.reports.model.attributes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entity of account.
 */
@Entity
@Table(schema = "reports", name = "user_attributes")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserAttributes {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(name = "user_id")
    private UUID userId;
    
    @Embedded
    private TaxiUIVisibility taxiUIVisibility;
    
    @Embedded
    private PersonalUIVisibility personalUIVisibility;
    
    @Embedded
    private PublicUIVisibility publicUIVisibility;
    
    @Embedded
    private CarsharingUIVisibility carsharingUIVisibility;
}
