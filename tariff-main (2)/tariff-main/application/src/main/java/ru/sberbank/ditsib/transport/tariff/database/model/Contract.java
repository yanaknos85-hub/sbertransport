package ru.sberbank.ditsib.transport.tariff.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Базовая сущность контракта
 */
@Entity
@Table(schema = "tariff", name = "contract")
@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Contract {

    public Contract(UUID id) {
        this.id = id;
    }

    // идентификатор
    @Id
    @GeneratedValue
    private UUID id;

    // поля общие с тарифом

    // вид транспортной услуги
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "tariff", name = "contract_region", joinColumns = @JoinColumn(name = "contract_id"))
    @Column(name = "region_id")
    @Builder.Default
    private Set<UUID> regionIds = new HashSet<>();

    // регион
    @Deprecated
    @Column
    private String region;

    // тип транспорта
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;

    // поля уникальные для контракта

    // Контрагент
    @Column(name = "contractor_id")
    private UUID contractorId;

    // Сумма
    @NotNull
    @Column(nullable = false)
    private Long sum;

    /**
     * Дата начала.
     */
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Дата конца.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // служебные поля

    // Автор записи
    @Column(name = "user_id")
    private UUID userId;

    /**
     * Время создания.
     */
    @NotNull
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;

    /**
     * Флаг активности.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    // номер контракта
    @NotBlank
    @Column(name = "contract_number", nullable = false)
    private String contractNumber;

    /**
     * Номер договора УВХД.
     */
    @Column(name = "uvhd")
    private String uvhd;

    /**
     * Флаг наличия НДС.
     */
    @NotNull
    @Builder.Default
    @Column(name = "include_vat", nullable = false)
    private boolean includeVat = false;

    // НДС
    @Column(name = "vat_value")
    private Integer vatValue;

    // Организации
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
    @JoinTable(name = "contract_organizations",
            schema = "tariff",
            joinColumns = @JoinColumn(name = "contract_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "organization_id", nullable = false)
    )
    @Builder.Default
    private Set<Organization> organizations = new HashSet<>();

    /**
     * Тип договора.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type")
    private ContractType contractType;

    /**
     * Тип ограничения связи.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type")
    private RestrictionType restrictionType;

    /**
     * Штраф за опоздание водителей ко времени подачи
     */
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal driverLatePickupPenalty = BigDecimal.valueOf(0.1);

    /**
     * Штраф за ненадлежащее качество услуг
     */
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal poorServiceQualityPenalty = BigDecimal.valueOf(0.1);

    /**
     * Штраф за отмену заявок водителями
     */
    @Builder.Default
    @Column(nullable = false)
    private BigDecimal driverOrderCancellationPenalty = BigDecimal.valueOf(0.1);

    /**
     * Id ответственного за договор
     */
    @Column(nullable = false)
    private UUID responsibleEmployeeId;

    /**
     * Id организации, которая оплачивает договор
     */
    @Column
    private UUID paymentOrganizationId;
}
