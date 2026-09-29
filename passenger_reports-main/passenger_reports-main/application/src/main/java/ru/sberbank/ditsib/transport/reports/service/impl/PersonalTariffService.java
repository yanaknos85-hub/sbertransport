package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.dao.PersonalTariffRepository;
import ru.sberbank.ditsib.transport.reports.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.reports.service.TariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonalTariffService implements TariffService<PersonalTariff> {
    
    private final PersonalTariffRepository tariffRepository;
    
    @Override
    public PersonalTariff save(PersonalTariff tariff) {
        return tariffRepository.save(tariff);
    }
    
    @Override
    public PersonalTariff updateOrCreate(TariffMessage message) {
        PersonalTariff personalTariff = findById(message.getId()).orElse(null);
        if (personalTariff == null) {
            personalTariff = new PersonalTariff();
            personalTariff.setId(message.getId());
        }
        personalTariff = personalTariff.toBuilder()
                                       .humanReadableId(message.getHumanReadableId())
                                       .transportType(getTransportType())
                                       .region(message.getRegion())
                                       .trustIdx((Double) message.getPriceDetails().get("trustIdx"))
                                       .build();
        
        return save(personalTariff);
    }
    
    @Override
    public Optional<PersonalTariff> findById(UUID id) {
        return tariffRepository.findById(id);
    }
    
    @Override
    public void deactivate(UUID id) {
        tariffRepository.deactivate(id);
    }
    
    @Override
    public TransportTypeEnum getTransportType() {
        return TransportTypeEnum.PERSONAL;
    }
    
}