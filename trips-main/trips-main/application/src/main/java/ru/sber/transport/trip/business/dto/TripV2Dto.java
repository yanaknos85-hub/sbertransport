package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.sber.transport.trip.business.model.Trip;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Объект поездки.
 *
 * @param id идентификатор.
 * @param humanReadableId человекочитаемый идентификатор.
 * @param status статус.
 * @param requests список заявок.
 * @param waypoints список точек.
 * @param factStartTime фактическое время начала поездки.
 * @param factEndTime фактическое время окончания поездки.
 * @param expectedStartTime плановое время начала поездки.
 * @param expectedEndTime плановое время окончания поездки.
 * @param driver водитель.
 * @param dispatcher диспетчер.
 */
@Schema(title = "Поездка",
        description = "Данные поездки")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TripV2Dto(

        @Schema(title = "Идентификатор", description = "Идентификатор поездки")
        UUID id,

        @Schema(title = "HRI", description = "Человекочитаемый идентификатор поездки")
        String humanReadableId,

        @Schema(title = "Статус", description = "Статус поездки")
        TripStatus status,

        @Schema(title = "Заявки", description = "Список заявок, участвующих в поездке")
        List<Map<String, Object>> requests,

        @Schema(title = "Дополнительные данные", description = "Кол-во пассажиров")
        int passengerCount,

        @Schema(title = "Дополнительные данные", description = "Класс такси")
        String taxiClass,

        @Schema(title = "Дополнительные данные", description = "Время ожидания водителя")
        Duration driverWaitingTime,

        @Schema(title = "Остановки", description = "Список остановок посадки/высадки пассажиров")
        List<RequestDto.Waypoint> waypoints,

        @Schema(title = "Начало", description = "Фактическое время начала поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime factStartTime,

        @Schema(title = "Окончание", description = "Фактическое время окончания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime factEndTime,

        @Schema(title = "Водитель", description = "Базовые данные водителя")
        DriverShortDTO driver,

        @Schema(title = "Автомобиль", description = "Базовые данные автомобиля")
        VehicleDTO vehicle,

        @Schema(title = "Диспетчер", description = "Базовые данные диспетчера")
        DispatcherShortDTO dispatcher,

        @Schema(title = "Контрагент", description = "Идентификатор контрагента")
        UUID contractorId,

        @Schema(title = "Дистанция", description = "Фактическая дистанция поездки")
        Double factDistance,

        @Schema(title = "Новизна", description = "Флаг новизны поездки")
        boolean isNew,

        @Schema(title = "Дата создания поездки", description = "Дата создания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime creationTime,

        @Schema(title = "Стоимость", description = "Плановая стоимость поездки (копейки)")
        Long expectedCost,

        @Schema(title = "Протяженность", description = "Плановая протяженность поездки (километры)")
        Double expectedDistance,

        @Schema(title = "Длительность", description = "Плановая длительность поездки (секунды)")
        Long expectedTime,

        @Schema(title = "План", description = "Плановые данные по водителю и автомобилю")
        Planned planned,

        @Schema(title = "Человекочитаемый идентификатор", description = "Человекочитаемый идентификатор поездки на стороне клиента")
        String externalHumanReadableId,

        @Schema(title = "Комментарий", description = "Комментарий к заказу")
        String comment,

        @Schema(title = "Дополнительная информация", description = "Дополнительная информация по трансферу")
        Information information,

        @Schema(title = "Начало", description = "Планируемое время начала поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime expectedStartTime,

        @Schema(title = "Окончание", description = "Планируемое время окончания поездки")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime expectedEndTime,

        @Schema(title = "Стоимость", description = "Фактическая стоимость поездки (копейки)")
        Long factCost,

        @Schema(title = "Время", description = "Время взятия заявки в работу диспетчером")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime dispatcherTakeToWork,

        @Schema(title = "Время", description = "Время последнего изменения статуса")
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime statusChangedAt
) {

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record Planned(
                @Schema(title = "Водитель", description = "Плановые данные водителя")
                DriverShortDTO driver,

                @Schema(title = "Автомобиль", description = "Плановые данные автомобиля")
                VehicleDTO vehicle
        ){ }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Information(
                @Schema(title = "Детское кресло", description = "Признак необходимости в детском кресле")
                Boolean childSeat,

                @Schema(title = "Детали детских кресел", description = "Объект о кол-ве кресел")
                ChildSeatDetails childSeatDetails,

                @Schema(title = "Багаж", description = "Признак наличия багажа")
                Boolean bugs,

                @Schema(title = "Комментарий по багажу", description = "Количество багажа, комментарий")
                String bugsComment,

                @Schema(title = "Негабаритный багаж", description = "Признак наличия негабаритного багажа")
                Boolean bugsOversized,

                @Schema(title = "Комментарий по негабаритному багажу", description = "Негабаритный багаж, комментарий")
                String bugsOversizedComment,

                @Schema(title = "Животное", description = "Признак наличия животного")
                Boolean animal,

                @Schema(title = "Комментарий по животному", description = "Животные, комментарий")
                String animalComment,

                @Schema(title = "Рейс/поезд", description = "Номер рейса/поезда")
                String numberFlight,

                @Schema(title = "Дата", description = "Дата и время рейса/поездка")
                @JsonSerialize(using = OffsetDateTimeSerializer.class)
                OffsetDateTime dateFlight,

                @Schema(title = "Номер", description = "Номер в гостинице")
                String phoneHotel,

                @Schema(title = "Контакт", description = "Телефон доп. контакта")
                String addContactPhone,

                @Schema(title = "Контакт", description = "ФИО доп. контакта")
                String addContactFIO,

                @Schema(title = "Тип", description = "Вид транспорта")
                String typeVehicle
        ){}

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ChildSeatDetails(
                @Schema(title = "Кресло", description = "Кресло от 9 мес. до 4 лет : число")
                Integer group1,

                @Schema(title = "Кресло", description = "Кресло 3-7 лет : число")
                Integer group2,

                @Schema(title = "Бустер", description = "Бустер 6-12 лет: число")
                Integer booster,

                @Schema(title = "Люлька", description = "Люлька до 1 года: число")
                Integer newborn
        ){}

}
