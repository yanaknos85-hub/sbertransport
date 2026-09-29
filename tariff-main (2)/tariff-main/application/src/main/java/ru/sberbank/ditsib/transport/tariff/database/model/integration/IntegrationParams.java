package ru.sberbank.ditsib.transport.tariff.database.model.integration;


import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Базовая сущность контракта
 */
//@Entity
//@Table(schema = "tariff", name = "integration_params")
@SuperBuilder
@Data
@EqualsAndHashCode(of = "id")
@Jacksonized
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "integration_type", discriminatorType = DiscriminatorType.STRING)
public class IntegrationParams {

    //идентификатор
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Length(min = 3)
    @Column(name = "screen_name")
    private String screenName;

    //тип интеграции
    @Column(name = "integration_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TaxiExternalIntegrationType taxiExternalIntegrationType;


    //Название исполнителя латиницей
    @NotBlank
    @Length(min = 3)
    @Column(name = "performer_name")
    private String performerName;

    //Название исполнителя кириллицей
    @NotBlank
    @Length(min = 3)
    @Column(name = "performer_rus_name")
    private String perfromerRusName;
}
