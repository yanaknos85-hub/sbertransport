package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import ru.sberbank.ditsib.transport.constants.CarsharingClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTrip;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NamedEntityGraph(
        name = "requestForCarsharingWithHistory",
        attributeNodes = {
                @NamedAttributeNode("historyItemsForCarsharing")
        }
)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_carsharing")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Slf4j
@SQLDelete(sql = "UPDATE request_for_carsharing SET active = false WHERE id = ?")
@SQLRestriction("active=true")
public class RequestForCarsharing extends AbstractRequestForTnPnC {
    
    @OneToOne(mappedBy = "request", fetch = FetchType.LAZY)
    private CarsharingTrip trip;
    
    /**
     * Класс автомобиля
     */
    @Column(name = "carsharing_class")
    @Enumerated(EnumType.STRING)
    private CarsharingClass carsharingClass;
    
    /**
     * Каршеринговая компания
     */
    @Column(name = "contractor_id")
    private UUID contractorId;
    
    
    /**
     * Коментарий при отмене инженером
     */
    @Column(name = "status_comment")
    private String statusComment;
    
    /**
     * Номер телефона, который использовался при создании заявки
     */
    @Column
    private String phoneNumber;
    
    @Embedded
    private RequestRating requestRating;
    
    /**
     * History of status changes
     */
    @OneToMany(mappedBy = "requestForCarsharing", cascade = CascadeType.ALL, orphanRemoval = true, fetch =
            FetchType.LAZY)
    @Builder.Default
    private final List<RequestHistoryElementForCarsharing> historyItemsForCarsharing = new ArrayList<>();
    
    @PrePersist
    private void addLinks() {
        if (getWaypoints() != null) {
            getWaypoints().forEach(wp -> wp.setRequest(this));
        }
        if (getHistoryItemsForCarsharing() != null) {
            getHistoryItemsForCarsharing().forEach(item -> item.setRequestForCarsharing(this));
        }
    }
    
    @PreRemove
    private void removeLinkedEntities() {
        this.historyItemsForCarsharing.clear();
    }
}
