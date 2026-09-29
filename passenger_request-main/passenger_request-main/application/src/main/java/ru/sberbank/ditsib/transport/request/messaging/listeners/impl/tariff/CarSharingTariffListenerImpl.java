package ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.tariff.messaging.CarSharingTariffMessage;
import ru.sberbank.ditsib.transport.request.service.CarsharingTariffService;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class CarSharingTariffListenerImpl implements Consumer<Message<CarSharingTariffMessage>> {
    
    private final CarsharingTariffService carSharingTariffService;
    
    private void handle(CarSharingTariffMessage message) {
        if (message.deleted()) {
            var optional = carSharingTariffService.getOptionalById(message.getId());
            if (optional.isPresent()) {
                carSharingTariffService.delete(optional.get());
                log.info("Тариф каршеринга ID '{}' был удален", message.getId());
            }
            return;
        }
        
        carSharingTariffService.save(message.getId(), message);
        log.info("Тариф каршеринга ID '{}' был записан / отредактирован", message.getId());
    }
    
    public void accept(Message<CarSharingTariffMessage> message) {
        handle(message.getPayload());
    }
}
