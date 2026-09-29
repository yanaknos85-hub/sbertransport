package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.TariffShortDTO;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@Table(schema = "reports", name = "tariff")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transport_type", discriminatorType = DiscriminatorType.STRING)
@DynamicUpdate
@DynamicInsert
@SqlResultSetMapping(
        name="TariffShortDTO",
        classes={
                @ConstructorResult(
                        targetClass = TariffShortDTO.class,
                        columns= {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "humanreadableid"),
                                @ColumnResult(name = "work_group")
                        }
                )
        }
)
public abstract class BaseTariff {
    
    //идентификатор
    @EqualsAndHashCode.Include
    @Id
    @Column(name = "id")
    private UUID id;
    
    //человекочитаемый идентификатор
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    //вид транспортной услуги
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private final TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    @Column(name = "region")
    private String region;
    
    //Организация владелец тарифа
    private UUID organizationId;
    
    //тип транспорта
    @Column(name = "transport_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    //Договор
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;
    
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
