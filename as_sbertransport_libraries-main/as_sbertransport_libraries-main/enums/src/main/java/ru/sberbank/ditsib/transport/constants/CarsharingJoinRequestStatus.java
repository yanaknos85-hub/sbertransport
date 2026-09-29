package ru.sberbank.ditsib.transport.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Статусы подключения к каршерингу.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CarsharingJoinRequestStatus {

    /**
     * На рассмотрении.
     */
    UNDER_CONSIDERATION("На рассмотрении", "#FFB467"),

    /**
     * Выполнено.
     */
    DONE("Исполнено", "#17D35B"),

    /**
     * Отменено.
     */
    CANCELLED("Отменено", "#757575");
    
    private final String description;

    private final String color;

    /**
     * Статусы отмены.
     */
    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum StatusCode {

        /**
         * Отменено пользователем.
         */
        CANCELLED_BY_EMPLOYEE(201, "Отмена пользователем", CANCELLED),

        /**
         * Отменено по КС.
         */
        AT_EXPIRATION(206, "По истечении срока", CANCELLED);
        
        private final int code;

        private final String description;

        private final CarsharingJoinRequestStatus carsharingJoinRequestStatus;
    }
}
