package ru.sberbank.ditsib.transport.request.database.model.publicTransport;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import ru.sberbank.ditsib.transport.request.config.publicTransport.CustomRevisionListener;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(schema = "request_audit", name = "request_for_public_revinfo")
@RevisionEntity(CustomRevisionListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomRevision {
    
    @RevisionNumber
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(columnDefinition = "serial (Types#INTEGER)")
    private Long rev;
    
    @Temporal(TemporalType.TIMESTAMP)
    @RevisionTimestamp
    private Date timestamp;
    
    @Column(name = "user_id")
    private UUID userId;
}
