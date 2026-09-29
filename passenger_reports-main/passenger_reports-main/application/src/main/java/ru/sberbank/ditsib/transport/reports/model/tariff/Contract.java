package ru.sberbank.ditsib.transport.reports.model.tariff;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.model.Contractor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Базовая сущность контракта
 */
@Entity
@Table(schema = "reports", name = "contract")
@Builder(toBuilder = true)
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
public class Contract {
    
    /**
     * идентификатор
     */
    @Id
    private UUID id;
    
    /**
     * Контрагент
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id")
    private Contractor contractor;
    
    /**
     * Флаг активности.
     */
    @Column
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Вид транспортной услуги.
     */
    @Column(name = "service_type", nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    
    /**
     * Тип транспорта.
     */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;
    
    /**
     * Сумма.
     */
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
    
    /**
     * Автор записи
     */
    @Column(name = "user_id")
    private UUID userId;
    
    /**
     * Время создания.
     */
    @NotNull
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    /**
     * Номер договора.
     */
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
    
    /**
     * НДС.
     */
    @Column(name = "vat_value")
    private Integer vatValue;
}
