package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_carsharing_history")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestForCarsharing")
public class RequestHistoryElementForCarsharing extends RequestHistoryElement {
    /**
     * Linked request
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_for_carsharing_id")
    private RequestForCarsharing requestForCarsharing;
    
    @PreRemove
    private void removeLinks(){
        requestForCarsharing.getHistoryItemsForCarsharing().remove(this);
    }
    
}

