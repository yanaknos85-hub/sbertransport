package ru.sberbank.ditsib.transport.tariff.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Getter
@Entity
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.WALK_STRING)
public class WalkTariff extends BaseTariff {
}
