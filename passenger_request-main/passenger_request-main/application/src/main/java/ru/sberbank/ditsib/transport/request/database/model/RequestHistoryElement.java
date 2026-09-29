package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.InitiatorType;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Entity describing request status changes history
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Data
@EqualsAndHashCode(of = "id")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class RequestHistoryElement {
    
    /**
     * Id of request history item
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Date and time or the change
     */
    @Column(name = "change_date", nullable = false)
    
    private LocalDateTime changeDate;
    
    /**
     * Status of request
     */
    @Column(name = "request_status", nullable = false)
    @Enumerated(EnumType.STRING)
    
    private TripRequestStatus status;
    
    /**
     * Code of status
     */
    @Builder.Default
    @Column(name = "code")
    
    private Integer code = 0;
    
    /**
     * Optional message of status change
     */
    @Column(name = "comment")
    
    private String comment;
    
    /*@ManyToOne(optional = false)
    @JoinColumn(name = "initiator_id")
    @IndexedEmbedded(includePaths = "humanReadableId")
    @IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)*/
    @Column(name = "initiator_id")
    private UUID initiator;

    @Column(name = "initiator_description")
    @Enumerated(EnumType.STRING)
    @NotNull
    @Builder.Default
    
    private InitiatorType initiatorDescription = InitiatorType.EMPLOYEE;
    
    @PrePersist
    @PreUpdate
    void insert() {
        changeDate = LocalDateTime.now(ZoneId.of("UTC"));
    }
}
