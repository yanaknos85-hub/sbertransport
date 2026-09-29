package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Getter
@Entity
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@DiscriminatorValue(value = TransportTypeEnum.Constants.PERSONAL_STRING)
public class PersonalTariff extends BaseTariff {
    
    /**
     * Индекс затрат на страхование, руб./км
     */
    @Builder.Default
    @Column(name = "trust_idx")
    private Double trustIdx = 0.0;
}
