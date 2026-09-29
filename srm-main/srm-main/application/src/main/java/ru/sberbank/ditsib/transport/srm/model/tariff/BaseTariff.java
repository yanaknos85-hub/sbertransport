package ru.sberbank.ditsib.transport.srm.model.tariff;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@Table(schema = "srm", name = "tariff")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transport_type", discriminatorType = DiscriminatorType.STRING)
public class BaseTariff {
    
    //идентификатор
    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    private UUID id;
    
    //человекочитаемый идентификатор
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    //вид транспортной услуги
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    @Column(name = "region")
    private String region;
    
    //Организация владелец тарифа
    @Column(name = "organization_id")
    private UUID organizationId;
    
    //тип транспорта
    @Column(name = "transport_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    //Контрагент
    @Column(name = "contract_id")
    private UUID contractId;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
