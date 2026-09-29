package ru.sber.transport.notifications.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(schema = "notifications_request", name = "trip_purpose")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class TripPurpose {
    
    @Id
    @NonNull
    private UUID id;
    
    @Column
    private String purpose;
    
    @Column(name = "organization_id")
    private UUID organizationId;
    
}
