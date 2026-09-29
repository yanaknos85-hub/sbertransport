package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * @deprecated Используйте ru.sber.transport:deadline-messaging
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class DeadlineSettingsMessage implements Message<UUID> {
    
    /** ID настройки */
    private UUID id;
    
    /** Организация. Одна настройка для одной организации */
    private UUID organizationId;
    
    /** Контрольный срок - такси - на согласовании */
    private RequestDeadlineSettingsItem taxiAwaitingApprovalDeadline;
    
    /** Контрольный срок - такси - ожидайте назначения водителя */
    private RequestDeadlineSettingsItem taxiAwaitingSearchDeadline;
    
    /** Контрольный срок - такси - поездка завершена */
    private RequestDeadlineSettingsItem taxiTripFinishedDeadline;
    
    /** Контрольный срок - личный а/м - на согласовании */
    private RequestDeadlineSettingsItem personalAwaitingApprovalDeadline;
    
    /** Контрольный срок - личный а/м - согласование присоединения к СП */
    private RequestDeadlineSettingsItem personalAwaitingSharedRideApprovalDeadline;
    
    /** Контрольный срок - личный а/м - поездка */
    private RequestDeadlineSettingsItem personalTripInProgressDeadline;
    
    /** Контрольный срок - личный а/м - утверждение маршрута */
    private RequestDeadlineSettingsItem personalAwaitingTripApprovalDeadline;
    
    /** Контрольный срок - личный а/м - формирование приказа на выплату */
    private RequestDeadlineSettingsItem personalOrderPaymentFormationDeadline;
    
    /** Контрольный срок - личный а/м - ожидание выплаты */
    private RequestDeadlineSettingsItem personalPaymentAwaitingDeadline;
    
    /** Контрольный срок - общественный тр-т - на согласовании */
    private RequestDeadlineSettingsItem publicAwaitingApprovalDeadline;
    
    /** Контрольный срок - общественный тр-т - подтверждение поездки */
    private RequestDeadlineSettingsItem publicTripConfirmationDeadline;
    
    /** Контрольный срок - общественный тр-т - утверждение */
    private RequestDeadlineSettingsItem publicAwaitingAffirmativeDeadline;
    
    /** Контрольный срок - общественный тр-т - формирование приказа на выплату */
    private RequestDeadlineSettingsItem publicOrderPaymentFormationDeadline;
    
    /** Контрольный срок - общественный тр-т - ожидание выплаты */
    private RequestDeadlineSettingsItem publicPaymentAwaitingDeadline;
    
    /** Контрольный срок - личный лимит - на согласовании */
    private LimitDeadlineSettingsItem employeeLimitDeadline;
    
    /** Контрольный срок - лимит подразделения - на согласовании */
    private LimitDeadlineSettingsItem departmentLimitDeadline;
    
    /** Контрольный срок - заявка на подключение к каршерингу - на согласовании */
    private CarsharingJoinDeadlineSettingsItem carsharingJoinDeadline;
    
    /** Признак удаления настройки для корп.клиента */
    @Builder.Default
    private boolean deleted = false;
    
    /** Элемент настройки - КС для этапа ЖЦ заявки на поездку */
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class RequestDeadlineSettingsItem {
        
        /** ID элемента настройки */
        private UUID id;
    
        /** Единица измерения времени */
        private ChronoUnit unit;
    
        /** Значение контрольного срока (в указанных ед.изменрения) */
        private Integer value;
        
        /** Соответствующий тип транспорта, для которого создана настройка */
        private String transportType;
    
        /** Соответствующий статус заявки, для которого создана настройка */
        private String requestStatus;
    }
    
    /** Элемент настройки - КС для конкретного типа лимита */
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class LimitDeadlineSettingsItem {
    
        /** ID элемента настройки */
        private UUID id;
    
        /** Единица измерения времени */
        private ChronoUnit unit;
    
        /** Значение контрольного срока (в указанных ед.изменрения) */
        private Integer value;
    
        /** Соответствующий тип лимита, для которого создана настройка */
        private String limitType;
    }
    
    /** Элемент настройки - КС для этапа ЖЦ заявки на подключение к корп.каршерингу */
    @Getter
    @Setter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class CarsharingJoinDeadlineSettingsItem {
    
        /** ID элемента настройки */
        private UUID id;
    
        /** Единица измерения времени */
        private ChronoUnit unit;
    
        /** Значение контрольного срока (в указанных ед.изменрения) */
        private Integer value;
    
        /** Статус заявки на подключение к корп. каршерингу */
        private String carsharingJoinStatus;
    
        /** Соответствующий тип транспорта, для которого создана настройка */
        private String transportType;
    }
}
