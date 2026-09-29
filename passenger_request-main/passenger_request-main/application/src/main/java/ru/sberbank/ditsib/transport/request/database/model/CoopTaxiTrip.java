package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TripType;

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
@ToString(callSuper = true)
public class CoopTaxiTrip extends TaxiTrip {
    
    @Column
    private UUID rideId;
}
