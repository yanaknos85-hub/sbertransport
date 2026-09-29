package ru.sber.transport.request.external.model;

/**
 * Заявка на поездку
 */
public interface EditTripOrderData {

    /**
     * Фактические данные поездки
     *
     * @return фактические данные поездки
     */
    OrderData getActual();

    /**
     * Статус заявки на поездку
     *
     * @return статус заявки на поездку
     */
    State getStatus();

    /**
     * Причина изменения заявки
     *
     * @return причина изменения заявки
     */
    String getReason();

    /**
     * Оценки
     *
     * @return оценки
     */
    Assessments getAssessments();

    /**
     * Ссылка на чек яндекс-go
     *
     * @return ссылка на яндекс-go
     */
    String getReceiptLink();
}
