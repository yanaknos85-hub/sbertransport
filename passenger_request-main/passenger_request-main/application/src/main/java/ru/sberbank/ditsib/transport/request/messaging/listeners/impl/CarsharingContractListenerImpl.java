package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.ContractMessage;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingContractRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CorporateCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingContract;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharing;
import ru.sberbank.ditsib.transport.request.mappers.CarsharingContractMapper;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Slf4j
public class CarsharingContractListenerImpl implements Consumer<Message<ContractMessage>> {
    
    private final CarsharingContractRepository contractRepository;
    private final CorporateCarsharingRepository carsharingRepository;
    private final CarsharingContractMapper mapper;
    
    private void handle(ContractMessage message) {
        // записывать только контракты для каршеринга
        if (!message.getTransportType().equals(TransportTypeEnum.CARSHARING.name())) {
            log.info("Контракт {} ID '{}' был проигнорирован", message.getTransportType(), message.getId());
            return;
        }
        final UUID contractId = message.getId();
        final boolean hasOldContract = contractRepository.existsById(contractId);
        
        // использовать soft delete для контракта, чтобы не сломалось отображение информации на фронте
        CarsharingContract contract = contractRepository.save(mapper.messageToEntity(message));
        log.info("Был сохранен Контракт с каршерингом ID '{}'", contractId);
        
        if (!contract.isActive() || contract.isDeleted()) {
            deactivateOrDeleteOldCarsharings(contract);
        } else {
            // Если контракт активен, создать новые или редактировать старые каршеринги
            // Если контракт уже был в БД, предварительно деактивировать / удалить старые каршеринги
            if (hasOldContract) {
                deactivateOrDeleteOldCarsharings(contract);
            }
            for (UUID org : contract.getOrganizations()) {
                Optional<CorporateCarsharing> optionalCarhsring =
                        carsharingRepository.findByContractIdAndOrganizationId(contractId, org);
                // если каршеринг найден, редактировать каршеринг
                if (optionalCarhsring.isPresent()) {
                    CorporateCarsharing oldCarsharing = optionalCarhsring.get();
                    oldCarsharing.setContract(contract);
                    oldCarsharing.setActive(true);
                    carsharingRepository.save(oldCarsharing);
                    log.info("Каршеринг c № контракта '{}' для корп.клиента '{}' был отредактирован", contractId, org);
                }
                // если каршеринг не найден, создать новый каршеринг
                else {
                    carsharingRepository.save(createCarsharing(contract, org));
                    log.info("Каршеринг c № контракта '{}' для корп.клиента '{}' был записан в БД", contractId, org);
                }
            }
        }
    }
    
    /**
     * Создать сущность Корпоративного каршеринга
     *
     * @param contract контракт
     * @param organizationId ID конкретного корп.клиента
     *
     * @return сущность Корпоративного каршеринга
     */
    private CorporateCarsharing createCarsharing(CarsharingContract contract, UUID organizationId) {
        return CorporateCarsharing.builder()
                                  .contract(contract)
                                  .organizationId(organizationId)
                                  .build();
    }
    
    /**
     * Деактивировать контракты, у которых список сотрудников не пуст, или удалить контракты, у которых он пустой
     *
     * @param contract контракт
     */
    private void deactivateOrDeleteOldCarsharings(CarsharingContract contract) {
        UUID contractId = contract.getId();
        
        for (CorporateCarsharing carsharing : carsharingRepository.findByContractId(contractId)) {
            UUID organizationId = carsharing.getOrganizationId();
            //деактивировать, если список сотрудников не пуст, и удалить, если он пустой
            if (!carsharing.getJoinedEmployees().isEmpty()) {
                carsharing.setActive(false);
                carsharingRepository.save(carsharing);
                log.info("Каршеринг с № контракта '{}' для корп.клиента '{}' был деактивирован", contractId, organizationId);
            } else {
                carsharingRepository.delete(carsharing);
                log.info("Каршеринг с № контракта '{}' для корп.клиента '{}' был удален", contractId, organizationId);
            }
        }
    }
    
    public void accept(Message<ContractMessage> message) {
        handle(message.getPayload());
    }
}
