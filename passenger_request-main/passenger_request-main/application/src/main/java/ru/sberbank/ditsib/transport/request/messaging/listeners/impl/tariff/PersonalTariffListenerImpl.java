package ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.tariff.messaging.PersonalTariffMessage;
import ru.sberbank.ditsib.transport.request.service.PersonalTariffService;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class PersonalTariffListenerImpl implements Consumer<Message<PersonalTariffMessage>> {
    
    private final PersonalTariffService personalTariffService;
    
    private void handle(PersonalTariffMessage message) {
        if (message.deleted()) {
            var optional = personalTariffService.getOptionalById(message.getId());
            if (optional.isPresent()) {
                personalTariffService.delete(optional.get());
                log.info("Тариф личного транспорта ID '{}' был удален", message.getId());
            }
            return;
        }
        
        personalTariffService.save(message.getId(), message);
        log.info("Тариф личного транспорта ID '{}' был записан / отредактирован", message.getId());
    }
    
    public void accept(Message<PersonalTariffMessage> message) {
        handle(message.getPayload());
    }
}
