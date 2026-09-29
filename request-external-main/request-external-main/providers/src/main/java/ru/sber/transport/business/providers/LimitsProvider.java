package ru.sber.transport.business.providers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Function;
import ru.sber.transport.request.external.model.TripOrderData;

/**
 * Провайдер данных о лимитах
 */
public interface LimitsProvider {

    /**
     * Зарезервировать средства на заявку на поездку
     *
     * @param source данные заявки
     * @param func функция, которая получает сумму заявки
     */
    void reserve(TripOrderData source, Function<TripOrderData, BigDecimal> func);

    /**
     * Подтвердить траты по заявке на поездку
     *
     * @param id идентификатор заявки на поездку
     */
    void confirm(UUID id);

    /**
     * Отменить заявку на поездку
     *
     * @param id идентификатор заявки на поездку
     */
    void cancel(UUID id);
}
