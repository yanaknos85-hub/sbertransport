package ru.sberbank.transport.oto.cargo.database.model.template;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.transport.oto.cargo.database.model.Employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "oto_cargo", name = "template_for_cargo")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateForCargo {
    
    @Id
    private UUID id;
    
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /**
     * Creator of request
     */
    @ManyToOne
    @JoinColumn(name = "author_id")
    @ToString.Exclude
    private Employee author;
    
    @Column(name = "transport_type")
    private String transportType;
    
    @Column(name = "creation_time")
    private LocalDateTime creationTime;
    
    @Column(name = "cron_expression")
    private String cronExpression;
    
    /**
     * Имя отправителя
     */
    @Column(name = "sender_name")
    private String senderName;
    
    /**
     * Адрес получателя
     */
    @Column(name = "recipient_address")
    private String recipientAddress;
    
    /**
     * Адрес отправителя
     */
    @Column(name = "sender_address")
    private String senderAddress;
    
    /**
     * Имя получателя
     */
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Enumerated(EnumType.STRING)
    @Column
    private TripRequestStatus status;
    
    @Column(columnDefinition = "jsonb", name = "requests_date_delivery")
    @Type(JsonBinaryType.class)
    private List<LocalDateTime> requestsDateDelivery;
    
    @Column(columnDefinition = "jsonb", name = "template")
    @Type(JsonBinaryType.class)
    private TemplateForCargoMessage.TemplateInfo template;
    
    @Column(name = "total_cost")
    private Long totalCost;
    
    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "count_requests")
    private Integer countRequests;

    @Column(name = "count_requests_in_route")
    private Integer countRequestsInRoute;
}
