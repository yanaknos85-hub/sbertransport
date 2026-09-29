package ru.sber.transport.request.external.web.model;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import ru.sber.transport.request.external.model.TripOrderData;
import ru.sber.transport.request.external.web.util.CollectionUtils;
import ru.sber.transport.web.model.FullExternalRequest;
import ru.sber.transport.web.model.State;
import ru.sber.transport.web.model.TariffType;
import ru.sber.transport.web.model.Waypoint;

/**
 * Данные заказа поездки.
 */
public class FullResponseTripOrderData extends FullExternalRequest {

    /**
     * Создание нового объекта.
     *
     * @param delegatee объект для обертывания.
     */
    public FullResponseTripOrderData(TripOrderData delegatee) {
        setId(delegatee.getId());
        setFactCost(delegatee.getActual().getCost());
        setPlannedCost(delegatee.getPlanned().getCost());
        setPlannedDuration(delegatee.getPlanned().getDuration().toString());
        setPassenger(new EmployeeResponse(delegatee.getPassenger()));
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
    }

}
