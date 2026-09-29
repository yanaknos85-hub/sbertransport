package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.messaging.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.ContractorListener;
import ru.sberbank.ditsib.transport.reports.model.Contractor;
import ru.sberbank.ditsib.transport.reports.service.ContractorService;

@RequiredArgsConstructor
@Component("contractorInput")
public class ContractorListenerImpl implements ContractorListener {

    private final ContractorService contractorService;

    @Override
    public void handleContractors(ContractorMessage message) {
        var contractor = contractorService.findById(message.getId()).orElse(null);
        if (!message.isDeleted()) {
            if (contractor == null) {
                contractorService.save(Contractor.builder().id(message.getId()).name(message.getName()).build());
            } else {
                contractor = contractor.toBuilder().name(message.getName()).build();
                contractorService.save(contractor);
            }
        } else {
            contractorService.delete(contractor);
        }
    }
}
