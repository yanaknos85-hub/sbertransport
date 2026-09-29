package ru.sber.transport.telemechanic.provider.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.model.Department;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.database.model.Position;
import ru.sber.transport.telemechanic.mapper.EmployeeMapper;
import ru.sber.transport.telemechanic.provider.EmployeeProvider;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.PositionService;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;

import java.util.Objects;

/**
 * Реализация провайдера сотрудников.
 */
@Transactional
@RequiredArgsConstructor
@Slf4j
@Component
public class EmployeeProviderImpl implements EmployeeProvider {
    
    public static final String NO_ENTITY_MESSAGE =
            "Can't save employee id:{}, personnelNumber:{}, positionId:{}, organizationId:{}, departmentId:{}, ";
    public static final String NO_ORGANIZATION_MESSAGE = NO_ENTITY_MESSAGE.concat("organization isn't present");
    public static final String NO_DEPARTMENT_MESSAGE = NO_ENTITY_MESSAGE.concat("department isn't present");
    public static final String NO_POSITION_MESSAGE = NO_ENTITY_MESSAGE.concat("position isn't present");
    public static final String AWAITING_ENTITY_MESSAGE =
            "Can't save employee id:%s, personnelNumber:%s, positionId:%s, organizationId:%s, departmentId:%s, ";
    public static final String AWAITING_ORGANIZATION_MESSAGE = AWAITING_ENTITY_MESSAGE.concat("awaiting organization synchronization");
    public static final String AWAITING_DEPARTMENT_MESSAGE = AWAITING_ENTITY_MESSAGE.concat("awaiting department synchronization");
    public static final String AWAITING_POSITION_MESSAGE = AWAITING_ENTITY_MESSAGE.concat("awaiting position synchronization");
    private final EmployeeService employeeService;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final PositionService positionService;
    private final EmployeeMapper employeeMapper;
    
    @Override
    public void delete(EmployeeMessage message) {
        employeeService.get(message.getId()).ifPresent(employeeService::delete);
    }
    
    @Override
    public void save(EmployeeMessage message) {
        if (Objects.isNull(message.getOrganizationId())) {
            log.info(NO_ORGANIZATION_MESSAGE,
                     message.getId(),
                     message.getPersonnelNumber(),
                     message.getPositionId(),
                     message.getOrganizationId(),
                     message.getDepartmentId());
        } else if (Objects.isNull(message.getDepartmentId())) {
            log.info(NO_DEPARTMENT_MESSAGE,
                     message.getId(),
                     message.getPersonnelNumber(),
                     message.getPositionId(),
                     message.getOrganizationId(),
                     message.getDepartmentId());
        } else if (Objects.isNull(message.getPositionId())) {
            log.info(NO_POSITION_MESSAGE,
                     message.getId(),
                     message.getPersonnelNumber(),
                     message.getPositionId(),
                     message.getOrganizationId(),
                     message.getDepartmentId());
        } else {
            var optionalOrganization = organizationService.get(message.getOrganizationId());
            if (optionalOrganization.isEmpty()) {
                organizationService.saveGrpcEntity(AWAITING_ORGANIZATION_MESSAGE.formatted(message.getId(),
                                                                                           message.getPersonnelNumber(),
                                                                                           message.getPositionId(),
                                                                                           message.getOrganizationId(),
                                                                                           message.getDepartmentId()),
                                                   message.getOrganizationId());
            }
            var optionalDepartment = departmentService.get(message.getDepartmentId());
            if (optionalDepartment.isEmpty()) {
                departmentService.saveGrpcEntity(AWAITING_DEPARTMENT_MESSAGE.formatted(message.getId(),
                                                                                       message.getPersonnelNumber(),
                                                                                       message.getPositionId(),
                                                                                       message.getOrganizationId(),
                                                                                       message.getDepartmentId()),
                                                 message.getDepartmentId());
            }
            var optionalPosition = positionService.get(message.getPositionId());
            if (optionalPosition.isEmpty()) {
                positionService.saveGrpcEntity(AWAITING_POSITION_MESSAGE.formatted(message.getId(),
                                                                                   message.getPersonnelNumber(),
                                                                                   message.getPositionId(),
                                                                                   message.getOrganizationId(),
                                                                                   message.getDepartmentId()),
                                               message.getPositionId());
            }
            var optionalDbEntity = employeeService.get(message.getId());
            if (optionalDbEntity.isPresent()) {
                employeeService.save(optionalDbEntity.get()
                                                     .withActive(true)
                                                     .withOrganization(optionalOrganization.orElse(
                                                     Organization.builder()
                                                                 .id(message.getOrganizationId())
                                                                 .build()))
                                                     .withDepartment(optionalDepartment.orElse(
                                                     Department.builder()
                                                               .id(message.getDepartmentId())
                                                               .build()))
                                                     .withPosition(optionalPosition.orElse(
                                                     Position.builder()
                                                             .id(message.getPositionId())
                                                             .build()))
                                                     .withFirstName(message.getFirstName())
                                                     .withLastName(message.getLastName())
                                                     .withPatronymic(message.getPatronymic())
                                                     .withHumanReadableId(message.getHumanReadableId())
                                                     .withPersonnelNumber(message.getPersonnelNumber())
                                                     .withMobilePhone(message.getMobilePhone())
                                                     .withUserId(message.getUserId()));
            } else {
                employeeService.save(employeeMapper.employeeMessageToEmployee(message));
            }
        }
    }
}
