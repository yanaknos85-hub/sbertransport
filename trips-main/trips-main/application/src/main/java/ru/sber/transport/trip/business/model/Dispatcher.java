package ru.sber.transport.trip.business.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Dispatcher implements HasName {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     *  Человекочитаемый идентификатор
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
     * Телефон
     */
    private String phone;

    /**
     * Электронная почта
     */
    private String email;

    /**
     * Идентификатор контрагента
     */
    private UUID contractorId;

    /**
     * Флаг активности
     */
    private boolean active;

    /**
     * Флаг согласия с условиями использования сервиса
     */
    private boolean consent;

    /**
     * Идентификатор пользователя во внешней системе
     */
    private UUID oauthId;

    /**
     * Идентификатор филиала автопарка
     */
    private UUID autoparkId;
}

