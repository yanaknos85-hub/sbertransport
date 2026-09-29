package ru.sber.transport.notifications.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Возможные типы уведомлений.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Schema(title = "Возможные типы уведомлений")
public enum NotificationTypeDto {
    
    @Schema(description = "Согласование")
    APPROVE,
    
    @Schema(description = "Статус согласования")
    APPROVE_STATUS,
    
    @Schema(description = "Ожидание водителя")
    WAITING_FOR_DRIVER,
    
    @Schema(description = "Водитель назначен")
    DRIVER_ASSIGNED,

    @Schema(description = "Водитель назначен. Диспетчер")
    DRIVER_ASSIGNED_DISPATCHER,

    @Schema(description = "Водитель назначен. Данные получены через XML")
    DRIVER_ASSIGNED_XML,
    
    @Schema(description = "Водитель прибыл в точку отправления")
    DRIVER_ARRIVED,
    
    @Schema(description = "Время бесплатного ожидания истекло")
    FREE_TIME_EXPIRED,
    
    @Schema(description = "Поездка начата")
    TRIP_STARTED,
    
    @Schema(description = "Прибытие в промежуточный пункт")
    WAYPOINT_ARRIVED,
    
    @Schema(description = "Время ожидания в промежуточном пункте превышено")
    WAYPOINT_WAITING_EXPIRED,
    
    @Schema(description = "Поездка завершена")
    TRIP_FINISHED,
    
    @Schema(description = "Присоединение к совместной поездке")
    COOP_TRIP_ATTACHMENT,
    
    @Schema(description = "Изменение совместной поездки")
    COOP_TRIP_CHANGES,
    
    @Schema(description = "Поездка подтверждена")
    TRIP_CONFIRM,
    
    @Schema(description = "Поездка утверждена")
    TRIP_AFFIRM,
    
    @Schema(description = "Статус утверждения поездки")
    TRIP_AFFIRM_STATUS,
    
    @Schema(description = "Статус оплаты")
    PAYMENT_STATUS,
    
    @Schema(description = "Согласование присоединения к СП")
    COOP_TRIP_ATTACHMENT_APPROVE,
    
    @Schema(description = "Статус присоединения к СП")
    COOP_TRIP_ATTACHMENT_STATUS,
    
    @Schema(description = "Напоминание о начале поездки")
    TRIP_START_REMIND,
    
    @Schema(description = "Утверждение финального маршрута")
    FINAL_ROUTE_AFFIRM,
    
    @Schema(description = "Статус утверждения финального маршрута")
    FINAL_ROUTE_AFFIRM_STATUS,
    
    @Schema(description = "Подтверждение доп. точек")
    ADDITIONAL_WAYPOINTS_APPROVAL,
    
    @Schema(description = "Статус подтверждения доп. точек")
    ADDITIONAL_WAYPOINTS_STATUS,
    
    @Schema(description = "Бронирование")
    TRANSPORT_BOOKING,
    
    @Schema(description = "Осмотр")
    TRANSPORT_OVERVIEW,
    
    @Schema(description = "Бронирование завершено")
    TRANSPORT_BOOKING_FINISHED,
    
    @Schema(description = "Исполнение заявки")
    REQUEST_EXECUTION,
    
    @Schema(description = "Выделение")
    ALLOCATION,
    
    @Schema(description = "Изменение")
   CHANGE,
    
    @Schema(description = "Статус")
    STATUS,
    
    @Schema(description = "Малый остаток")
    LOW_REMAINS,
    
    @Schema(description = "Малый остаток (от сотрудника)")
    LOW_REMAINS_FROM_EMPLOYEE,
    
    @Schema(description = "Назначение")
    ASSIGNMENT,
    
    @Schema(description = "Полномочия изменены (дата)")
    RESTRICTIONS_EDITED_DATE,
    
    @Schema(description = "Полномочия изменены (тип)")
    RESTRICTIONS_EDITED_TYPE,
    
    @Schema(description = "Полномочия изменены")
    RESTRICTIONS_EDITED,
    
    @Schema(description = "Оплата")
    PAYMENT,
    
    @Schema(description = "PUSH: Отказ от поездки водителем")
    PUSH_DECLINED_BY_DRIVER,
    
    @Schema(description = "PUSH: Пассажиру нужна связь с водителем")
    PUSH_PASSENGER_COMMUNICATION_REQUEST,
    
    @Schema(description = "PUSH: Водитель не назначен")
    PUSH_DRIVER_NOT_ASSIGNED,
    
    @Schema(description = "PUSH: Отмена поездки пассажиром")
    PUSH_DECLINED_BY_PASSENGER,
    
    @Schema(description = "PUSH: Поездка с низкой оценкой")
    PUSH_LOW_GRADE_TRIP,
    
    @Schema(description = "PUSH: Создана заявка с индикацией")
    PUSH_INDICATION_TRIP_REQUEST,
    
    @Schema(description = "Отказ от поездки водителем")
    DECLINED_BY_DRIVER,
    
    @Schema(description = "Пассажиру нужна связь с водителем")
    PASSENGER_COMMUNICATION_REQUEST,
    
    @Schema(description = "Водитель не назначен")
    DRIVER_NOT_ASSIGNED,
    
    @Schema(description = "Отмена поездки пассажиром")
    DECLINED_BY_PASSENGER,
    
    @Schema(description = "Поездка с низкой оценкой")
    LOW_GRADE_TRIP,
    
    @Schema(description = "Создана заявка с индикацией")
    INDICATION_TRIP_REQUEST,

    DRIVER_REJECTED_REQUEST,
    PASSENGER_LOOKING_FOR_DRIVER,
    PASSENGER_REJECTED_REQUEST,
    TRIP_WITH_LOW_RATE,
    REQUEST_WITH_INDICATION_CREATED,
    DECLINED_BY_APPROVER,
    CARGO_DRIVER_AWAITING_DATA,
    CARGO_COURIER_AWAITING_DATA,
    CARGO_SHIPMENT_FINISHED,
    CARGO_CANCELED_BY_ENGINEER,
    CARGO_CANCELED_BY_CONTRACTOR,
    CARGO_AWAITING_TRANSFER,
    CARGO_TRANSFER_FINISHED,
    CARGO_DELIVERY_CONFIRMATION_FINISHED,
    CARGO_PLANNING,
    @Schema(description = "Изменение плановых сроков исполнения")
    CARGO_CHANGE_DESIRED_DATE,

    GROUP_TRANSFER_AWAITING_APPROVAL,
    GROUP_TRANSFER_DRIVER_SEARCH,
    GROUP_TRANSFER_DRIVER_FOUND,
    GROUP_TRANSFER_DRIVER_FOUND_MZK,
    GROUP_TRANSFER_APPROVED,
    GROUP_TRANSFER_AWAITING_SEARCH,
    GROUP_TRANSFER_DRIVER_ON_THE_WAY,
    GROUP_TRANSFER_DRIVER_ARRIVED,
    GROUP_TRANSFER_TRIP_IN_PROGRESS,
    GROUP_TRANSFER_TRIP_FINISHED,
    GROUP_TRANSFER_CANCELLED,

    CUSTOM_NOTIFICATION
}
