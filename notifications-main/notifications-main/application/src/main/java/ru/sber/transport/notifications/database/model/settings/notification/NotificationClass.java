package ru.sber.transport.notifications.database.model.settings.notification;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static ru.sber.transport.notifications.database.model.settings.notification.NotificationType.*;

/**
 * Доступные типы уведомлений.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum NotificationClass {
    
    /**
     * Запрос на такси.
     */
    REQUEST_TAXI(List.of(APPROVE, APPROVE_STATUS, WAITING_FOR_DRIVER, DRIVER_ASSIGNED, DRIVER_ARRIVED,
                         FREE_TIME_EXPIRED, TRIP_STARTED, WAYPOINT_ARRIVED, WAYPOINT_WAITING_EXPIRED, TRIP_FINISHED,
                         COOP_TRIP_ATTACHMENT, COOP_TRIP_CHANGES)),
    
    /**
     * Запрос на общественный транспорт.
     */
    REQUEST_PUBLIC(List.of(APPROVE, APPROVE_STATUS, TRIP_CONFIRM, TRIP_AFFIRM, TRIP_AFFIRM_STATUS,
                                 PAYMENT_STATUS)),
    
    /**
     * Запрос на личный транспорт.
     */
    REQUEST_PERSONAL(List.of(APPROVE, APPROVE_STATUS, COOP_TRIP_ATTACHMENT_APPROVE,
                                   COOP_TRIP_ATTACHMENT_STATUS, TRIP_START_REMIND, WAYPOINT_ARRIVED, TRIP_FINISHED,
                                   FINAL_ROUTE_AFFIRM, FINAL_ROUTE_AFFIRM_STATUS, PAYMENT,
                                   PAYMENT_STATUS, COOP_TRIP_ATTACHMENT, COOP_TRIP_CHANGES,
                                   ADDITIONAL_WAYPOINTS_APPROVAL, ADDITIONAL_WAYPOINTS_STATUS)),
    
    /**
     * Запрос на каршеринг.
     */
    REQUEST_CAR_SHARING(List.of(APPROVE, APPROVE_STATUS, TRANSPORT_BOOKING, TRANSPORT_OVERVIEW,
                                      TRIP_STARTED, WAYPOINT_ARRIVED, TRANSPORT_BOOKING_FINISHED, TRIP_FINISHED,
                                      COOP_TRIP_ATTACHMENT, COOP_TRIP_CHANGES)),
    
    /**
     * Запрос на велосипед.
     */
    REQUEST_BICYCLE(List.of(APPROVE, APPROVE_STATUS, TRANSPORT_BOOKING, TRANSPORT_OVERVIEW,
                                  TRIP_STARTED, WAYPOINT_ARRIVED, TRANSPORT_BOOKING_FINISHED, TRIP_FINISHED)),
    
    /**
     * Запрос на самокат.
     */
    REQUEST_SCOOTER(List.of(APPROVE, APPROVE_STATUS, TRANSPORT_BOOKING, TRANSPORT_OVERVIEW,
                                  TRIP_STARTED, WAYPOINT_ARRIVED, TRANSPORT_BOOKING_FINISHED, TRIP_FINISHED)),

    /**
     * Запрос на доставку груза
     */
    REQUEST_CARGO(List.of(APPROVE, APPROVE_STATUS, WAITING_FOR_DRIVER, DRIVER_ASSIGNED, DRIVER_ARRIVED,
            FREE_TIME_EXPIRED, TRIP_STARTED, WAYPOINT_ARRIVED, WAYPOINT_WAITING_EXPIRED, TRIP_FINISHED,
            COOP_TRIP_ATTACHMENT, COOP_TRIP_CHANGES)),


    /**
     * Запрос на доставку тела
     */
    REQUEST_GROUP_TRANSFER(List.of(GROUP_TRANSFER_AWAITING_APPROVAL, GROUP_TRANSFER_APPROVED,
            GROUP_TRANSFER_AWAITING_SEARCH, GROUP_TRANSFER_DRIVER_SEARCH, GROUP_TRANSFER_DRIVER_FOUND, GROUP_TRANSFER_DRIVER_FOUND_MZK,
            GROUP_TRANSFER_DRIVER_ON_THE_WAY, GROUP_TRANSFER_DRIVER_ARRIVED, GROUP_TRANSFER_TRIP_FINISHED,
            GROUP_TRANSFER_CANCELLED)),

    /**
     * Запрос на персональный лимит.
     */
    REQUEST_LIMIT_PERSON(List.of(APPROVE, APPROVE_STATUS, REQUEST_EXECUTION)),
    
    /**
     * Запрос на лимит подразделения.
     */
    REQUEST_LIMIT_DEPARTMENT(List.of(APPROVE, APPROVE_STATUS, REQUEST_EXECUTION)),
    
    /**
     * Событие лимита подразделения.
     */
    LIMIT_DEPARTMENT(List.of(ALLOCATION, CHANGE, STATUS, LOW_REMAINS, LOW_REMAINS_FROM_EMPLOYEE)),
    
    /**
     * Событие персонального лимита.
     */
    LIMIT_PERSON(List.of(ALLOCATION, CHANGE, STATUS, LOW_REMAINS)),
    
    /**
     * Событие делегирования.
     */
    USER_DELEGATE(List.of(ASSIGNMENT, RESTRICTIONS_EDITED_DATE, RESTRICTIONS_EDITED_DATE)),
    
    /**
     * Событие владельца лимита.
     */
    USER_OWNER_LIMIT(List.of(ASSIGNMENT, RESTRICTIONS_EDITED)),
    
    /**
     * Событие назначения.
     */
    USER_ASSIGNMENT(List.of(ASSIGNMENT)),

    /**
     * События водителей.
     */
    CONTRACTOR(List.of(DRIVER_ASSIGNED, DRIVER_REJECTED_REQUEST, PASSENGER_LOOKING_FOR_DRIVER, PASSENGER_REJECTED_REQUEST, TRIP_WITH_LOW_RATE, REQUEST_WITH_INDICATION_CREATED)),
    
    /**
     * События диспетчерской.
     */
    DISPATCHER_NOTIFICATION(List.of(PUSH_DECLINED_BY_DRIVER, PUSH_PASSENGER_COMMUNICATION_REQUEST, PUSH_DRIVER_NOT_ASSIGNED,
                                    PUSH_DECLINED_BY_PASSENGER, PUSH_LOW_GRADE_TRIP, PUSH_INDICATION_TRIP_REQUEST,
                                    DECLINED_BY_DRIVER, PASSENGER_COMMUNICATION_REQUEST, DRIVER_NOT_ASSIGNED,
                                    DECLINED_BY_PASSENGER, LOW_GRADE_TRIP, INDICATION_TRIP_REQUEST));
    
    private final List<NotificationType> types;
}
