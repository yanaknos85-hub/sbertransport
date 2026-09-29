package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.DelegateRepository;
import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.service.DelegateService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса работы с делегатами.
 */
@RequiredArgsConstructor
@Transactional
@Component
class DelegateServiceImpl implements DelegateService {
    
    private final DelegateRepository delegateRepository;
    
    @Override
    public Optional<Delegate> get(UUID id) {
        return delegateRepository.findById(id);
    }
    
    @Override
    public void delete(Delegate delegate) {
        delegateRepository.delete(delegate);
    }
    
    @Override
    public void save(Delegate delegate) {
        delegateRepository.save(delegate);
    }
    
    @Override
    public List<Delegate> getEmployeeDelegateRecords(Employee employee) {
        return delegateRepository.findByDelegateIdAndDateBetweenStartAndEnd(employee.getId(), LocalDate.now());
    }
    
    @Override
    public List<Delegate> getDelegatesBySupervisor(Employee employee) {
        return delegateRepository.findAllBySupervisorIdAndDateBetweenStartAndEnd(employee.getId(), LocalDate.now());
    }
}
