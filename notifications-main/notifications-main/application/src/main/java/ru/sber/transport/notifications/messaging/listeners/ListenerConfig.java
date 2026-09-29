package ru.sber.transport.notifications.messaging.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.approvals.messages.ApproveSharedRideMessage;
import ru.sber.transport.approvals.messages.ApproveTripRequestMessage;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sber.transport.dispatcher.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.ContractorUpdateTripMessage;
import ru.sber.transport.dispatcher.messages.DriverMessage;
import ru.sber.transport.limits.messaging.ApproveLimitRequestMessage;
import ru.sber.transport.messages.corporate.avro.DepartmentMessage;
import ru.sber.transport.messages.corporate.avro.EmployeeMessage;
import ru.sber.transport.messages.corporate.avro.OrganizationMessage;
import ru.sber.transport.notifications.database.dao.RoleRepository;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.messages.limits.ApproveLimitRequestRepository;
import ru.sber.transport.notifications.database.dao.messages.trip_request.TripPurposeRepository;
import ru.sber.transport.notifications.database.model.TripPurpose;
import ru.sber.transport.notifications.database.model.contractor.Contractor;
import ru.sber.transport.notifications.database.model.contractor.Dispatcher;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.coprorate.Department;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.coprorate.Organization;
import ru.sber.transport.notifications.database.model.deadline.DeadlineSettings;
import ru.sber.transport.notifications.database.model.limits.ApproveLimitRequest;
import ru.sber.transport.notifications.database.model.request.TaxiTrip;
import ru.sber.transport.notifications.database.model.request.Vehicle;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;
import ru.sber.transport.notifications.database.model.tariff.TaxiTariff;
import ru.sber.transport.notifications.database.model.trip.Trip;
import ru.sber.transport.notifications.mapper.RoleMapper;
import ru.sber.transport.notifications.mapper.TripMapper;
import ru.sber.transport.notifications.mapper.VehicleMapper;
import ru.sber.transport.notifications.mapper.deadline.DeadlineSettingsMapper;
import ru.sber.transport.notifications.mapper.tariff.TariffMapper;
import ru.sber.transport.notifications.messaging.listeners.mappers.*;
import ru.sber.transport.notifications.messaging.message.NotificationMessage;
import ru.sber.transport.notifications.messaging.message.TripMessage;
import ru.sber.transport.notifications.services.*;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.request.messaging.TaxiTripMessage;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.messaging.messages.DelegateMessage;
import ru.sberbank.ditsib.transport.messaging.messages.TripPurposeMessage;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class ListenerConfig {

    @Bean
    public Consumer<Message<ContractorMessage>> contractorInput(ContractorService contractorService,
                                                                ContractorMapper contractorMapper) {

        return contractorInputSsl(contractorService, contractorMapper);
    }

    @Bean
    public Consumer<Message<TripMessage>> tripInput(Processor<Trip> tripProcessor, TripMapper tripMapper) {
        return tripInputSsl(tripProcessor, tripMapper);
    }

    @Bean
    public Consumer<Message<DriverMessage>> driverInput(DriverService driverService, DriverMapper driverMapper) {
        return driverInputSsl(driverService, driverMapper);
    }

    @Bean
    public Consumer<Message<ContractorUpdateTripMessage.Dispatcher>> dispatcherInput(DispatcherService dispatcherService,
                                                                                     DispatcherMapper dispatcherMapper) {
        return dispatcherInputSsl(dispatcherService, dispatcherMapper);
    }

    @Bean
    public Consumer<Message<ContractorUpdateTripMessage.Vehicle>> vehicleInput(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        return vehicleInputSsl(vehicleService, vehicleMapper);
    }

    @Bean
    public Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInput(DeadlineSettingsService settingsService, DeadlineSettingsMapper mapper) {
        return deadlineSettingsInputSsl(settingsService, mapper);
    }

    @Bean
    public Consumer<Message<DelegateMessage>> delegateInput(DelegateHandler handler) {
        return delegateInputSsl(handler);
    }

    @Bean
    public Consumer<Message<ApproveSharedRideMessage>> sharedRideApproveInput(ApproveSharedRideHandler handler) {
        return sharedRideApproveInputSsl(handler);
    }

    @Bean
    public Consumer<Message<TaxiTariffMessage>> taxiTariffInput(TariffService<TaxiTariff> tariffService, TariffMapper tariffMapper) {
        return taxiTariffInputSsl(tariffService, tariffMapper);
    }

    @Bean
    public Consumer<Message<TripPurposeMessage>> tripPurposeInput(TripPurposeRepository tripPurposeRepository, TripPurposeMapper mapper) {
        return tripPurposeInputSsl(tripPurposeRepository, mapper);
    }

    @Bean
    public Consumer<Message<RequestMessage>> tripRequestInput(TripRequestHandler listener) {
        return tripRequestInputSsl(listener);
    }

    @Bean
    public Consumer<Message<TaxiTripMessage>> taxiTripInput(TaxiTripService taxiTripService, TaxiTripMapper mapper, Processor<TaxiTrip> tripProcessor) {
        return taxiTripInputSsl(taxiTripService, mapper, tripProcessor);
    }

    @Bean
    public Consumer<Message<ApproveTripRequestMessage>> tripApproveInput(TripApproveHandler handler) {
        return tripApproveInputSsl(handler);
    }

    @Bean
    public Consumer<Message<ApproveLimitRequestMessage>> approveLimitsRequestInput(Processor<ApproveLimitRequest> processor, ApproveLimitRequestRepository repository, ApproveLimitMapper mapper) {
        return approveLimitsRequestInputSsl(processor, repository, mapper);
    }

    @Bean
    public Consumer<Message<RoleMessage>> rolesInput(RoleRepository repository, RoleMapper mapper) {
        return rolesInputSsl(repository, mapper);
    }

    @Bean
    public Consumer<Message<TripMessage>> tripInputSsl(Processor<Trip> tripProcessor, TripMapper tripMapper) {
        return rawMessage -> {
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var message = rawMessage.getPayload();
            var trip = tripMapper.toModel(message);
            trip.setId(id);
            try {
                tripProcessor.process(trip);
            } catch (JsonProcessingException e) {
                log.warn("Message of trip %s handling failed".formatted(id));
            }
        };
    }

    @Bean
    public Consumer<Message<ContractorUpdateTripMessage.Dispatcher>> dispatcherInputSsl(DispatcherService dispatcherService,
                                                                                        DispatcherMapper dispatcherMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (!message.active()) {
                dispatcherService.deleteById(message.id());
            } else {
                var dispatcher = dispatcherService.get(message.id()).orElseGet(Dispatcher::new);
                dispatcherMapper.update(dispatcher, message);
                dispatcherService.save(dispatcher);
            }
        };
    }

    @Bean
    public Consumer<Message<DriverMessage>> driverInputSsl(DriverService driverService, DriverMapper driverMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (!message.active()) {
                driverService.deleteById(message.id());
            } else {
                var driver = driverService.get(message.id()).orElseGet(Driver::new);
                driverMapper.update(driver, message);
                driverService.save(driver);
            }
        };
    }

    @Bean
    public Consumer<Message<ContractorMessage>> contractorInputSsl(ContractorService contractorService,
                                                                   ContractorMapper contractorMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (message.deleted()) {
                contractorService.deleteById(message.id());
            } else {
                var contractor = contractorService.get(message.id()).orElseGet(Contractor::new);
                contractorMapper.update(contractor, message);
                contractorService.save(contractor);
            }
        };
    }

    @Bean
    public Consumer<Message<ContractorUpdateTripMessage.Vehicle>> vehicleInputSsl(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (message.deleted()) {
                vehicleService.deleteById(message.id());
            } else {
                var vehicle = vehicleService.get(message.id()).orElseGet(Vehicle::new);
                vehicleMapper.update(vehicle, message);
                vehicleService.save(vehicle);
            }
        };
    }

    @Bean
    public Consumer<Message<OrganizationMessage>> organizations(OrganizationRepository organizationService, NotificationSettingsService notificationSettingsService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var organizationId = Optional.ofNullable(rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class)).orElse(message.getId());
            if (message.getDeleted()) {
                organizationService.findById(organizationId).ifPresent(organizationService::delete);
            } else {
                Optional<Organization> organizationOpt = organizationService.findById(organizationId);
                if (organizationOpt.isEmpty()) {
                    var organization = organizationService.save(Organization.builder().id(organizationId).build());
                    try {
                        notificationSettingsService.addSettingsForNewOrganization(organization.getId());
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        };
    }

    @Bean
    public Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInputSsl(DeadlineSettingsService settingsService, DeadlineSettingsMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();

            if (message.deleted()) {
                Optional<DeadlineSettings> optional = settingsService.getOptional(message.getId());
                if (optional.isPresent()) {
                    settingsService.hardDelete(optional.get());
                    log.info("Настройки КС ID '{}' были удалены", message.getId());
                } else {
                    log.error("При попытке удаления Настроек КС ID '{}', они не были найдены в БД", message.getId());
                }
            } else {
                settingsService.save(mapper.fromMessage(message));
                log.info("Настройки КС ID '{}' были записаны / отредактированы", message.getId());
            }
        };
    }

    @Bean
    public Consumer<Message<DepartmentMessage>> departments(DepartmentService departmentService, DepartmentMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = message.getId();

            if (message.getDeleted()) {
                departmentService.delete(message.getId());
            } else {
                var department = departmentService.get(id).orElseGet(Department::new);
                mapper.update(department, message);
                departmentService.save(department);
            }
        };
    }

    @Bean(name = "employee")
    public Consumer<Message<EmployeeMessage>> employees(EmployeeService employeeService, EmployeeMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();

            if (message.getDeleted()) {
                employeeService.delete(message.getId());
            } else {
                var employee = employeeService.get(message.getId()).orElseGet(Employee::new);
                mapper.update(employee, message);
                employeeService.save(employee);
            }
        };
    }

    @Bean
    public Consumer<Message<DelegateMessage>> delegateInputSsl(DelegateHandler handler) {
        return rawMessage -> handler.handle(rawMessage.getPayload());
    }

    @Bean
    public Consumer<Message<ApproveSharedRideMessage>> sharedRideApproveInputSsl(ApproveSharedRideHandler handler) {
        return rawMessage -> handler.handle(rawMessage.getPayload());
    }

    @Bean
    public Consumer<Message<TaxiTariffMessage>> taxiTariffInputSsl(TariffService<TaxiTariff> tariffService, TariffMapper tariffMapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var key = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);

            if (message.deleted()) {
                tariffService.deactivate(key);
            } else {
                var taxiTariff = tariffService.findById(key).orElseGet(TaxiTariff::new);
                tariffMapper.update(taxiTariff, message);
                tariffService.save(taxiTariff);
            }
        };
    }

    @Bean
    public Consumer<Message<TripPurposeMessage>> tripPurposeInputSsl(TripPurposeRepository tripPurposeRepository, TripPurposeMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var purposeOptional = tripPurposeRepository.findById(message.getId());
            if (message.isDeleted()) {
                purposeOptional.ifPresent(tripPurposeRepository::delete);
                return;
            }
            var purpose = purposeOptional.orElseGet(TripPurpose::new);
            mapper.update(purpose, message);

            tripPurposeRepository.save(purpose);
        };
    }

    @Bean
    public Consumer<Message<RequestMessage>> tripRequestInputSsl(TripRequestHandler listener) {
        return rawMessage -> listener.handle(rawMessage.getPayload());
    }

    @Bean
    public Consumer<Message<TaxiTripMessage>> taxiTripInputSsl(TaxiTripService taxiTripService, TaxiTripMapper mapper, Processor<TaxiTrip> tripProcessor) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            if (!message.deleted() && (message.resolution() != null || message.driver() != null)) {
                var trip = taxiTripService.get(Optional.ofNullable(id).orElseGet(message::getId)).orElseGet(TaxiTrip.builder().id(id)::build);
                mapper.update(trip, message);
                try {
                    tripProcessor.process(taxiTripService.save(trip));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to process trip", e);
                }
            }
        };
    }

    @Bean
    public Consumer<Message<ApproveTripRequestMessage>> tripApproveInputSsl(TripApproveHandler handler) {
        return rawMessage -> handler.handle(rawMessage.getPayload());
    }

    @Bean
    public Consumer<Message<ApproveLimitRequestMessage>> approveLimitsRequestInputSsl(Processor<ApproveLimitRequest> processor, ApproveLimitRequestRepository repository, ApproveLimitMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = message.getId();
            if (message.deleted()) {
                repository.findById(id).ifPresent(repository::delete);
            } else {
                var approveLimit = repository.findById(id).orElseGet(ApproveLimitRequest::new);
                mapper.update(approveLimit, message);
                if (message.limitType().equals(LimitType.Values.DEPARTMENT)) {
                    approveLimit.setDepartmentId(message.departmentId());
                    approveLimit.setEmployeeId(message.departmentHeadId());
                    approveLimit.setOrganizationId(message.organizationDepartmentId());
                } else {
                    approveLimit.setEmployeeId(message.employeeId());
                    approveLimit.setOrganizationId(message.organizationEmployeeId());
                }
                repository.save(approveLimit);
                try {
                    processor.process(approveLimit);
                } catch (JsonProcessingException e) {
                    log.error("Processing failed", e);
                }
            }
        };
    }

    @Bean
    public Consumer<Message<RoleMessage>> rolesInputSsl(RoleRepository repository, RoleMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();

            var role = repository.findById(message.code()).orElseGet(Role::new);

            mapper.update(role, message);

            repository.save(role);
        };
    }

    @Bean
    public Consumer<Message<NotificationMessage>> notificationInput(NotificationHandler handler) {
        return rawMessage -> handler.accept(rawMessage.getPayload());
    }

    @Bean
    public Consumer<Message<NotificationMessage>> notificationInputSsl(NotificationHandler handler) {
        return rawMessage -> handler.accept(rawMessage.getPayload());
    }
}
