package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_public_history")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestForPublic")
public class RequestHistoryElementForPublic extends RequestHistoryElement {
    /**
     * Linked request
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_for_public_id")
    private RequestForPublic requestForPublic;
    
    @PreRemove
    private void removeLinks(){
        requestForPublic.getHistoryItemsForPublic().remove(this);
    }
}

