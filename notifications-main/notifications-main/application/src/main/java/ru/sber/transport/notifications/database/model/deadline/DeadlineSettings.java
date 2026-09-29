package ru.sber.transport.notifications.database.model.deadline;

import lombok.*;

import jakarta.persistence.*;
import java.util.UUID;

/** Сущность - настройки контрольных сроков для организации, получаемые из сообщения */
@Entity
@Table(schema = "notifications_corporate", name = "deadline_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineSettings {
    
    /** ID настройки */
    @Id
    private UUID id;
    
    /** ID корп.клиента. Одна настройка для одного корп.клиента */
    @Column(name = "organization_id")
    private UUID organizationId;
    
    /** Контрольный срок - такси - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "taxi_awaiting_approvals_deadline_id")
    private RequestDeadlineSettingsItem taxiAwaitingApprovalDeadline;
    
    /** Контрольный срок - такси - ожидайте назначения водителя */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "taxi_driver_search_deadline_id")
    private RequestDeadlineSettingsItem taxiAwaitingSearchDeadline;
    
    /** Контрольный срок - такси - поездка завершена */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "taxi_trip_finished_deadline_id")
    private RequestDeadlineSettingsItem taxiTripFinishedDeadline;
    
    /** Контрольный срок - личный а/м - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_awaiting_approvals_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingApprovalDeadline;
    
    /** Контрольный срок - личный а/м - согласование присоединения к СП */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_awaiting_shared_ride_approval_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingSharedRideApprovalDeadline;
    
    /** Контрольный срок - личный а/м - поездка */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_trip_in_progress_deadline_id")
    private RequestDeadlineSettingsItem personalTripInProgressDeadline;
    
    /** Контрольный срок - личный а/м - утверждение маршрута */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "personal_awaiting_trip_approval_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingTripApprovalDeadline;
    
    /** Контрольный срок - общественный тр-т - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "public_awaiting_approval_deadline_id")
    private RequestDeadlineSettingsItem publicAwaitingApprovalDeadline;
    
    /** Контрольный срок - общественный тр-т - подтверждение поездки */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "public_trip_confirmation_deadline_id")
    private RequestDeadlineSettingsItem publicTripConfirmationDeadline;
    
    /** Контрольный срок - общественный тр-т - утверждение */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "public_awaiting_affirmative_deadline_id")
    private RequestDeadlineSettingsItem publicAwaitingAffirmativeDeadline;
    
    /** Контрольный срок - личный лимит - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_limit_deadline_id")
    private LimitDeadlineSettingsItem employeeLimitDeadline;
    
    /** Контрольный срок - лимит подразделения - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "department_limit_deadline_id")
    private LimitDeadlineSettingsItem departmentLimitDeadline;
    
    /** Контрольный срок - заявка на подключение к каршерингу - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "carsharing_join_deadline_id")
    private CarsharingJoinDeadlineSettingsItem carsharingJoinDeadline;
}
