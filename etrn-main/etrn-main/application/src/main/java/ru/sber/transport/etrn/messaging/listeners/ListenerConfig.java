package ru.sber.transport.etrn.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.etrn.database.dao.OrganizationGroupRepository;
import ru.sber.transport.etrn.database.model.Department;
import ru.sber.transport.etrn.database.model.Employee;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sber.transport.etrn.database.model.OrganizationGroup;
import ru.sber.transport.etrn.mapper.EmployeeMapper;
import ru.sber.transport.etrn.service.DepartmentService;
import ru.sber.transport.etrn.service.EmployeeService;
import ru.sber.transport.etrn.service.OrganizationService;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class ListenerConfig {

    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentService departmentService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.isDeleted()) {
                departmentService.findById(payload.getId()).ifPresent(departmentService::delete);
            } else {
                departmentService.save(Department.builder()
                        .id(payload.getId())
                        .humanReadableId(payload.getHumanReadableId())
                        .parentId(payload.getParentId())
                        .organizationId(payload.getOrganizationId())
                        .departmentName(payload.getDepartmentName())
                        .build());
            }
        };
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(
            EmployeeMapper mapper,
            EmployeeService employeeService,
            DepartmentService departmentService
    ) {
        return message -> {
            var payload = message.getPayload();
            var id = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var employeeId = Optional.ofNullable(id).orElse(payload.getId());
            if (payload.isDeleted()) {
                employeeService.findById(employeeId).ifPresent(employeeService::delete);
            } else {
                Employee employee = employeeService.findById(employeeId).orElse(null);
                if (employee == null) {
                    employeeService.save(mapper.employeeMessageToEmployeeEntity(payload));
                } else {
                    if (payload.getDepartmentId() != null) {
                        var department = departmentService.findOrCreateById(payload.getDepartmentId());
                        employee.setDepartment(department);
                    }
                    Department department = departmentService.findOrCreateById(payload.getDepartmentId());
                    employeeService.save(employee.toBuilder()
                            .department(department)
                            .firstName(payload.getFirstName())
                            .lastName(payload.getLastName())
                            .patronymic(payload.getPatronymic())
                            .userId(payload.getUserId())
                            .personnelNumber(payload.getPersonnelNumber())
                            .humanReadableId(payload.getHumanReadableId()).build());
                }
            }
        };
    }

    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInput(
            OrganizationService organizationService,
            OrganizationGroupRepository groupRepository
    ) {
        return message -> {
            var id = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var payload = message.getPayload();
            var organizationId = Optional.ofNullable(id).orElse(payload.getId());
            OrganizationGroup organizationGroup = null;
            if (payload.getOrganizationGroup() != null && payload.getOrganizationGroup().id() != null) {
                organizationGroup = groupRepository.save(OrganizationGroup.builder()
                        .id(payload.getOrganizationGroup().id())
                        .internal(payload.getOrganizationGroup().internal())
                        .name(payload.getOrganizationGroup().name())
                        .build());
            }
            if (payload.isDeleted()) {
                organizationService.delete(organizationService.get(organizationId).orElse(null));
            } else {
                organizationService.save(Organization.builder().id(organizationId).officialName(payload.getOfficialName())
                        .digitId(payload.getDigitId()).organizationGroup(organizationGroup).build());
            }
        };
    }
}