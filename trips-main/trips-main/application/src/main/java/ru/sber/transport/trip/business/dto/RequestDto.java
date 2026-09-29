package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(title = "Заявка", description = "Данные заявки")
public record RequestDto(

        @Schema(description = "Идентификатор")
        UUID id,

        @Schema(description = "Идентификатор контрагента")
        UUID contractorId,

        @Schema(description = "Человекочитаемый идентификатор")
        String humanReadableId,

        @Schema(description = "Идентификатор автора")
        UUID authorId,

        @Schema(description = "Идентификатор пассажира")
        UUID passengerId,

        @Schema(description = "Решение по заявке")
        String resolution,

        @Schema(description = "Дата создания")
        LocalDateTime creationTime,

        @Schema(description = "Временная зона поездки")
        String timeZone,

        @Schema(description = "Время закрытия заявки")
        LocalDateTime finishedTime,

        @Schema(description = "Тип транспорта")
        TransportTypeEnum transportType,

        @Schema(description = "Класс такси")
        TaxiClass tripClass,

        @Schema(description = "Маршрут")
        List<Waypoint> waypoints,

        @Schema(description = "Идентификатор согласования")
        UUID approvalId,

        @Schema(description = "Дата согласования")
        LocalDateTime approvalDate,

        @Schema(description = "Признак совместной поездки")
        boolean coop,

        @Schema(description = "Желаемое время отправления")
        LocalDateTime desiredDate,

        @Schema(description = "SLA истек")
        boolean slaExpired,

        @Schema(description = "Состояние КС")
        String deadlineState,

        @Schema(description = "Признак межгорода")
        boolean suburb,

        @Schema(description = "Статус заявки")
        TripRequestStatus status,

        @Schema(description = "Код статуса")
        Integer statusCode,

        @Schema(description = "Количество пассажиров")
        int passengerCount,

        @Schema(description = "Ожидаемые данные поездки")
        Expected expected,

        @Schema(description = "Цель поездки")
        UUID purposeId,

        @Schema(description = "Комментарии водителю")
        String commentForDriver,

        @Schema(description = "Признак, является ли пассажир инициатором поездки")
        boolean passengerOwner,

        @Schema(description = "Идентификатор совместной поездки")
        UUID rideId,

        @Schema(description = "КС")
        LocalDateTime deadLine,

        @Schema(description = "Время начала поиска водителя")
        LocalDateTime taxiAwaitingSearchStartDate,

        @Schema(description = "Идентификатор водителя")
        UUID driverId,

        @Schema(description = "Идентификатор автопарка")
        UUID autoparkId,

        @Schema(description = "Автор заявки")
        Employee author,

        @Schema(description = "Пассажир")
        Employee passenger
) {

    @Schema(description = "Путевая точка маршрута", title = "Маршрут")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Waypoint(

            @Schema(description = "Идентификатор точки")
            UUID id,

            @Schema(description = "Страна")
            String country,

            @Schema(description = "Регион")
            String region,

            @Schema(description = "Город")
            String city,

            @Schema(description = "Улица")
            String street,

            @Schema(description = "Дом")
            String house,

            @Schema(description = "Корпус")
            String building,

            @Schema(description = "Строеник")
            String structure,

            @Schema(description = "Широта")
            double latitude,

            @Schema(description = "Долгота")
            double longitude,

            @Schema(description = "Порядковый номер точки")
            int index,

            @Schema(description = "Полный адрес")
            String fullAddress,

            @Schema(description = "Контакт")
            Contact contact,

            List<Passenger> passengers

            ) {
    }

    /**
     * Контакт на точке маршрута
     * @deprecated используется только для обратной совместимости, актуальный класс - {@link Passenger}
     */
    @Deprecated(since = "D-04.008.000")
    @Schema(description = "Контакт", title = "Контакт пассажира")
    public record Contact(

            @Schema(description = "Номер", title = "Номер телефона")
            String phone,

            @Schema(description = "ФИО", title = "ФИО пассажира")
            String name
    ) {
    }

    @Schema(description = "Пассажир", title = "Пассажир")
    public record Passenger(

            @Schema(description = "Действие пассажира на точке")
            PassengerActionType type,

            @Schema(description = "Имя пассажира")
            String firstName,

            @Schema(description = "Отчество пассажира")
            String patronymic,

            @Schema(description = "Телефон")
            String phone
    ) {
    }


    @Schema(description = "Ожидаемые данные поездки", title = "Ожидаемые данные")
    public record Expected(

            @Schema(description = "Расстояние")
            double distance,

            @Schema(description = "Стоимость")
            double cost,

            @Schema(description = "Время в пути")
            @JsonSerialize(using = DurationMillisConverter.class)
            Duration time
    ) {
    }

    public record Employee(
            @Schema(description = "Имя")
            String firstName,

            @Schema(description = "Фамилия")
            String lastName,

            @Schema(description = "Отчество")
            String patronymic,

            @Schema(description = "Табельный номер")
            String personnelNumber,

            @Schema(description = "Идентификатор подразделения")
            UUID departmentId,

            @Schema(description = "Идентификатор пользователя")
            UUID userId,

            @Schema(description = "Идентификатор должности")
            UUID positionId,

            @Schema(description = "Делегатор")
            UUID delegatedById,

            @Schema(description = "Название должности")
            String positionName,

            @Schema(description = "Рук-ль")
            UUID supervisorId,

            @Schema(description = "Организация")
            UUID organizationId,

            @Schema(description = "Название подразделения")
            String departmentName,

            @Schema(description = "Место возникновения затрат")
            String mvz,

            @Schema(description = "Человекочитаемый идентификатор")
            String humanReadableId,

            @Schema(description = "Номер телефона")
            String mobilePhone
    ) {
    }

}
