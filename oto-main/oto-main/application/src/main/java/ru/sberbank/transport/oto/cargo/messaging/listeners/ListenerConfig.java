package ru.sberbank.transport.oto.cargo.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.kafka.BinderHeaderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractMessage;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.tariff.messaging.TariffMessage;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.messaging.messages.trip.DeadlineStateUpdateMessage;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMessage;
import ru.sberbank.ditsib.transport.request.messaging.CargoRequestMultiMessage;
import ru.sberbank.ditsib.transport.request.messaging.EvaluationMessage;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;
import ru.sberbank.ditsib.transport.route.messaging.RouteMessage;
import ru.sberbank.transport.oto.cargo.database.dao.OrganizationGroupRepository;
import ru.sberbank.transport.oto.cargo.database.dao.RequestStatusOverdueRepository;
import ru.sberbank.transport.oto.cargo.database.model.*;
import ru.sberbank.transport.oto.cargo.database.model.tariff.Contract;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.transport.oto.cargo.dto.mapper.PositionMapper;
import ru.sberbank.transport.oto.cargo.mappers.DepartmentMapper;
import ru.sberbank.transport.oto.cargo.mappers.EvaluationDTOMapper;
import ru.sberbank.transport.oto.cargo.mappers.OrganizationMapper;
import ru.sberbank.transport.oto.cargo.mappers.RequestStatusOverdueMapper;
import ru.sberbank.transport.oto.cargo.messaging.messages.RequestStatusOverdueMessage;
import ru.sberbank.transport.oto.cargo.messaging.messages.TripRatingMessage;
import ru.sberbank.transport.oto.cargo.messaging.processors.RouteProcessor;
import ru.sberbank.transport.oto.cargo.service.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<ContractorMessage>> contractorInput(ContractorService contractorService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new ContractorMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.name(), payload.msrn(), payload.tin(), payload.rating(),
                        payload.contactPersonInfo(), payload.contactPersonPhone(),
                        payload.regionIds(), payload.contractorName(),
                        payload.contractorRusName(), payload.integrationEmail(),
                        payload.deleted(), payload.integrationType(),
                        payload.url(), payload.login(), payload.password(), payload.mainDispatcher(), payload.digitId(),
                        payload.autoassign());
            }
            var contractor = contractorService.findById(payload.id()).orElse(null);
            if (!payload.deleted()) {
                if (contractor == null) {
                    contractorService.save(Contractor.builder().id(payload.id()).name(payload.name()).build());
                } else {
                    contractor = contractor.toBuilder().name(payload.name()).build();
                    contractorService.save(contractor);
                }
            } else {
                if (contractor != null) {
                    contractorService.delete(contractor);
                }
            }
        };
    }

    @Bean
    Consumer<Message<ContractMessage>> contractInput(ContractorService contractorService, ContractService contractService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new ContractMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.serviceType(), payload.regionIds(), payload.transportType(),
                        payload.contractorId(), payload.sum(), payload.startDate(),
                        payload.endDate(), payload.userId(), payload.creationTime(),
                        payload.organizations(), payload.active(), payload.deleted());
            }
            if (payload.deleted()) {
                contractService.deactivate(payload.id());
            } else {
                var contract = contractService.findById(payload.id()).orElse(null);
                var contractor =
                        contractorService.findById(payload.contractorId())
                                .orElse(contractorService.save(
                                        Contractor.builder().id(payload.contractorId()).build()));
                if (contract == null) {
                    contractService.save(
                            Contract.builder().id(payload.getId()).active(payload.active()).contractor(contractor).build());
                } else {
                    contract = contract.toBuilder().active(payload.active()).build();
                    contractService.save(contract);
                }
            }
        };
    }

    @Bean
    Consumer<Message<DeadlineStateUpdateMessage>> deadlineStateUpdateInput(RequestService requestService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new DeadlineStateUpdateMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.getDeadlineState(), payload.getDeadlineTill());
            }
            var finalPayload = payload;
            var requestId = finalPayload.getRequestId();
            var deadlineState = finalPayload.getDeadlineState();
            var deadlineTill = finalPayload.getDeadlineTill();

            log.info("Message about deadline state update received. Request ID: {}, deadline State: {}, deadline till: {}",
                    requestId, deadlineState, deadlineTill);
            requestService.findById(requestId)
                    .ifPresentOrElse(request -> {
                        DeadlineState.getByName(deadlineState).ifPresent(request::setDeadlineState);
                        request.setDeadline(deadlineTill);
                        requestService.save(request);
                    }, () -> {
                        throw new EntityNotFoundException(Request.class, finalPayload.getId());
                    });
        };
    }

    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentMapper mapper, DepartmentService service) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                Department.DepartmentBuilder department = Department.builder();
                department.id(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class));
                department.parentId(payload.getParentId());
                department.organizationId(payload.getOrganizationId());
                department.humanReadableId(payload.getHumanReadableId());
                department.departmentName(payload.getDepartmentName());
                if (!payload.isDeleted()) {
                    service.save(department.build());
                }
            } else {
                if (!payload.isDeleted()) {
                    service.save(mapper.fromMessage(payload));
                }
            }
        };
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(
            OrganizationService organizationService, DepartmentService departmentService,
            PositionService positionService, EmployeeService employeeService
    ) {
        return message -> {
            var payload = message.getPayload();
            var id = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var employeeId = Optional.ofNullable(id).orElse(payload.getId());
            if (!payload.isDeleted()) {
                var employee = new Employee();
                if (payload.getDepartmentId() != null) {
                    var department = departmentService.findOrCreateById(payload.getDepartmentId());
                    employee.setDepartment(department);
                }
                if (payload.getPositionId() != null) {
                    var position = positionService.findOrCreatePositionById(payload.getPositionId());
                    employee.setPosition(position);
                }
                employee.setId(employeeId);
                employee.setUserId(payload.getUserId());
                employee.setPersonnelNumber(payload.getPersonnelNumber());
                employee.setFirstName(payload.getFirstName());
                employee.setLastName(payload.getLastName());
                employee.setPatronymic(payload.getPatronymic());
                employee.setHumanReadableId(payload.getHumanReadableId());
                employee.setMobilePhone(payload.getMobilePhone());
                if (payload.getItinerantType() != null) {
                    employee.setItinerantType(ItinerantType.valueOf(payload.getItinerantType()));
                }
                employee.setCostCenter(payload.getCostCenter());
                employee.setMarriageCertificateNumber(payload.getMarriageCertificateNumber());
                employeeService.save(employee);
            }
        };
    }

    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInput(
            OrganizationService organizationService, OrganizationMapper organizationMapper,
            OrganizationGroupRepository groupRepository
    ) {
        return message -> {
            var id = message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class);
            var payload = message.getPayload();
            var organizationId = Optional.ofNullable(id).orElse(payload.getId());
            if (!payload.isDeleted()) {

                if (payload.getOrganizationGroup() != null && payload.getOrganizationGroup().id() != null) {
                    groupRepository.save(OrganizationGroup.builder()
                            .id(payload.getOrganizationGroup().id())
                            .internal(payload.getOrganizationGroup().internal())
                            .name(payload.getOrganizationGroup().name())
                            .build());
                }

                var newOrganiztion = organizationMapper.fromMessage(payload);
                if (newOrganiztion.getId() == null) {
                    newOrganiztion.setId(organizationId);
                }
                var existingOrg = organizationService.findById(organizationId);
                if (existingOrg.isPresent()) {
                    organizationService.save(organizationMapper.update(newOrganiztion, existingOrg.get()));
                } else {
                    organizationService.save(newOrganiztion);
                }
            }
        };
    }

    @Bean
    Consumer<Message<PositionMessage>> positionsInput(PositionMapper mapper, PositionService positionService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new PositionMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.getOrganizationId(), payload.getPositionName(),
                        payload.getAvailableClasses(), payload.isSelfApproved(), payload.isDeleted());
            }
            if (!payload.isDeleted()) {
                positionService.save(mapper.positionMessageToPosition(payload));
            }
        };
    }

    @Bean
    Consumer<Message<TripRatingMessage>> requestRatingInput(RequestService requestService) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new TripRatingMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.advantages(), payload.drawbacks(), payload.rating(), payload.ratingComment());
            }
            var finalPayload = payload;
            requestService.findById(finalPayload.requestId()).ifPresent(request -> {

                request.setRequestRating(RequestRating.builder().
                        advantages(finalPayload.advantages()).
                        drawbacks(finalPayload.drawbacks()).
                        rating(finalPayload.rating()).
                        ratingComment(finalPayload.ratingComment()).build());

                requestService.save(request);
            });
        };
    }

    @Bean
    Consumer<Message<RequestStatusOverdueMessage>> requestStatusOverdueInput(
            RequestStatusOverdueMapper mapper,
            RequestStatusOverdueRepository repository
    ) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new RequestStatusOverdueMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.requestId(), payload.tripRequestStatus(),
                        payload.carsharingJoinRequestStatus(), payload.deadlineChronoUni(),
                        payload.deadlineValue(), payload.overdueTime());
            }
            log.debug("Message received {}", payload.id());
            repository.save(mapper.toModel(payload));
        };
    }

    @Bean
    Consumer<Message<TariffMessage>> tariffInput(List<TariffService<?>> tariffServiceList) {
        return message -> {
            var payload = message.getPayload();
            if (payload.getId() == null) {
                payload = new TariffMessage(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                        payload.humanReadableId(), payload.organizationId(), payload.regionId(),
                        payload.priceDetails(), payload.transportTypeId(), payload.contractId(),
                        payload.workGroup(), payload.deleted(), payload.cloneId());
            }
            var finalPayload = payload;
            TransportTypeEnum.fromId(finalPayload.transportTypeId())
                    .flatMap(transportTypeEnum -> tariffServiceList.stream()
                            .filter(service -> service.getTransportType().equals(transportTypeEnum))
                            .findFirst())
                    .ifPresent(service -> service.updateOrCreate(finalPayload.id(), finalPayload));
        };
    }

    @Bean
    Consumer<Message<CargoRequestMessage>> tripCargoRequestInput(TripRequestListener tripRequestListener) {
        return message -> {
            var payload = message.getPayload();
            tripRequestListener.handleCargo(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                    payload);
        };
    }

    @Bean
    Consumer<Message<CargoRequestMultiMessage>> tripCargoRequestMultiInput(TripRequestMultiListener tripRequestListener) {
        return message -> {
            var payload = message.getPayload();
            tripRequestListener.handleCargo(message.getHeaders().get(KafkaHeaders.RECEIVED_KEY, UUID.class),
                    payload);
        };
    }

    /**
     * Слушатель топика оценки заявки на грузоперевозку
     */
    @Bean
    Consumer<Message<EvaluationMessage>> evaluationInput(
            EvaluationService service, RequestService requestService,
            EvaluationDTOMapper mapper
    ) {
        return message -> {
            var payload = message.getPayload();
            log.info("Get evaluation for Request with id {}", payload.getRequestId());
            var evaluation = mapper.fromMessage(payload);
            if (evaluation.getId() == null) {
                evaluation.setId(UUID.randomUUID());
            }
            var request = requestService.findById(payload.getRequestId())
                    .orElseThrow(() -> new RuntimeException(
                            "Request with id %s for evaluation not found".formatted(payload.getRequestId())));
            evaluation.setRequest(request);
            service.save(evaluation);
        };
    }

    @Bean
    Consumer<Message<TemplateForCargoMessage>> templateForCargoInput(TemplateForCargoService service) {
        return message -> {
            var payload = message.getPayload();
            Optional<TemplateForCargo> entity = service.findById(message.getPayload().getId());
            entity.ifPresent(e -> e.setStatus(TripRequestStatus.valueOf(payload.getStatus())));
            service.save(payload);
        };
    }

    @Bean
    Consumer<Message<RouteMessage>> routeInput(RouteProcessor service) {
        return message -> service.handle(message.getPayload());
    }

    private String getType(Map<?, ?> headers) {
        var headerValue = headers.get("transportType");
        if (headerValue instanceof DefaultKafkaHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"", "");
        } else if (headerValue instanceof BinderHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"", "");
        } else {
            return String.valueOf(headerValue);
        }
    }
}
