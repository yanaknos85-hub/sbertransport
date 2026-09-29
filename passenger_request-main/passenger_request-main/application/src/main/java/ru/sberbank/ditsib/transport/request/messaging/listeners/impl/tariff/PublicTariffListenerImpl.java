package ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.tariff.messaging.PublicTariffMessage;
import ru.sberbank.ditsib.transport.request.service.PublicTariffService;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class PublicTariffListenerImpl implements Consumer<Message<PublicTariffMessage>> {
    
    private final PublicTariffService publicTariffService;
    
    private void handle(PublicTariffMessage message) {
        if (message.deleted()) {
            var optional = publicTariffService.getOptionalById(message.getId());
            if (optional.isPresent()) {
                publicTariffService.delete(optional.get());
                log.info("Тариф общественного транспорта ID '{}' был удален", message.getId());
            }
            return;
        }
        
        publicTariffService.save(message.getId(), message);
        log.info("Тариф общественного транспорта ID '{}' был записан / отредактирован", message.getId());
    }
    
    public void accept(Message<PublicTariffMessage> message) {
        handle(message.getPayload());
    }
}
