package ru.sber.transport.request.external.web.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import ru.sber.transport.request.external.model.BaseTripOrderData;
import ru.sber.transport.request.external.model.Tariff;
import ru.sber.transport.request.external.model.WaypointData;
import ru.sber.transport.web.model.NewExternalRequest;

/**
 * Данные о заказе на новую поездку.
 */
public class WebNewTripOrderData implements BaseTripOrderData {

    private final NewExternalRequest delegatee;

    /**
     * Создание нового объекта.
     *
     * @param delegatee объект для обертывания.
     */
    public WebNewTripOrderData(NewExternalRequest delegatee) {
        this.delegatee = delegatee;
    }

    @Override
    public OffsetDateTime getDate() {
        return delegatee.getTripDate();
    }

    @Override
    public Tariff getTariff() {
        return Tariff.valueOf(delegatee.getTariff().name());
    }

    @Override
    public String getComment() {
        return delegatee.getComment();
    }

    @Override
    public List<WaypointData> getWaypoints() {
        return delegatee
                .getWaypoints()
                .stream()
                .map(WebRequestWaypointData::new)
                .map(WaypointData.class::cast)
                .toList();
    }

    @Override
    public UUID getPurposeId() {
        return delegatee.getPurposeId();
    }
}
