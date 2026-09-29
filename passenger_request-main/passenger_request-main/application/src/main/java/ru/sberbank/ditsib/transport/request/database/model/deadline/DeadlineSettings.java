package ru.sberbank.ditsib.transport.request.database.model.deadline;

import jakarta.persistence.*;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.List;
import java.util.UUID;

/** Сущность - настройки контрольных сроков для организации, получаемые из сообщения */
@Entity
@Table(schema = "request", name = "deadline_settings_message")
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
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "taxi_awaiting_approvals_deadline_id")
    private RequestDeadlineSettingsItem taxiAwaitingApprovalDeadline;
    
    /** Контрольный срок - такси - ожидайте назначения водителя */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "taxi_awaiting_search_deadline_id")
    private RequestDeadlineSettingsItem taxiAwaitingSearchDeadline;
    
    /** Контрольный срок - такси - поездка завершена */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "taxi_trip_finished_deadline_id")
    private RequestDeadlineSettingsItem taxiTripFinishedDeadline;
    
    /** Контрольный срок - личный а/м - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_awaiting_approvals_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingApprovalDeadline;
    
    /** Контрольный срок - личный а/м - согласование присоединения к СП */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_awaiting_shared_ride_approval_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingSharedRideApprovalDeadline;
    
    /** Контрольный срок - личный а/м - поездка */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_trip_in_progress_deadline_id")
    private RequestDeadlineSettingsItem personalTripInProgressDeadline;
    
    /** Контрольный срок - личный а/м - утверждение маршрута */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_awaiting_trip_approval_deadline_id")
    private RequestDeadlineSettingsItem personalAwaitingTripApprovalDeadline;
    
    /** Контрольный срок - личный а/м - формирование приказа на выплату */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_order_payment_formation_deadline_id")
    private RequestDeadlineSettingsItem personalOrderPaymentFormationDeadline;
    
    /** Контрольный срок - личный а/м - ожидание выплаты */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "personal_payment_awaiting_deadline_id")
    private RequestDeadlineSettingsItem personalPaymentAwaitingDeadline;
    
    /** Контрольный срок - общественный тр-т - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "public_awaiting_approval_deadline_id")
    private RequestDeadlineSettingsItem publicAwaitingApprovalDeadline;
    
    /** Контрольный срок - общественный тр-т - подтверждение поездки */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "public_trip_confirmation_deadline_id")
    private RequestDeadlineSettingsItem publicTripConfirmationDeadline;
    
    /** Контрольный срок - общественный тр-т - утверждение */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "public_awaiting_affirmative_deadline_id")
    private RequestDeadlineSettingsItem publicAwaitingAffirmativeDeadline;
    
    /** Контрольный срок - общественный тр-т - формирование приказа на выплату */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "public_order_payment_formation_deadline_id")
    private RequestDeadlineSettingsItem publicOrderPaymentFormationDeadline;
    
    /** Контрольный срок - общественный тр-т - ожидание выплаты */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "public_payment_awaiting_deadline_id")
    private RequestDeadlineSettingsItem publicPaymentAwaitingDeadline;
    
    /** Контрольный срок - заявка на подключение к каршерингу - на согласовании */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "carsharing_join_deadline_id")
    private CarsharingJoinDeadlineSettingsItem carsharingJoinDeadline;
    
    /**
     * Вернуть все элементы настроек
     * @return все элементы настроек
     */
    public List<? extends DeadlineSettingsItem> getAllItems() {
        return List.of(taxiAwaitingApprovalDeadline, taxiAwaitingSearchDeadline, taxiTripFinishedDeadline,
                       personalAwaitingApprovalDeadline, personalAwaitingSharedRideApprovalDeadline,
                       personalTripInProgressDeadline, personalAwaitingTripApprovalDeadline,
                       personalOrderPaymentFormationDeadline, personalPaymentAwaitingDeadline,
                       publicAwaitingApprovalDeadline, publicTripConfirmationDeadline,
                       publicAwaitingAffirmativeDeadline, publicOrderPaymentFormationDeadline,
                       publicPaymentAwaitingDeadline, carsharingJoinDeadline);
    }
    
    /**
     * Вернуть конкретный элемент настройки КС для заявок на поездки
     * @param transportType тип транспорта
     * @param status статус заявки
     * @return RequestDeadlineSettingsItem
     */
    public RequestDeadlineSettingsItem getRequestItem(TransportTypeEnum transportType, TripRequestStatus status) {
        return List.of(taxiAwaitingApprovalDeadline, taxiAwaitingSearchDeadline, taxiTripFinishedDeadline,
                       personalAwaitingApprovalDeadline, personalAwaitingSharedRideApprovalDeadline,
                       personalTripInProgressDeadline, personalAwaitingTripApprovalDeadline,
                       personalOrderPaymentFormationDeadline, personalPaymentAwaitingDeadline,
                       publicAwaitingApprovalDeadline, publicTripConfirmationDeadline,
                       publicAwaitingAffirmativeDeadline, publicOrderPaymentFormationDeadline,
                       publicPaymentAwaitingDeadline)
                   .stream()
                   .filter(reqItem ->
                           reqItem.getTransportType().equals(transportType) && reqItem.getRequestStatus().equals(status))
                   .findFirst()
                .orElse(null);
    }
    
    /**
     * Вернуть конкретный элемент настройки КС для заявок на подключение к корп.каршерингам
     * @param carsharingJoinStatus статус заявки на подключение к корп.каршерингам
     * @return CarsharingJoinDeadlineSettingsItem
     */
    public CarsharingJoinDeadlineSettingsItem getCarsharingJoinItem(CarsharingJoinRequestStatus carsharingJoinStatus) {
        return List.of(carsharingJoinDeadline)
                   .stream()
                   .filter(reqItem -> reqItem.getTransportType().equals(TransportTypeEnum.CARSHARING) &&
                                      reqItem.getCarsharingJoinStatus().equals(carsharingJoinStatus))
                   .findFirst().orElse(null);
    }
}
