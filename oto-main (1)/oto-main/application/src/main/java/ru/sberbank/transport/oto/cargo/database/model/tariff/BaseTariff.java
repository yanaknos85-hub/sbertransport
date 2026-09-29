package ru.sberbank.transport.oto.cargo.database.model.tariff;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.transport.oto.cargo.database.model.Organization;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@Table(schema = "oto_cargo", name = "tariff")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transport_type", discriminatorType = DiscriminatorType.STRING)
public abstract class BaseTariff {
    
    /**
     * идентификатор
     */
    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    private UUID id;
    
    /**
     * человекочитаемый идентификатор
     */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /**
     * вид транспортной услуги
     */
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    @Column(name = "region")
    private String region;
    
    /**
     * Организация владелец тарифа
     */
    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    /**
     * тип транспорта
     */
    @Column(name = "transport_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * Договор
     */
    @OneToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
