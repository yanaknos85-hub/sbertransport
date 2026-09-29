package ru.sberbank.ditsib.transport.request.database.model.publicTransport;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class PublicTripData {
    
    /**
     * Количество билетов/жетонов на метро
     */
    @Min(0)
    @Builder.Default
    @Column(columnDefinition = "int2")
    private final Integer metroTicketsQuantity = 0;
    
    /**
     * Количество билетов на трамвай
     */
    @Min(0)
    @Builder.Default
    @Column(columnDefinition = "int2")
    private final Integer tramTicketsQuantity = 0;
    
    /**
     * Количество билетов на троллейбус
     */
    @Min(0)
    @Builder.Default
    @Column(columnDefinition = "int2")
    private final Integer trolleybusTicketsQuantity = 0;
    
    /**
     * Количество билетов на автобус
     */
    @Min(0)
    @Builder.Default
    @Column(columnDefinition = "int2")
    private final Integer busTicketsQuantity = 0;
}
