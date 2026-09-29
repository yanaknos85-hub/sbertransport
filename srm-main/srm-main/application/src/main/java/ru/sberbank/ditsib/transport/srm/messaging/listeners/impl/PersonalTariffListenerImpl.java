package ru.sberbank.ditsib.transport.srm.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.tariff.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.srm.mapper.TariffMapper;
import ru.sberbank.ditsib.transport.srm.messaging.listeners.PersonalTariffListener;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.srm.service.PersonalTariffService;


@Component("personalTariffInput")
@RequiredArgsConstructor
@Slf4j
public class PersonalTariffListenerImpl implements PersonalTariffListener {
    
    private final PersonalTariffService personalTariffService;
    private final TariffMapper tariffMapper;
    
    @Override
    public void handle(@Payload PersonalTariffMessage message) {
        if (message.getId() == null) {
            log.warn("SRM: PersonalTariffListener: tariff came with id null!");
            return;
        } else {
            log.debug("SRM: PersonalTariffListener: tariff came with id " + message.getId());
            log.debug("SRM: PersonalTariffListener: tariff distanceDeviationKm " + message.getDistanceDeviationKm());
            log.debug("SRM: PersonalTariffListener: tariff savingsDeviationPct " + message.getSavingsDeviationPct());
            log.debug("SRM: PersonalTariffListener: tariff timeDeviationMin " + message.getTimeDeviationMin());
        }
        if (message.isDeleted()) {
            var tariff = personalTariffService.findById(message.getId());
            tariff.ifPresent(personalTariffService::delete);
        } else {
            PersonalTariff personalTariff = personalTariffService.findById(message.getId()).orElse(null);
            if (personalTariff == null) { // new tariff
                log.debug("SRM: PersonalTariffListener: tariff with id " + message.getId() + " is new");
                personalTariff = tariffMapper.personalTariffMessageToModel(message);
                personalTariff = personalTariff.toBuilder()
                                               .contractId(message.getContractId())
                                               .organizationId(message.getOrganizationId())
                                               .transportType(TransportTypeEnum.valueOf(message.getTransportType()))
                                               .serviceType(TransportServiceType.valueOf(message.getServiceType()))
                                               .build();
            } else { // old tariff changing
                log.debug("SRM: PersonalTariffListener: tariff with id " + message.getId() + " is old");
                personalTariff.setHumanReadableId(message.getHumanReadableId());
                personalTariff.setMinRideDistanceCost(message.getMinRideDistanceCost());
                personalTariff.setMinRideTimeCost(message.getMinRideTimeCost());
                personalTariff.setRideCostPerKm(message.getRideCostPerKm());
                personalTariff.setRideCostPerMin(message.getRideCostPerMin());
                personalTariff.setWaitCostPerMin(message.getWaitCostPerMin());
                personalTariff.getCoopTariffParams().setMinCancelTimeMin(message.getMinCancelTimeMin());
                personalTariff.getCoopTariffParams().setTimeDeviationMin(message.getTimeDeviationMin());
                personalTariff.getCoopTariffParams().setDistanceDeviationKm(message.getDistanceDeviationKm());
                personalTariff.getCoopTariffParams().setSavingsDeviationPct(message.getSavingsDeviationPct());
            }
            personalTariffService.save(personalTariff);
        }
    }
}
