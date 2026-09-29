package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * Сервис публикации поездок на такси
 */
public interface PublishTripService {
    /**
     * Сохранить одиночную поездку на такси
     *
     * @param request заявка на одиночную поездку на такси
     * @return сохраненная поездка
     */
    SingleTaxiTrip publishNewSingleTrip(RequestForTaxi request);

    /**
     * Сохранить поездку на трансфере
     *
     * @param request заявка на поездку на трансфере
     * @return сохраненная поездка
     */
    GroupTransferTrip publishNewGroupTransferTrip(RequestForGroupTransfer request);

    /**
     * Сохранить совметную поездку на такси
     *
     * @param rideId         заявка на совместную поездку на такси
     * @param activeRequests список связанных активных заявок
     * @return сохраненная поездка
     */
    CoopTaxiTrip publishNewCoopTrip(UUID rideId, UUID tariffId, UUID outcomeTariffId, @NotEmpty List<RequestForTaxi> activeRequests);

    /**
     * Сохранить одиночную поездку на такси
     *
     * @param singleTaxiTrip индивидуальная поездка на такси
     * @param taxiTariff     используемый тариф
     */
    void publishSingleTrip(SingleTaxiTrip singleTaxiTrip, RequestForTaxi request, TaxiTariff taxiTariff);

    /**
     * Сохранить поездку на трансфере
     *
     * @param trip поездка
     */
    void publishGroupTransferTrip(GroupTransferTrip trip);


    /**
     * Сохранить совметную поездку на такси
     *
     * @param coopTaxiTrip совместная поездка
     */
    void publishCoopTrip(CoopTaxiTrip coopTaxiTrip, List<RequestForTaxi> activeRequests, TaxiTariff tariff);

}
