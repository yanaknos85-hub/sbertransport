package ru.sber.transport.dispatcher.database.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Entity of contractor.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(schema = "dispatcher", name = "contractor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contractor {

    /**
     * Идентификатор контрагента
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Цифровой идентификатор, используется для генерации человекочитаемого идентификатора
     */
    @org.hibernate.annotations.Generated
    @Column(name = "digit_id", insertable = false, updatable = false)
    private Long digitId;

    @Column(name = "employee_count")
    private int employeeCount;

    /**
     * Имя контрагента
     */
    @Column(nullable = false)
    private String name;

    /**
     * ИНН контрагента
     */
    @Column(nullable = false)
    private String tin;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_dispatcher_id")
    private Dispatcher mainDispatcher;

    @Column
    @Builder.Default
    boolean autoassign = false;

    @Column
    @Builder.Default
    private boolean active = true;

    @Column(name = "technical_account_owner_email")
    private String technicalAccountOwnerEmail;

    @Column(name = "technical_account_owner")
    private String technicalAccountOwner;

    /**
     * Флаг необходимости запроса штрафов.
     */
    @Column(name = "is_fine_fetch_required")
    @Builder.Default
    private boolean isFineFetchRequired = false;

    /**
     * ОГРН
     */
    @Column(nullable = false)
    private String msrn;

    @Column(name = "vehicle_count_norm")
    private Integer vehicleCountNorm;

    /**
     * Признак внутреннего автопарка
     */
    @Column(name = "is_internal", nullable = false)
    private boolean isInternal;

}
