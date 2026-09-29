package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Getter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.PUBLIC_STRING)
public class PublicTariff extends BaseTariff {

}
