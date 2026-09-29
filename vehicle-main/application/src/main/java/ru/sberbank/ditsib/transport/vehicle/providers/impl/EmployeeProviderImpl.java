package ru.sberbank.ditsib.transport.vehicle.providers.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.vehicle.database.model.Department;
import ru.sberbank.ditsib.transport.vehicle.database.model.Position;
import ru.sberbank.ditsib.transport.vehicle.mapper.EmployeeMapper;
import ru.sberbank.ditsib.transport.vehicle.providers.EmployeeProvider;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.PositionService;

import java.util.Objects;

/**
 * Реализация провайдера сотрудников.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class EmployeeProviderImpl implements EmployeeProvider {
    
    private final EmployeeService service;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    
    private final EmployeeMapper mapper;
    
    @Override
    public void delete(EmployeeMessage message) {
        var entity = mapper.employeeMessageToEmployee(message);
        service.delete(entity);
    }
    
    @Override
    public void save(EmployeeMessage message) {
        var entity = mapper.employeeMessageToEmployee(message);
        if (Objects.isNull(entity.getDepartment())) {
            log.info("Can't save employee id:{}, personnelNumber:{}, positionId:{}, organizationId:{}, departmentId:{}, department is not present",
                     message.getId(),
                     message.getPersonnelNumber(),
                     message.getPositionId(),
                     message.getOrganizationId(),
                     message.getDepartmentId());
        } else if (departmentService.get(entity.getDepartment().getId()).isEmpty()) {
            log.info("Can't save employee id:{}, personnelNumber:{}, positionId:{}, organizationId:{}, departmentId:{}, awaiting department " +
                            "synchronization",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            throw new EntityNotFoundException(Department.class, entity.getDepartment());
        } else if (Objects.isNull(entity.getPosition()))  {
            log.info("Can't save employee id:{}, personnelNumber:{}, positionId:{}, organizationId:{}, departmentId:{}, position is not present",
                     message.getId(),
                     message.getPersonnelNumber(),
                     message.getPositionId(),
                     message.getOrganizationId(),
                     message.getDepartmentId());
        } else if (positionService.get(entity.getPosition().getId()).isEmpty()) {
            log.info("Can't save employee id:{}, personnelNumber:{}, positionId:{}, organizationId:{}, departmentId:{}, awaiting position " +
                            "synchronization",
                    message.getId(),
                    message.getPersonnelNumber(),
                    message.getPositionId(),
                    message.getOrganizationId(),
                    message.getDepartmentId());
            throw new EntityNotFoundException(Position.class, entity.getPosition());
        } else {
            service.saveOrUpdate(entity);
        }
    }
}
