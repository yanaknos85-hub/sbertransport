package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.CorporateCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.service.CorporateCarsharingService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorporateCarsharingServiceImpl implements CorporateCarsharingService {
    
    private final CorporateCarsharingRepository carsharingRepository;
    
    @Override
    public List<CorporateCarsharing> getByOrganizationId(UUID organizationId) {
        return carsharingRepository.findByOrganizationId(organizationId);
    }
    
    @Override
    public CorporateCarsharing getByContractIdAndOrganizationId(UUID contractId, UUID organizationId) {
        Optional<CorporateCarsharing> carsharingOptional =
                carsharingRepository.findByContractIdAndOrganizationId(contractId, organizationId);
    
        // корп.каршеринги генерируются в слушателе контрактов. Причиной ошибок м.б. неконсистентность данных
        if (carsharingOptional.isEmpty()) {
            log.error("Для контракта ID '{}' и корп.клиента '{}' не был сгенерирован корп.каршеринг!" +
                      "Проверьте консистентность БД в части корп.каршерингов, контрактов и тарифов каршеринга!",
                      contractId, organizationId);
            throw new EntityNotFoundException(CorporateCarsharing.class, Map.of("contractId", contractId, "organizationId", organizationId));
        }
        return carsharingOptional.get();
    }
    
    @Override
    public boolean checkEmployeesSetContainsUuid(Set<Employee> employees, UUID employeeId) {
        return employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toSet()).contains(employeeId);
    }
    
    
}
