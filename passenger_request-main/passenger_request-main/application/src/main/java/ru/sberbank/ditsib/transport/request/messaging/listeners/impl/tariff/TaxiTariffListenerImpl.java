package ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.request.service.TaxiTariffService;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class TaxiTariffListenerImpl implements Consumer<Message<TaxiTariffMessage>> {
    
    private final TaxiTariffService taxiTariffService;
    
    private void handle(TaxiTariffMessage message) {
        if (message.deleted()) {
            var optional = taxiTariffService.getOptionalById(message.getId());
            if (optional.isPresent()) {
                taxiTariffService.delete(optional.get());
                log.info("Тариф такси ID '{}' был удален", message.getId());
            }
            return;
        }
        
        taxiTariffService.save(message.getId(), message);
        log.info("Тариф такси ID '{}' был записан / отредактирован", message.getId());
    }
    
    public void accept(Message<TaxiTariffMessage> message) {
        handle(message.getPayload());
    }
}
