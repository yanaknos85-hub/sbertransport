package ru.sberbank.ditsib.transport.reports.model;


import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "transport_compensation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name = "TransportCompensationDTO",
        classes = {
                @ConstructorResult(
                        targetClass = TransportCompensationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "compensation_type", type = String.class),
                                @ColumnResult(name = "transport_type", type = String.class),
                                @ColumnResult(name = "tickets_cost", type = Integer.class),
                                @ColumnResult(name = "tickets_count", type = Integer.class),
                                @ColumnResult(name = "tickets_expiration_start", type = LocalDate.class),
                                @ColumnResult(name = "tickets_expiration_end", type = LocalDate.class),
                                @ColumnResult(name = "request_id", type = UUID.class),
                                @ColumnResult(name = "attached_document_id", type = UUID.class)
                        }
                )
        }
)
public class TransportCompensation {
    
    @Id
    private UUID id;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private Request request;
    
    /**
     * Тип компенсации
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "compensation_type", nullable = false)
    private PublicCompensationType compensationType;
    
    /**
     * Тип транспорта
     */
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private PublicTransportType transportType;
    
    /**
     * Цена билета, коп
     */
    @NotNull
    @Min(0)
    @Column(name = "tickets_cost", columnDefinition = "varchar (Types#VARCHAR)")
    private Integer ticketsCost;
    
    /**
     * Количество билетов
     */
    @NotNull
    @Min(1)
    @Column(name = "tickets_count", columnDefinition = "varchar (Types#VARCHAR)")
    @Builder.Default
    private Integer ticketsCount = 1;
    
    /**
     * Дата начала действия билета
     */
    @Column(name = "tickets_expiration_start")
    private LocalDate ticketsExpirationStart;
    
    /**
     * Дата окончания действия билета
     */
    @Column(name = "tickets_expiration_end")
    private LocalDate ticketsExpirationEnd;
    
    /**
     * Идентификатор документа
     */
    @Column(name = "attached_document_id")
    private UUID attachedDocumentId;
}
