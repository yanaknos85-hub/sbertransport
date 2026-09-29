package ru.sber.transport.dispatcher.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.service.ContractorCounter;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
class ContractorCounterImpl implements ContractorCounter {

    private final ContractorRepository contractorRepository;

    @Value("${staff.limit:5000}")
    private int limit;

    @Override
    public synchronized @NonNull Contractor changeCount(@NonNull Contractor source, int count) {
        var data = contractorRepository.getReferenceById(source.getId());
        var employeeCount = data.getEmployeeCount();
        var newCount = employeeCount + count;
        data.setEmployeeCount(newCount);
        return contractorRepository.saveAndFlush(data);
    }

    @Override
    public boolean canAddStaff(@NonNull Contractor target) {
        return Optional.ofNullable(target.getId())
                .flatMap(contractorRepository::findById)
                .map(Contractor::getEmployeeCount)
                .map(count -> count < limit)
                .orElse(true);
    }
}
