package ru.sberbank.ditsib.transport.request.database.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;
import ru.sberbank.ditsib.transport.request.database.model.personal.PersonalTariff;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import java.util.UUID;

/**
 * Базовая сущность тарифа
 */
@Entity
@Table(schema = "request", name = "tariff")
@SuperBuilder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "transport_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "transport_type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                name = TransportTypeEnum.Constants.TAXI_STRING,
                value = TaxiTariff.class
        ),
        @JsonSubTypes.Type(
                name = TransportTypeEnum.Constants.CARSHARING_STRING,
                value = CarsharingTariff.class
        ),
        @JsonSubTypes.Type(
                name = TransportTypeEnum.Constants.PERSONAL_STRING,
                value = PersonalTariff.class
        ),
        @JsonSubTypes.Type(
                name = TransportTypeEnum.Constants.PUBLIC_STRING,
                value = PublicTariff.class
        ),
        @JsonSubTypes.Type(
                name = TransportTypeEnum.Constants.GROUP_TRANSFER_STRING,
                value = GroupTransferTariff.class
        )
})
public class BaseTariff {
    
    /**
     * идентификатор.
     */
    @EqualsAndHashCode.Include
    @Id
    private UUID id;
    
    /**
     * человекочитаемый идентификатор.
     */
    @Column(name = "humanreadableid", updatable = false, nullable = false)
    private String humanReadableId;
    
    /**
     * Идентификатор департамента.
     */
    @Column(name = "department_id", nullable = false)
    private UUID departmentId;
    
    /**
     * вид транспортной услуги.
     */
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    /**
     * Организация владелец тарифа.
     */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /**
     * Идентификатор региона.
     */
    @Column(name = "region_id")
    private UUID regionId;
    
    /**
     * Регион.
     *
     * @deprecated устаревшее, перейти на regionId.
     */
    @Deprecated
    @Column(name = "region")
    private String region;
    
    /**
     * тип транспорта.
     */
    @Column(name = "transport_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * Флаг активности сущности.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
    
    /**
     * Получение типа транспорта тарифа.
     *
     * @return тип транспорта тарифа.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public TransportTypeEnum getTransportType() {
        return transportType;
    }
}
