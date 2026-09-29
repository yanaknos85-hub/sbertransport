package ru.sberbank.ditsib.transport.tariff.messaging.listener;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.messaging.messages.*;
import ru.sberbank.ditsib.transport.tariff.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.Contractor;
import ru.sberbank.ditsib.transport.tariff.database.model.Organization;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.GeoZone;
import ru.sberbank.ditsib.transport.tariff.mappers.PositionMapper;
import ru.sberbank.ditsib.transport.tariff.mappers.DepartmentMapper;
import ru.sberbank.ditsib.transport.tariff.service.*;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Конфигурация слушателей.
 */
@Configuration
class ListenerConfig {
    
    @Bean
    Consumer<Message<ContractorMessage>> contractorInput(ContractorService contractorService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            
            var contractor = contractorService.get(message.getId()).orElseGet(Contractor::new);
            contractor.setId(message.getId());
            contractor.setName(message.name());
            contractor.setRegionIds(message.regionIds());
            contractor.setIntegrationEmail(message.integrationEmail());
            contractor.setActive(!message.deleted());
            contractor.setMsrn(message.msrn());
            contractor.setTin(message.tin());
            contractor.setIntegrationType(message.integrationType());
            contractor.setLogin(message.login());
            contractor.setPassword(message.password());
            contractorService.save(contractor);
        };
    }
    
    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentMapper departmentMapper, DepartmentService departmentService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            
            if (!message.isDeleted()) {
                departmentService.save(departmentMapper.fromMessage(message));
            }
        };
    }
    
    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(EmployeeService employeeService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var headers = rawMessage.getHeaders();
            var id = headers.get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            
            var employeeId = Optional.ofNullable(id).orElse(message.getId());
            if (!message.isDeleted()) {
                var employee = employeeService.get(employeeId).orElse(new Employee());
                employee.setDepartmentId(message.getDepartmentId());
                employee.setFirstName(message.getFirstName());
                employee.setId(message.getId());
                employee.setLastName(message.getLastName());
                employee.setPatronymic(message.getPatronymic());
                employee.setUserId(message.getUserId());
                employee.setOrganizationId(message.getOrganizationId());
                employee.setPositionId(message.getPositionId());
                employeeService.save(employee);
            }
        };
    }
    
    @Bean
    Consumer<Message<GeoZoneMessage>> geoZoneInput(GeoZoneService geoZoneService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            
            var geoZone = geoZoneService.get(message.getId()).orElseGet(GeoZone::new);
            
            if (!message.isDeleted()) {
                geoZone.setId(message.getId());
                geoZone.setCode(message.getCode());
                geoZone.setName(message.getName());
                geoZone.setParentId(message.getParentId());
                
                geoZoneService.save(geoZone);
            }
        };
    }
    
    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationService organizationService) {
        return rawMessage -> {
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var message = rawMessage.getPayload();
            
            var organizationId = Optional.ofNullable(id).orElse(message.getId());
            if (!message.isDeleted()) {
                organizationService.save(Organization.builder().id(organizationId).digitId(message.getDigitId())
                                                     .name(message.getOfficialName()).build());
            }
        };
    }
    
    @Bean
    Consumer<Message<PositionMessage>> positionsInput(PositionMapper mapper, PositionRepository positionRepository) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (message.isDeleted()) {
                positionRepository.findById(message.getId()).ifPresent(positionRepository::delete);
            } else {
                positionRepository.save(mapper.toModel(message));
            }
        };
    }
}
