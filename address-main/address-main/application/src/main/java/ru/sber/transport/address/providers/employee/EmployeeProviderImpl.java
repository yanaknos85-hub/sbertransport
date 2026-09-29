package ru.sber.transport.address.providers.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.address.business.model.Employee;
import ru.sber.transport.address.business.provider.EmployeeProvider;
import ru.sber.transport.address.providers.employee.mappers.EmployeeDatabaseMapper;
import ru.sber.transport.database.addresses.tables.records.EmployeeRecord;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
@RequiredArgsConstructor
class EmployeeProviderImpl implements EmployeeProvider, JooqRepository<ru.sber.transport.database.addresses.tables.Employee, EmployeeRecord, UUID> {

    private final EmployeeDatabaseMapper mapper;

    @Override
    public Optional<Employee> get(UUID id) {
        return findById(id).map(mapper::toBusiness);
    }

    @Override
    public void save(Employee source) {
        save(mapper.toDatabase(source));
    }

    @Override
    public ru.sber.transport.database.addresses.tables.Employee table() {
        return ru.sber.transport.database.addresses.tables.Employee.EMPLOYEE;
    }
}
