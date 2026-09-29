package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Сущность тарифа общественного транспорта
 */
@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.PUBLIC_STRING)
public class PublicTariff extends BaseTariff {
    
    /**
     * Цена билета на метро, коп.
     */
    @NotNull
    @Min(0)
    @Column(name = "metro_ticket_cost")
    private int metroTicketCost;
    
    /**
     * Цена билета на трамвай, коп.
     */
    @NotNull
    @Min(0)
    @Column(name = "tram_ticket_cost")
    private int tramTicketCost;
    
    /**
     * Цена билета на троллейбус, коп.
     */
    @NotNull
    @Min(0)
    @Column(name = "trolleybus_ticket_cost")
    private int trolleybusTicketCost;
    
    /**
     * Цена билета на автобус, коп.
     */
    @NotNull
    @Min(0)
    @Column(name = "bus_ticket_cost")
    private int busTicketCost;
    
    /**
     * Цена билета на электричку, коп.
     */
    @Column(name = "city_local_train_cost")
    @Builder.Default
    private int cityLocalTrainCost = 0;
    
    /**
     * Доступность метро в регионе.
     */
    @NotNull
    @Column(name = "metro_availability")
    private boolean metroAvailability;
    
    /**
     * Доступность трамвая в регионе.
     */
    @NotNull
    @Column(name = "tram_availability")
    private boolean tramAvailability;
    
    /**
     * Доступность троллейбуса в регионе.
     */
    @NotNull
    @Column(name = "trolleybus_availability")
    private boolean trolleybusAvailability;
    
    /**
     * Доступность автобуса в регионе.
     */
    @NotNull
    @Column(name = "bus_availability")
    private boolean busAvailability;
    
    /**
     * Доступность электрички в регионе.
     */
    @Column(name = "city_local_train_availability")
    @Builder.Default
    private boolean cityLocalTrainAvailability = false;
    
    /**
     * Стоимость проездного на метро, коп.
     */
    @Column(name = "travel_card_metro_cost")
    @Builder.Default
    private int travelCardMetroCost = 0;
    
    /**
     * Стоимость проездного на трамвай, коп.
     */
    @Column(name = "travel_card_tram_cost")
    @Builder.Default
    private int travelCardTramCost = 0;
    
    /**
     * Стоимость проездного на троллейбус, коп.
     */
    @Column(name = "travel_card_trolleybus_cost")
    @Builder.Default
    private int travelCardTrolleybusCost = 0;
    
    /**
     * Стоимость проездного на автобус, коп.
     */
    @Column(name = "travel_card_bus_cost")
    @Builder.Default
    private int travelCardBusCost = 0;
    
    /**
     * Стоимость проездного на электричку, коп.
     */
    @Column(name = "travel_card_local_train_cost")
    @Builder.Default
    private int travelCardLocalTrainCost = 0;
    
    /**
     * Стоимость проездного на метро, коп.
     */
    @Column(name = "travel_card_all_city_transport_cost")
    @Builder.Default
    private int travelCardAllCityTransportCost = 0;
    
    /**
     * Доступность проездного на метро.
     */
    @Column(name = "travel_card_metro_availability")
    @Builder.Default
    private boolean travelCardMetroAvailability = false;
    
    /**
     * Доступность проездного на трамвай.
     */
    @Column(name = "travel_card_tram_availability")
    @Builder.Default
    private boolean travelCardTramAvailability = false;
    
    /**
     * Доступность проездного на троллейбус.
     */
    @Column(name = "travel_card_trolleybus_availability")
    @Builder.Default
    private boolean travelCardTrolleybusAvailability = false;
    
    /**
     * Доступность проездного на автобус.
     */
    @Column(name = "travel_card_bus_availability")
    @Builder.Default
    private boolean travelCardBusAvailability = false;
    
    /**
     * Доступность проездного на электричку.
     */
    @Column(name = "travel_card_local_train_availability")
    @Builder.Default
    private boolean travelCardLocalTrainAvailability = false;
    
    /**
     * Доступность проездного на метро.
     */
    @Column(name = "travel_card_all_city_transport_availability")
    @Builder.Default
    private boolean travelCardAllCityTransportAvailability = false;

}
