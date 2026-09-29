package ru.sberbank.ditsib.transport.request.database.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TripType;

/**
 * Сущность одиночной поездки на такси
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue(value = TripType.Constants.SINGLE_STRING)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class SingleTaxiTrip extends TaxiTrip {

}
