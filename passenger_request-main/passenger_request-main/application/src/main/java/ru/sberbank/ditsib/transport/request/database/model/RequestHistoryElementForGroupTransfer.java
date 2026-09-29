package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_group_transfer_history")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestForGroupTransfer")
public class RequestHistoryElementForGroupTransfer extends RequestHistoryElement {
    /**
     * Linked request
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_for_group_transfer_id")
    private RequestForGroupTransfer requestForGroupTransfer;
    
    @PreRemove
    private void removeLinks() {
        requestForGroupTransfer.getHistoryItemsForGroupTransfer().remove(this);
    }
}

