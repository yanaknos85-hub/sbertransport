package ru.sberbank.ditsib.transport.reports.model.cargo;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.reports.model.Employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(schema = "reports", name = "request_for_cargo")
@Data
@SuperBuilder
@NoArgsConstructor
@Slf4j
public class RequestForCargo {
    
    /**
     * Идентификатор заявки
     */
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * Human readable id
     */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /**
     * Type of transport used for request
     */
    @Column(name = "transport_type")
    private String transportType;
    
    /**
     * Отправитель
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "senderId", nullable = false)
    private Employee sender;
    
    /**
     * Получатель
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "recipientId", nullable = false)
    private Employee recipient;
    
    /**
     * Планируемая дата и время поездки.
     */
    @Column(name = "desired_date", nullable = false)
    private LocalDateTime desiredDate;
    
    /**
     * Организация отправитель
     */
    @Column(name = "sender_organization")
    private String senderOrganization;
    
    /**
     * Организация получатель
     */
    @Column(name = "recipient_organization")
    private String recipientOrganization;
    
    /**
     * Общая длина груза
     */
    @Column(name = "length")
    private Double length;
    
    /**
     * Общая ширина груза
     */
    @Column(name = "width")
    private Double width;
    
    /**
     * Общая высота груза
     */
    @Column(name = "height")
    private Double height;
    
    /**
     * Общий объем груза
     */
    @Column(name = "volume")
    private Double volume;
    
    /**
     * Общий вес груза
     */
    @Column(name = "weight")
    private Double weight;
    
    /**
     * Общее количество занятых мест
     */
    @Column(name = "occupied_places_count")
    private Integer occupiedPlacesCount;
    
    /**
     * Комментарий к заявке
     */
    @Column(name = "comment")
    private String comment;
    
    /**
     * Список параметров грузов
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "request", fetch = FetchType.EAGER)
    @Builder.Default
    private final List<CargoDetail> cargoDetails = new ArrayList<>();
    
    @NotNull
    @Column
    @Builder.Default
    private boolean active = true;
}

