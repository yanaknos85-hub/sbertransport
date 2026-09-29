package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Заявка на поездку
 */
public interface TripOrderData extends BaseTripOrderData, EditTripOrderData {

    /**
     * Идентификатор заявки на поездку
     *
     * @return идентификатор заявки на поездку
     */
    UUID getId();

    /**
     * Человеко-читаемый идентификатор
     *
     * @return человеко-читаемый идентификатор
     */
    String getHumanReadableId();

    /**
     * Пассажир, который создал заявку на поездку
     *
     * @return пассажир, который создал заявку на поездку
     */
    Employee getPassenger();

    /**
     * Руководитель, который одобрил заявку на поездку
     *
     * @return руководитель, который одобрил заявку на поездку
     */
    Employee getApprover();

    /**
     * Ссылка на заявку на поездку
     *
     * @return ссылка на заявку на поездку
     */
    URI getLink();

    /**
     * Название файла с чеком поездки
     *
     * @return название файла с чеком поездки
     */
    String getReceipt();

    /**
     * Планируемая данные поездки
     *
     * @return планируемая данные поездки
     */
    OrderData getPlanned();

    /**
     * Место возникновения затрат (МВЗ)
     *
     * @return Место возникновения затрат (МВЗ)
     */
    String getCostCenter();

    /**
     * Информация о фроде
     *
     * @return информация о фроде
     */
    Fraud getFraud();

    /**
     * Тайм зона
     *
     * @return тайм зона
     */
    String getTimeZone();

    /**
     * Дата согласования заявки
     *
     * @return дата согласования заявки
     */
    OffsetDateTime getApprovalDate();

    /**
     * Экономия(руб.) при заказе поездки в яндекс такси, чем в корп. такси
     *
     * @return экономия
     */
    BigDecimal getEconomy();
}
