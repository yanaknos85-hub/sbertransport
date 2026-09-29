package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.mappers.ContractorMapper;
import ru.sberbank.ditsib.transport.request.service.ContractorService;

import java.util.Optional;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class ContractorListenerImpl implements Consumer<Message<ContractorMessage>> {
    
    private final ContractorService contractorService;
    private final ContractorMapper contractorMapper;
    
    private void handle(ContractorMessage message) {
        // использовать soft delete для контрагента, чтобы не поломать отображение информации на фронте
        if (message.deleted()) {
            Optional<Contractor> optional = contractorService.getOptional(message.getId());
            if (optional.isPresent()) {
                contractorService.softDelete(optional.get());
                log.info("Контрагент ID '{}' был деактивирован", message.getId());
            } else {
                log.info("При попытке деактивации Контрагента ID '{}', он не был найден в БД", message.getId());
            }
        } else {
            contractorService.saveOrUpdate(contractorMapper.messageToEntity(message));
            log.info("Контрагент ID '{}' был записан / отредактирован", message.getId());
        }
        
    }
    
    public void accept(Message<ContractorMessage> message) {
        handle(message.getPayload());
    }
}
