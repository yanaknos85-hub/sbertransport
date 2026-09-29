package ru.sberbank.ditsib.transport.request.database.model.publicTransport;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class PublicTariffData {
    
    /**
     * Цена билета на метро, коп
     */
    @NotNull @Min(0)
    @Column(name = "metro_ticket_cost")
    private Integer metroTicketCost;
    
    /**
     * Цена билета на трамвай, коп
     */
    @NotNull @Min(0)
    @Column(name = "tram_ticket_cost")
    private Integer tramTicketCost;
    
    /**
     * Цена билета на троллейбус, коп
     */
    @NotNull @Min(0)
    @Column(name = "trolleybus_ticket_cost")
    private Integer trolleybusTicketCost;
    
    /**
     * Цена билета на автобус, коп
     */
    @NotNull @Min(0)
    @Column(name = "bus_ticket_cost")
    private Integer busTicketCost;
    
    /**
     * Доступность метро в регионе
     */
    @NotNull
    @Column(name = "metro_availability")
    private Boolean metroAvailability;
    
    /**
     * Доступность трамвая в регионе
     */
    @NotNull
    @Column(name = "tram_availability")
    private Boolean tramAvailability;
    
    /**
     * Доступность троллейбуса в регионе
     */
    @NotNull
    @Column(name = "trolleybus_availability")
    private Boolean trolleybusAvailability;
    
    /**
     * Доступность автобуса в регионе
     */
    @NotNull
    @Column(name = "bus_availability")
    private Boolean busAvailability;
}
