package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(schema = "request", name = "request_for_personal_history")
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "requestForPersonal")

public class RequestHistoryElementForPersonal extends RequestHistoryElement {
    /**
     * Linked request
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_for_personal_id")
    
    private RequestForPersonal requestForPersonal;
    
    @PreRemove
    private void removeLinks(){
        requestForPersonal.getHistoryItemsForPersonal().remove(this);
    }
}

