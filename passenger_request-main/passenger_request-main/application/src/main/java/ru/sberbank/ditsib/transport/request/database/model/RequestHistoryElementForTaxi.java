package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_taxi_history")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestForTaxi")
public class RequestHistoryElementForTaxi extends RequestHistoryElement {
    /**
     * Linked request
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_for_taxi_id")
    private RequestForTaxi requestForTaxi;
    
    @PreRemove
    private void removeLinks(){
        requestForTaxi.getHistoryItemsForTaxi().remove(this);
    }
}

