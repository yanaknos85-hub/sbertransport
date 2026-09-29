package ru.sberbank.ditsib.transport.reports.model;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.dto.taxi.LimitResultSet;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Модель лимита
 */

@Entity
@Table(schema = "reports", name = "limit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@DynamicUpdate
@DynamicInsert

@SqlResultSetMapping(
        name="LimitResultSet",
        classes= {
                @ConstructorResult(
                        targetClass = LimitResultSet.class,
                        columns = {
                                @ColumnResult(name = "id", type = UUID.class),
                                @ColumnResult(name = "human_readable_id"),
                                @ColumnResult(name = "department_id", type = UUID.class),
                        }
                )
        }
)

public class Limit {

    /**
     * Идентификатор лимита.
     */
    @Id
    private UUID id;

    /**
     * Идентификатор лимита департамента.
     */
    @Column(name="DEPARTMENT_LIMIT_ID")
    private UUID departmentLimitId;

    /**
     * Тип лимита DEPARTMENT\EMPLOYEE
     */
    @Column(name = "limit_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private LimitType limitType;

    /**
     * Человекочитаемый идентификатор
     */
    @Column(name = "human_readable_id", nullable = false)
    private String humanReadableId;

    /**
     * Статус лимита PLANNING\SHARED\CLOSED
     */
    @Column(name = "limit_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LimitStatus limitStatus;


    /**
     * Год лимита
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * Тип распределения лимита MONTHLY\QUARTER\PERCENTS
     */
    @Column(name = "limit_sharing_type")
    @Enumerated(EnumType.STRING)
    private LimitSharingType limitSharingType;

    /**
     * Тип обслуживания PASSENGER/CARGO
     */
    @Column(name = "limit_service_type")
    @Enumerated(EnumType.STRING)
    private LimitServiceType limitServiceType;

    /**
     * Тип обслуживания PASSENGER/CARGO
     */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;

    /**
     * Общая годовая сумма лимита
     */
    @Column(name = "sum", nullable = false)
    private Long sum;

    /**
     * Баланс лимита(Для типов транспорта)
     */
    @Column
    private Long balance;

    /**
     * Резерв лимита(Для подразделений)
     */
    @Column
    private Long reserve;

    /**
     * Дата создания лимита
     */
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;

    /**
     * Идентификатор организации владеющей лимитом.
     */
    @Column(name = "organization_id")
    private UUID organizationId;

    /**
     * Идентификатор подразделения владеющего лимитом.
     */
    @Column(name = "department_id")
    private UUID departmentId;

    /**
     * Родительское подразделение лимита.
     */
    @Column(name = "parent_department_id")
    private UUID parentDepartmentId;

    /**
     * Идентификатор родительского лимита
     */
    @Column(name = "parent_id")
    private UUID parentId;
    
    /**
     * Признак активности лимита.
     */
    @Column(name = "active")
    private boolean active = true;

}
