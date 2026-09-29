package ru.sberbank.ditsib.transport.tariff.database.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;

import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@Table(schema = "tariff", name = "tariff")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transport_type", discriminatorType = DiscriminatorType.STRING)
public class BaseTariff {
    
    //идентификатор
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue
    private UUID id;
    
    //человекочитаемый идентификатор
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    //вид транспортной услуги
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    //Организация владелец тарифа
    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    //регион
    /**
     * @deprecated Устаревшее. Используйте `regionId`. Это поле будет удалено через 2 релиза (2021-06-10)
     */
    @Deprecated
    @Column
    private String region;
    
    @Column(name = "region_id")
    private UUID regionId;
    
    //тип транспорта
    @Column(name = "transport_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    // служебные
    
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
