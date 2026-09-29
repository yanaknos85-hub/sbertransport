package ru.sber.transport.request.external.web.model;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.web.util.CollectionUtils;
import ru.sber.transport.web.model.ListExternalRequest;
import ru.sber.transport.web.model.State;
import ru.sber.transport.web.model.TariffType;
import ru.sber.transport.web.model.Waypoint;

/**
 * Данные заказа поездки.
 */
public class ListResponseTripOrderData extends ListExternalRequest {

    /**
     * Создание нового объекта.
     *
     * @param delegatee объект для обертывания.
     */
    public ListResponseTripOrderData(TripOrderData delegatee) {
        setId(delegatee.getId());
        setFactCost(delegatee.getActual().getCost());
        setPlannedCost(delegatee.getPlanned().getCost());
        setPlannedDuration(delegatee.getPlanned().getDuration().toString());
        setPassengerId(delegatee.getPassenger().getId());
        setTripDate(delegatee.getDate());
        setWaypoints(Optional.ofNullable(delegatee.getWaypoints()).orElseGet(List::of).stream().map(WebResponseWaypoint::new).map(Waypoint.class::cast).toList());
        setPurposeId(delegatee.getPurposeId());
        setStatus(State.valueOf(delegatee.getStatus().name()));
        setHumanReadableId(delegatee.getHumanReadableId());
        setComment(delegatee.getComment());
        setTariff(TariffType.valueOf(delegatee.getTariff().name()));
        setReason(delegatee.getReason());
        setLink(Optional.ofNullable(delegatee.getLink()).map(URI::toASCIIString).orElse(null));
        setReceipt(delegatee.getReceipt());
        setAssessments(Optional.ofNullable(delegatee.getAssessments()).map(WebResponseAssessments::new).orElse(null));
        setFraudComment(CollectionUtils.toFraudComment(delegatee.getFraud()));
        setReceiptLink(delegatee.getReceiptLink());
    }

}
