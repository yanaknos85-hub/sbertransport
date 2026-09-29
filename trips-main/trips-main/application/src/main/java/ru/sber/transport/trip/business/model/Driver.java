package ru.sber.transport.trip.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver implements HasName {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Человекочитаемый идентификатор
     */
    private String humanReadableId;

    /**
     * Фамилия
     */
    private String lastName;

    /**
     * Имя
     */
    private String firstName;

    /**
     * Отчество
     */
    private String patronymic;

    /**
     * Идентификатор контрагента
     */
    private UUID contractorId;

    /**
     * Флаг активности
     */
    private boolean active;

    /**
     * Рейтинг
     */
    private int rating;

    /**
     * Водительское удостоверение
     */
    private String driverLicenseNumber;

    /**
     * Лицензия на перевозку грузов
     */
    private String cargoLicenceNumber;

    /**
     * Сервисная лицензия
     */
    private String serviceLicenseNumber;

    /**
     * Широта
     */
    private Double latitude;

    /**
     * Долгота
     */
    private Double longitude;

    /**
     * Время последнего пребывания на точке
     */
    private ZonedDateTime pointTime;

    /**
     * Часовой пояс
     */
    private String timeZone;

    /**
     * Флаг "Везет клиента"
     */
    private boolean serving;

    /**
     * Флаг нахождения на линии
     */
    private boolean online;

    /**
     * Идентификатор активной поездки
     */
    private UUID activeTripId;

    /**
     * Идентификатор активной смены
     */
    private UUID shiftId;

    /**
     * Стаж
     */
    private String experience;

    /**
     * Телефон
     */
    private String contactPhone;

    /**
     * Электронная почта
     */
    private String email;

    /**
     * Паспорт
     */
    private String passport;

    /**
     * Согласие на обработку данных
     */
    private boolean consent;

    /**
     * Азимут
     */
    private Double azimuth;

    /**
     * Идентификатор во внешней системе
     */
    private UUID oauthId;

    /**
     * Идентификатор филиала
     */
    private UUID autoparkId;
}
