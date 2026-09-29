package ru.sber.transport.magenta.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO с данными по публикуемому тарифу
 * <p>
 {
 "TransportUnitedTrip": {
 "Timezone": "Etc/GMT-3",
 "passengers": [
 {
 "PassengerID": "1",
 "PassengerTripPlanPrice": "9",
 "PassengerIncidentID": "10015901",
 "PassengerTripComment": null,
 "PassengerPhone": null,
 "PassengerTripFactPrice": null,
 "PassengerContactName": null,
 "PassengerTripPlanShare": "0.5",
 "PassengerTripPlanDistance": "16",
 "PassengerTripPlanDuration": "1970-01-01T00:22:00+03:00"
 },
 {
 "PassengerID": "2",
 "PassengerTripPlanPrice": "9",
 "PassengerIncidentID": "10015902",
 "PassengerTripComment": null,
 "PassengerPhone": null,
 "PassengerTripFactPrice": null,
 "PassengerContactName": null,
 "PassengerTripPlanShare": "0.5",
 "PassengerTripPlanDistance": "16",
 "PassengerTripPlanDuration": "1970-01-01T00:22:00+03:00"
 }
 ],
 "ExternalID": "2005765",
 "TripTotalPlanDistance": "16",
 "TripStartAddress": "Россия, Москва, улица Расплетина, 3",
 "waypoints": [
 {
 "WaypointTimeArrive": "2022-04-28T00:00:00+03:00",
 "WaypointType": "Посадка",
 "WaypointTimeWait": "1970-01-01T00:00:00+03:00",
 "WaypointAddress": "Россия, Москва, улица Расплетина, 3",
 "WaypointLongitude": "37.480058",
 "WaypointLatitude": "55.790342",
 "WaypointIncidentID": "10015901",
 "WaypointID": "1",
 "WaypointTimeDepart": "2022-04-28T00:00:00+03:00"
 },
 {
 "WaypointTimeArrive": "2022-04-28T00:00:00+03:00",
 "WaypointType": "Посадка",
 "WaypointTimeWait": "1970-01-01T00:00:00+03:00",
 "WaypointAddress": "Россия, Москва, улица Расплетина, 3",
 "WaypointLongitude": "37.480058",
 "WaypointLatitude": "55.790342",
 "WaypointIncidentID": "10015902",
 "WaypointID": "2",
 "WaypointTimeDepart": "2022-04-28T00:00:00+03:00"
 },
 {
 "WaypointTimeArrive": "2022-04-28T00:22:10+03:00",
 "WaypointType": "Высадка",
 "WaypointTimeWait": "1970-01-01T00:00:00+03:00",
 "WaypointAddress": "Россия, Москва, улица Пришвина, 3",
 "WaypointLongitude": "37.592276",
 "WaypointLatitude": "55.890139",
 "WaypointIncidentID": "10015901",
 "WaypointID": "3",
 "WaypointTimeDepart": "2022-04-28T00:22:10+03:00"
 },
 {
 "WaypointTimeArrive": "2022-04-28T00:22:10+03:00",
 "WaypointType": "Высадка",
 "WaypointTimeWait": "1970-01-01T00:00:00+03:00",
 "WaypointAddress": "Россия, Москва, улица Пришвина, 3",
 "WaypointLongitude": "37.592276",
 "WaypointLatitude": "55.890139",
 "WaypointIncidentID": "10015902",
 "WaypointID": "4",
 "WaypointTimeDepart": "2022-04-28T00:22:10+03:00"
 }
 ],
 "TripStartIncidentID": "10015901",
 "CountPassengers": 2,
 "TripEndIncidentID": "10015902",
 "TripTotalPlanDuration": "1970-01-01T00:22:00+03:00",
 "TripTotalPlanPrice": "18",
 "TripEndAddress": "Россия, Москва, улица Пришвина, 3",
 "PlanTimeStart": "2022-04-28T00:00:00+03:00"
 }
 }
 </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MagentaOrgUpdateInfoRequestDTO {

    /**
     * Объединенная поездка
     */
    private TransportUnitedTrip transportUnitedTrip = new TransportUnitedTrip();

    /**
     * Модель поездки
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportUnitedTrip {
        /*
         "ExternalID": "2005765",
         "TripTotalPlanDistance": "16",
         "TripStartAddress": "Россия, Москва, улица Расплетина, 3",
         "TripStartIncidentID": "10015901",
         "CountPassengers": 2,
         "TripEndIncidentID": "10015902",
         "TripTotalPlanDuration": "1970-01-01T00:22:00+03:00",
         "TripTotalPlanPrice": "18",
         "TripEndAddress": "Россия, Москва, улица Пришвина, 3",
         "PlanTimeStart": "2022-04-28T00:00:00+03:00"
         */
        /**
         * Временная зона
         */
        private String timezone;

        /**
         * Внешний ID
         */
        private String externalID;

        /**
         * ID случая начала поездки
         */
        private String tripStartIncidentID;

        /**
         * ID случая конца поездки
         */
        private String tripEndIncidentID;

        /**
         * Адрес начала поездки
         */
        private String tripStartAddress;

        /**
         * Адрес конца поездки
         */
        private String tripEndAddress;

        /**
         * Количество пассажиров
         */
        private String countPassengers;

        /**
         * Плановая длительность поездки
         */
        private String tripTotalPlanDistance;

        /**
         * Плановая длительность поездки
         */
        private String tripTotalPlanDuration;

        /**
         * Плановая стоимость поездки
         */
        private String tripTotalPlanPrice;

        /**
         * Плановое время начала поездки
         */
        private String planTimeStart;

        /**
         * Список точек
         */
        private final List<Waypoint> waypoints = new ArrayList<>();

        /**
         * Список пассажиров
         */
        private final List<Passenger> passengers = new ArrayList<>();
    }

    /**
     * Данные по пассажиру
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Passenger {

        /**
         * ID пассажира
         */
        private String passengerID;

        /**
         * ID инцидента пассажира
         */
        private String passengerIncidentID;

        /**
         * Телефон пассажира
         */
        private String passengerPhone;

        /**
         * Имя контактного лица пассажира
         */
        private String passengerContactName;

        /**
         * Комментарий пассажира
         */
        private String passengerTripComment;

        /**
         * Цена поездки пассажира
         */
        private String passengerTripFactPrice;

        /**
         * Доля поездки пассажира
         */
        private String passengerTripPlanShare;

        /**
         * Плановая стоимость поездки пассажира
         */
        private String passengerTripPlanPrice;

        /**
         * Плановое расстояние поездки пассажира
         */
        private String passengerTripPlanDistance;

        /**
         * Плановая длительность поездки пассажира
         */
        private String passengerTripPlanDuration;
        
/*
 "PassengerID": "1",
 "PassengerTripPlanPrice": "9",
 "PassengerIncidentID": "10015901",
 "PassengerTripComment": null,
 "PassengerPhone": null,
 "PassengerTripFactPrice": null,
 "PassengerContactName": null,
 "PassengerTripPlanShare": "0.5",
 "PassengerTripPlanDistance": "16",
 "PassengerTripPlanDuration": "1970-01-01T00:22:00+03:00"
 */
    }

    /**
     * Точка поездки
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Waypoint {

        /**
         * ID точки
         */
        private String waypointID;

        /**
         * ID инцидента точки
         */
        private String waypointIncidentID;

        /**
         * Тип точки
         */
        private String waypointType;

        /**
         * Время ожидания
         */
        private String waypointTimeWait;

        /**
         * Адрес
         */
        private String waypointAddress;

        /**
         * Долгота
         */
        private String waypointLongitude;

        /**
         * Широта
         */
        private String waypointLatitude;

        /**
         * Время отправления
         */
        private String waypointTimeDepart;

        /**
         * Время прибытия
         */
        private String waypointTimeArrive;
/*
"WaypointTimeArrive": "2022-04-28T00:00:00+03:00",
 "WaypointType": "Посадка",
 "WaypointTimeWait": "1970-01-01T00:00:00+03:00",
 "WaypointAddress": "Россия, Москва, улица Расплетина, 3",
 "WaypointLongitude": "37.480058",
 "WaypointLatitude": "55.790342",
 "WaypointIncidentID": "10015901",
 "WaypointID": "1",
 "WaypointTimeDepart": "2022-04-28T00:00:00+03:00"
 */
    }
}