package ru.sber.transport.trips.cargo.business.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.trips.cargo.serializer.OffsetDateTimeDeserializer;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Маршрут", description = "Данные маршрута грузоперевозки")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IntegrationRequestDTO {

        @Schema(description = "Идентификатор (человекочитаемый)", required = true)
        private String humanReadableId;

        @Schema(description = "Автор", required = true)
        private ContactDto author;

        @Schema(description = "Ид департамента")
        private UUID departmentId;

        @Schema(description = "Дата отправления", required = true)
        @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
        private OffsetDateTime desiredDate;

        @Schema(description = "Желаемый вид автомобиля")
        private Auto desiredAuto;

        @Schema(description = "Рабочая группа")
        private String workgroup;

        @Schema(description = "Инн контрагента")
        private String contragentInn;

        @Schema(description = "Код группы")
        private String reestr;

        @Schema(description = "Комментарий к маршруту")
        private String comment;

        @Schema(description = "Точки маршрута")
        private List<RouteWaypointDto> waypoints = new ArrayList<>();

        @Schema(description = "Предварительная стоимость доставки, коп")
        private Long cost;

        @Schema(description = "Стоимость каждого последующего часа доставки, коп/час")
        private Long hourTariff;

        @Schema(description = "Предварительная дальность доставки, км")
        private Double distance;

        @Schema(description = "Идентификатор в системе контрагента")
        private String contractorTripId;

        @Schema(description = "Сервис-источник маршрута")
        private RequestSource source;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Контактное лицо", description = "Данные контактного лица")
        public static class ContactDto {

                @Schema(description = "ФИО", required = true)
                private String name;

                @Schema(description = "Телефон", required = true)
                private String phone;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Точка маршрута", description = "Данные точки маршрута")
        public static class RouteWaypointDto {

                @Schema(description = "Порядковый номер", required = true)
                private int orderingIndex;

                @Schema(description = "id точки")
                private UUID id;

                @Schema(description = "Тип точки", required = true)
                private WaypointType type;

                @Schema(description = "Адрес точки", required = true)
                private Address address;

                @Schema(description = "Заявки, связанные с данной точкой")
                private List<Contact> contacts = new ArrayList<>();
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Контакт на маршруте", description = "Данные контактного лица на маршруте")
        public static class Contact {

                @Schema(description = "Контакт", required = true)
                private ContactDto contact;

                @Schema(description = "Заявки", required = true)
                private List<RouteRequestDto> requests;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Адрес", description = "Данные адреса")
        public static class Address {

                @Schema(description = "Строковое представление адреса")
                private String addressStringRepresentation;

                @Schema(description = "Координаты")
                private Coordinates coordinates;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Координаты", description = "Координаты")
        public static class Coordinates {

                @Schema(description = "Широта")
                private Double latitude;

                @Schema(description = "Долгота")
                private Double longitude;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Заявка маршрута", description = "Данные заявки маршрута")
        public static class RouteRequestDto {

                @Schema(description = "Идентификатор (человекочитаемый)", required = true)
                private String humanReadableId;

                @Schema(description = "Тип точки")
                private WaypointType type;

                @Schema(description = "Организация по заявке")
                private String organization;

                @Schema(description = "Груз")
                private List<CargoData> cargo;

                @Schema(description = "Колличество грузчиков")
                private Integer loaders;

                @Schema(description = "Комментарий")
                private String comment;

                @Schema(description = "Упаковка")
                @JsonAlias(value = "package")
                private List<Pack> pack;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Авто", description = "Данные автомобиля")
        public static class Auto {

                @Schema(description = "Грузоподъемность, кг")
                private Double weight;

                @Schema(description = "Объем, м3")
                private Double volume;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Груз", description = "Данные груза")
        public static class CargoData {

                @Schema(description = "Порядковый номер груза")
                private Integer orderingIndex;

                @Schema(description = "Наименование груза")
                private String cargoName;

                @Schema(description = "Масса, кг")
                private Double weight;

                @Schema(description = "Объем, м3")
                private Double volume;

                @Schema(description = "Количество мест")
                private Integer occupiedPlacesCount;

                @Schema(description = "Высота, см")
                private Double height;

                @Schema(description = "Длина, см")
                private Double length;

                @Schema(description = "Ширина, см")
                private Double width;

                @Schema(description = "Бьющийся")
                private boolean fragile;
        }

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        @Schema(title = "Упаковка")
        public static class Pack {

                @Schema(description = "Наименование (только для LOAD)")
                private String name;

                @Schema (description = "Единица измерения (только для LOAD)")
                private String unit;

                @Schema (description = "Количество")
                private int count;
        }
}