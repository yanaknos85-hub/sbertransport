package ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.tariff.messaging.GroupTransferTariffMessage;
import ru.sberbank.ditsib.transport.request.database.dao.GroupTransferTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.GroupTransferTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class GroupTransferTariffListenerImpl implements Consumer<Message<GroupTransferTariffMessage>> {
    
    private final GroupTransferTariffRepository groupTransferTariffRepository;
    
    private final TariffMapper tariffMapper;
    
    private void handle(GroupTransferTariffMessage message) {
        var optional = groupTransferTariffRepository.findById(message.getId());
        if (message.deleted()) {
            if (optional.isPresent()) {
                groupTransferTariffRepository.delete(optional.get());
                log.info("Тариф группового трансфера ID '{}' был удален", message.getId());
            }
            return;
        }
        var tariff = optional.orElse(GroupTransferTariff.builder().build());
        tariffMapper.map(tariff, message);
        groupTransferTariffRepository.save(tariff);
        log.info("Тариф группового трансфера ID '{}' был записан / отредактирован", message.getId());
    }
    
    public void accept(Message<GroupTransferTariffMessage> message) {
        handle(message.getPayload());
    }
}
