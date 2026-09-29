package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.ContractMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.ContractListener;
import ru.sberbank.ditsib.transport.reports.model.Contractor;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;
import ru.sberbank.ditsib.transport.reports.service.ContractService;
import ru.sberbank.ditsib.transport.reports.service.ContractorService;

@RequiredArgsConstructor
@Component("contractInput")
public class ContractListenerImpl implements ContractListener   {
    private final ContractService contractService;
    private final ContractorService contractorService;
    
    @Override
    public void handleContracts(ContractMessage message) {
        if (message.isDeleted()) {
            contractService.deactivate(message.getId());
        } else {
            var contract = contractService.findById(message.getId()).orElse(null);
            var contractor =
                    contractorService.findById(message.getId())
                                     .orElse(contractorService.save(Contractor.builder().id(message.getContractorId()).build()));
            if (contract == null) {
                var newContract = Contract.builder()
                                          .id(message.getId())
                                          .active(message.isActive())
                                          .contractor(contractor)
                                          .contractNumber(message.getContractNumber())
                                          .serviceType(TransportServiceType.valueOf(message.getServiceType()))
                                          .transportType(TransportTypeEnum.valueOf(message.getTransportType()))
                                          .creationTime(message.getCreationTime())
                                          .startDate(message.getStartDate())
                                          .endDate(message.getEndDate())
                                          .sum(message.getSum())
                                          .includeVat(message.isIncludeVat())
                                          .uvhd(message.getUvhd())
                                          .build();
                contractService.save(newContract);
            } else {
                contract = contract.toBuilder()
                                   .active(message.isActive())
                                   .contractor(contractor)
                                   .contractNumber(message.getContractNumber())
                                   .serviceType(TransportServiceType.valueOf(message.getServiceType()))
                                   .transportType(TransportTypeEnum.valueOf(message.getTransportType()))
                                   .creationTime(message.getCreationTime())
                                   .startDate(message.getStartDate())
                                   .endDate(message.getEndDate())
                                   .sum(message.getSum())
                                   .includeVat(message.isIncludeVat())
                                   .uvhd(message.getUvhd())
                                   .build();
                contractService.save(contract);
            }
        }
    }
}
