package ru.sberbank.ditsib.transport.request.human_readable_id.model;

/**
 * Префиксы формирования идентификатора
 */
public enum Prefix implements ru.sber.transport.humanreadableid.model.interfaces.Prefix {

    /**
     * Заявка на перевозку
     */
    OT,

    /**
     * Лимит подразделения
     */
    LD,

    /**
     * Пользователь
     */
    US
}
