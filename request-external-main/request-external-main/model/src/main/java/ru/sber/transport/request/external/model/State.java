package ru.sber.transport.request.external.model;

import lombok.RequiredArgsConstructor;

/**
 * Статус заявки
 */
@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum State {

    /**
     * Новая заявка
     */
    NEW,

    /**
     * Требуется подтверждение завершения поездки
     */
    CONFIRMATION_NEEDED,

    /**
     * Заявка на подтверждении
     */
    CONFIRMATION,

    /**
     * Подтверждена
     */
    CONFIRMED,

    /**
     * Проверка GenAI заявки перед выплатой
     */
    GENAI_CHECK,

    /**
     * Отклонена
     */
    DECLINED,

    /**
     * Необходимы данные
     */
    DATA_NEEDED,

    /**
     * Отменена
     */
    CANCELLED,
    /**
     * Формирование приказа на выплату
     */
    ORDER_PAYMENT_FORMATION,
    /**
     * Ожидание выплаты
     */
    PAYMENT_AWAITING,
    /**
     * Выплата произведена
     */
    PAYMENT_DONE,
    /**
     * Выплата не произведена
     */
    PAYMENT_NOT_DONE
}
