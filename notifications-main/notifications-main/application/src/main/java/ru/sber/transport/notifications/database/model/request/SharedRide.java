package ru.sber.transport.notifications.database.model.request;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import java.util.*;

@Entity
@Table(schema = "notifications_request", name = "shared_trip")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRide {
    
    /**
     * Идентификатор, формируется на стороне SRM
     */
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * Количество пассажиров
     */
    @Min(value = 1, message = "Number of passengers must be greater than 0")
    @Column(name = "passengers")
    private int passengers;
    
    /**
     * Идентификатор тарифа
     */
    @Column(name = "tariff_id")
    private UUID tariffId;
    
    /**
     * Флаг активности поездки. false -  удалена/отменена
     */
    @Column(name = "active")
    @Builder.Default
    private boolean active = true;
    
    @Transient
    @Builder.Default
    private Set<UUID> requestIds = new HashSet<>();
    
}
