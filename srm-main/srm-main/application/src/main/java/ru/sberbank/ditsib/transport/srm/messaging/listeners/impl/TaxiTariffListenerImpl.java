package ru.sberbank.ditsib.transport.srm.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.srm.mapper.TariffMapper;
import ru.sberbank.ditsib.transport.srm.messaging.listeners.TaxiTariffListener;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.service.TaxiTariffService;


@Component("taxiTariffInput")
@RequiredArgsConstructor
@Slf4j
public class TaxiTariffListenerImpl implements TaxiTariffListener {
    
    private final TaxiTariffService taxiTariffService;
    private final TariffMapper tariffMapper;
    
    @Override
    public void handle(TaxiTariffMessage message) {
        if (message.getId() == null) {
            log.warn("SRM: TaxiTariffListener: tariff came with id null!");
            return;
        } else {
            log.debug("SRM: TaxiTariffListener: tariff came with id " + message.getId());
            log.debug("SRM: TaxiTariffListener: tariff distanceDeviationKm " + message.getDistanceDeviationKm());
            log.debug("SRM: TaxiTariffListener: tariff savingsDeviationPct " + message.getSavingsDeviationPct());
            log.debug("SRM: TaxiTariffListener: tariff timeDeviationMin " + message.getTimeDeviationMin());
        }
        if (message.isDeleted()) {
            var tariff = taxiTariffService.findById(message.getId());
            tariff.ifPresent(taxiTariffService::delete);
        } else {
            TaxiTariff taxiTariff = taxiTariffService.findById(message.getId()).orElse(null);
            if (taxiTariff == null) { // new tariff
                log.debug("SRM: TaxiTariffListener: tariff with id " + message.getId() + " is new");
                taxiTariff = tariffMapper.taxiTariffMessageToModel(message);
                taxiTariff = taxiTariff.toBuilder()
                                       .contractId(message.getContractId())
                                       .organizationId(message.getOrganizationId())
                                       .transportType(TransportTypeEnum.valueOf(message.getTransportType()))
                                       .serviceType(TransportServiceType.valueOf(message.getServiceType()))
                                       .build();
            } else { // old tariff changing
                log.debug("SRM: TaxiTariffListener: tariff with id " + message.getId() + " is old");
                taxiTariff.setHumanReadableId(message.getHumanReadableId());
                taxiTariff.setMinRideDistanceCost(message.getMinRideDistanceCost());
                taxiTariff.setCarServiceCost(message.getCarServiceCost());
                taxiTariff.setMinRideTimeCost(message.getMinRideTimeCost());
                taxiTariff.setRideCostPerKm(message.getRideCostPerKm());
                taxiTariff.setRideCostPerMin(message.getRideCostPerMin());
                taxiTariff.setWaitCostPerMin(message.getWaitCostPerMin());
                taxiTariff.setWaitCostPerMinIntermediate(message.getWaitCostPerMinIntermediate());
                taxiTariff.getCoopTariffParams().setMinCancelTimeMin(message.getMinCancelTimeMin());
                taxiTariff.getCoopTariffParams().setTimeDeviationMin(message.getTimeDeviationMin());
                taxiTariff.getCoopTariffParams().setDistanceDeviationKm(message.getDistanceDeviationKm());
                taxiTariff.getCoopTariffParams().setSavingsDeviationPct(message.getSavingsDeviationPct());
            }
            taxiTariffService.save(taxiTariff);
        }
    }
}
