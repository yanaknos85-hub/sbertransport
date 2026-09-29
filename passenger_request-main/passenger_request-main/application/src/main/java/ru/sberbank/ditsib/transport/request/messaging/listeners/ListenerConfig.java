package ru.sberbank.ditsib.transport.request.messaging.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractorUpdateTripMessage;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sber.transport.integrations.messaging.OrdersLocationMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sber.transport.tariff.messaging.*;
import ru.sber.transport.tariff.messaging.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.integrations.carsharing.messages.CarsharingDataMessage;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.FraudData;
import ru.sberbank.ditsib.transport.request.database.model.FraudType;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.mappers.*;
import ru.sberbank.ditsib.transport.request.messaging.listeners.impl.*;
import ru.sberbank.ditsib.transport.request.messaging.listeners.impl.tariff.*;
import ru.sberbank.ditsib.transport.request.messaging.message.*;
import ru.sberbank.ditsib.transport.request.messaging.message.ContractMessage;
import ru.sberbank.ditsib.transport.request.provider.CarLocationProvider;
import ru.sberbank.ditsib.transport.request.provider.RequestFactDataProvider;
import ru.sberbank.ditsib.transport.request.service.*;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.OtherTrTypesApprovalsSettingsService;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.PublicApprovalsSettingsService;
import ru.sberbank.ditsib.transport.request.service.approvals.settings.TaxiApprovalsSettingsService;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.service.corp.OrganizationService;
import ru.sberbank.ditsib.transport.request.service.corp.PositionService;
import ru.sberbank.ditsib.transport.request.service.impl.RequestForPersonalServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class ListenerConfig {
    
    @Bean
    public Executor bootstrapExecutor() {
        log.info("Creating bootstrap executor");
        return Executors.newCachedThreadPool();
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<Map<String, Object>>> approvalsSettingsInput(TaxiApprovalsSettingsService tripService,
                                                                         PublicApprovalsSettingsService settingsService,
                                                                         OtherTrTypesApprovalsSettingsService othersSettings,
                                                                         ApprovalsSettingsMapper mapper,
                                                                         ObjectMapper objectMapper
                                                                        ) {
        log.info("Creating approvals settings listener");
        return new ApprovalsSettingsListenerImpl(tripService, settingsService, othersSettings, mapper, objectMapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ApproveFinalTripMessage>> approveFinalTripInput(RequestApprovementService service) {
        log.info("Creating approve final trip listener");
        return new ApproveFinalTripListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ApproveTripRequestMessage>> approveRequestInput(RequestApprovementService service) {
        log.info("Creating approve request listener");
        return new ApproveRequestListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ApproveSharedRideMessage>> approveSharedRideInput(RequestForPersonalServiceImpl service) {
        log.info("Creating approve shared ride listener");
        return new ApproveSharedRideListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ApproveUpdateTripRequestMessage>> approveUpdateTripInput(RequestService service) {
        log.info("Creating approve update trip listener");
        return new ApproveUpdateTripListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ContractMessage>> contractInput(CarsharingContractRepository contractRepository,
                                                            CorporateCarsharingRepository carsharingRepository,
                                                            CarsharingContractMapper mapper
                                                           ) {
        log.info("Creating contract listener");
        return new CarsharingContractListenerImpl(contractRepository, carsharingRepository, mapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<CarSharingTariffMessage>> carSharingTariffInput(CarsharingTariffService service) {
        log.info("Creating car sharing tariff listener");
        return new CarSharingTariffListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<CarsharingDataMessage>> carsharingTripInput(CarsharingTripRepository carsharingRepository,
                                                                        RequestForCarsharingRepository requestCarsharingRepository,
                                                                        EmployeeRepository employeeRepository,
                                                                        CarsharingTripMapper mapper,
                                                                        RequestService service
                                                                       ) {
        log.info("Creating carsharing trip listener");
        return new CarsharingTripListenerImpl(carsharingRepository, requestCarsharingRepository, employeeRepository, mapper, service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ru.sber.transport.contractor.messages.ContractorMessage>> contractorInput(ContractorService service,
                                                                                                      ContractorMapper mapper
                                                                                                     ) {
        log.info("Creating contractor listener");
        return new ContractorListenerImpl(service, mapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<ContractorUpdateTripMessage>> contractorTripUpdateInput(RequestForTaxiRepository taxiRepository,
                                                                                    RequestForGroupTransferRepository groupRepository,
                                                                                    DriverDTOMapper mapper,
                                                                                    DriverRepository driverRepository,
                                                                                    RequestService service
                                                                                   ) {
        log.info("Creating contractor trip update listener");
        return new ContractorTripUpdateListenerImpl(taxiRepository, groupRepository, mapper, driverRepository, service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInput(DeadlineSettingsService service, DeadlineSettingsMapper mapper) {
        log.info("Creating deadline settings listener");
        return new DeadlineSettingsListenerImpl(service, mapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<DelegateMessage>> delegateInput(DelegateService service) {
        log.info("Creating delegate listener");
        return new DelegateListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentService depService, OrganizationService orgService) {
        log.info("Creating department listener");
        return new DepartmentListenerImpl(depService, orgService);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<DepartmentTripRequestApproversMessage>> departmentTripRequestApproversInput(DepartmentService service,
                                                                                                        DepartmentTripRequestApproversMapper mapper
                                                                                                       ) {
        log.info("Creating department trip request approvers listener");
        return new DepartmentTripRequestApproversListenerImpl(service, mapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<LimitMessage>> limitInput(DepLimitMapper mapper, DepLimitRepository repository) {
        log.info("Creating limit listener");
        return new DepLimitListenerImpl(mapper, repository);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<EmployeeMessage>> employeesInput(EntityDTOMapper mapper, EmployeeService empService, DepartmentService depService) {
        log.info("Creating employee listener");
        return new EmployeeListenerImpl(mapper, empService, depService);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<GeoZoneMessage>> geoZoneInput(GeoZoneService service) {
        log.info("Creating geo zone listener");
        return new GeoZoneListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<GroupTransferTariffMessage>> groupTransferTariffInput(GroupTransferTariffRepository repository, TariffMapper mapper) {
        log.info("Creating group transfer tariff listener");
        return new GroupTransferTariffListenerImpl(repository, mapper);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<InContractorTaxiTripInProgressMessage>> inContractorTaxiTripInProgressInput(List<InProgressMessageProcessor> processors) {
        log.info("Creating in contractor taxi trip in progress listener");
        return new InContractorTaxiTripInProgressListenerImpl(processors);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationService service) {
        log.info("Creating organization listener");
        return new OrganizationListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<PersonalTariffMessage>> personalTariffInput(PersonalTariffService service) {
        log.info("Creating personal tariff listener");
        return new PersonalTariffListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<PositionMessage>> positionsInput(EntityDTOMapper mapper, PositionService service) {
        log.info("Creating position listener");
        return new PositionListenerImpl(mapper, service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<PublicTariffMessage>> publicTariffInput(PublicTariffService service) {
        log.info("Creating public tariff listener");
        return new PublicTariffListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<RequestMessage>> requestInput(List<TransportTypeService<? extends Request>> services,
                                                          RequestRepository repository
                                                         ) {
        log.info("Creating request listener");
        return new RequestListenerImpl(services, repository);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<TaxiTariffMessage>> taxiTariffInput(TaxiTariffService service) {
        log.info("Creating taxi tariff listener");
        return new TaxiTariffListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<TripPurposeMessage>> tripPurposeInput(TripPurposeService service) {
        log.info("Creating trip purpose listener");
        return new TripPurposeListenerImpl(service);
    }
    
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<UpdateTripRequestStatusMessage>> updateRequestStatusFromReportsInput(RequestService requestService,
                                                                                                 EmployeeService empService
                                                                                                ) {
        log.info("Creating update request status from reports listener");
        return new UpdateRequestStatusFromReportsListenerImpl(requestService, empService);
    }

    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Consumer<Message<UpdateTripRequestStatusMessage>> updateRequestStatusInput(RequestService requestService,
                                                                                      EmployeeService empService
                                                                                     ) {
        log.info("Creating update request status listener");
        return new UpdateRequestStatusListenerImpl(requestService, empService);
    }

    @Bean
    public Consumer<Message<OrdersLocationMessage>> orderLocationResponseInput(CarLocationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            log.debug("Car location response received: {}", payload);
            provider.sendLocationsToSubscribers(payload);
        };
    }

    @Bean
    public Consumer<Message<ResourceIdMessage>> carLocationResourceIdInput(CarLocationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            provider.deactivateTask(payload.id());
        };
    }

    @Bean
    public Consumer<Message<RequestFactDataMessage>> contractorRequestInput(RequestFactDataProvider provider) {
        return message -> {
            final var payload = message.getPayload();
            provider.enrichRequest(payload);
        };
    }

    @Bean
    public Consumer<Message<FraudMessage>> fraudInput(FraudService service, RequestRepository requestRepository) {
        return raw -> {
            final var message = raw.getPayload();
            saveData(requestRepository, message.id(), FraudType.RECEIPT, message.comment(), service);
        };
    }

    @Bean
    public Consumer<Message<FraudMessage>> fraudInputRadius(FraudService service, RequestRepository requestRepository) {
        return raw -> {
            final var message = raw.getPayload();
            saveData(requestRepository, message.id(), FraudType.RADIUS, message.comment(), service);
        };
    }

    private void saveData(RequestRepository requestRepository, UUID requestId, FraudType type, String message, FraudService service) {
        log.debug("[saveData] Saving fraud data for request {}", requestId);
        final var fraudData = new FraudData();
        fraudData.setRequest(requestRepository.getReferenceById(requestId));
        fraudData.setType(type);
        fraudData.setComment(message);
        service.saveAndSend(fraudData);
    }

}
