package ru.sberbank.ditsib.transport.reports.model.taxiTrip;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Сущность совместной поездки на такси
 */
@Getter
@Setter
@Entity
@DiscriminatorValue(value = TripType.Constants.COOP_STRING)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class CoopTaxiTrip extends TaxiTrip {
    /**
     * Связанная заявка на совместную поездку
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_ride_id")
    private SharedRide sharedRide;
    
    @Column
    private UUID rideId;
}
