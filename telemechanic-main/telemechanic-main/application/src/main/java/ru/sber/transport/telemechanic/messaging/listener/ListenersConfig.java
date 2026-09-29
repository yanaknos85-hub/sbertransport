package ru.sber.transport.telemechanic.messaging.listener;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;
import ru.sber.transport.telemechanic.FirstTitleCreateRequestMessage;
import ru.sber.transport.telemechanic.messaging.listener.message.*;
import ru.sber.transport.telemechanic.messaging.sender.message.TransportMessage;
import ru.sber.transport.telemechanic.provider.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

/**
 * Конфигурация получателей.
 */
@Configuration
public class ListenersConfig {
    
    private static final String CONTRACTOR_SERVICE_TYPE = "INTERNAL_AUTO_PARK";
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputSsl(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputDlq(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMessage>> organizationsInputDlqSsl(OrganizationProvider provider) {
        return getOrganizationConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<OrganizationMessage>> getOrganizationConsumer(OrganizationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInput(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputSsl(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputDlq(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DepartmentMessage>> departmentsInputDlqSsl(DepartmentProvider provider) {
        return getDepartmentConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<DepartmentMessage>> getDepartmentConsumer(DepartmentProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInput(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputSsl(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputDlq(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<PositionMessage>> positionsInputDlqSsl(PositionProvider provider) {
        return getPositionConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<PositionMessage>> getPositionConsumer(PositionProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInput(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputSsl(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputDlq(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EmployeeMessage>> employeesInputDlqSsl(EmployeeProvider provider) {
        return getEmployeeConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<EmployeeMessage>> getEmployeeConsumer(EmployeeProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.isDeleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<TransportMessage>> transportInput(TransportProvider provider) {
        return getTransportConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<TransportMessage>> transportInputSsl(TransportProvider provider) {
        return getTransportConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<TransportMessage>> getTransportConsumer(TransportProvider provider) {
        return message -> {
            var payload = message.getPayload();
            var deleted = payload.deleted();
            if (deleted) {
                provider.delete(payload);
            } else {
                provider.save(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<FleetOwnerOrganizationMessage>> fleetOwnerOrganizationsInput(FleetOwnerOrganizationProvider provider) {
        return getFleetOwnerOrganizationConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<FleetOwnerOrganizationMessage>> fleetOwnerOrganizationsInputSsl(FleetOwnerOrganizationProvider provider) {
        return getFleetOwnerOrganizationConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<FleetOwnerOrganizationMessage>> getFleetOwnerOrganizationConsumer(FleetOwnerOrganizationProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<OrganizationMedicalLicenseMessage>> organizationMedicalLicensesInput(OrganizationMedicalLicenseProvider provider) {
        return getOrganizationMedicalLicenseConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<OrganizationMedicalLicenseMessage>> organizationMedicalLicensesInputSsl(OrganizationMedicalLicenseProvider provider) {
        return getOrganizationMedicalLicenseConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<OrganizationMedicalLicenseMessage>> getOrganizationMedicalLicenseConsumer(OrganizationMedicalLicenseProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<EwbContractMessage>> ewbContractsInput(EwbContractProvider provider) {
        return getEwbContractConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EwbContractMessage>> ewbContractsInputSsl(EwbContractProvider provider) {
        return getEwbContractConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EwbContractMessage>> ewbContractsInputDlq(EwbContractProvider provider) {
        return getEwbContractConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EwbContractMessage>> ewbContractsInputDlqSsl(EwbContractProvider provider) {
        return getEwbContractConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<EwbContractMessage>> getEwbContractConsumer(EwbContractProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<EwbTariffMessage>> ewbTariffsInput(EwbTariffProvider provider) {
        return getEwbTariffConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<EwbTariffMessage>> ewbTariffsInputSsl(EwbTariffProvider provider) {
        return getEwbTariffConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<EwbTariffMessage>> getEwbTariffConsumer(EwbTariffProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<DispatcherMessage>> dispatcherInput(DispatcherProvider provider) {
        return getDispatcherMessageConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DispatcherMessage>> dispatcherInputSsl(DispatcherProvider provider) {
        return getDispatcherMessageConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DispatcherMessage>> getDispatcherMessageConsumer(DispatcherProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<DriverMessage>> driverInput(DriverProvider provider) {
        return getDriverMessageConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DriverMessage>> driverInputSsl(DriverProvider provider) {
        return getDriverMessageConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<DriverMessage>> getDriverMessageConsumer(DriverProvider provider) {
        return message -> provider.save(message.getPayload());
    }
    
    @Bean
    public Consumer<Message<ContractorMessage>> contractorInput(OrganizationProvider provider) {
        return getContractorConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<ContractorMessage>> contractorInputSsl(OrganizationProvider provider) {
        return getContractorConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<ContractorMessage>> getContractorConsumer(OrganizationProvider provider) {
        return message -> {
            var payload = message.getPayload();
            if (payload.serviceType().equals(CONTRACTOR_SERVICE_TYPE)) {
                if (payload.deleted()) {
                    provider.deleteContractor(payload);
                } else {
                    provider.addContractor(payload);
                }
            }
        };
    }
    
    @Bean
    public Consumer<Message<AutoparkMessage>> autoparkInput(DepartmentProvider provider) {
        return getAutoparkConsumer(provider);
    }
    
    @Bean
    public Consumer<Message<AutoparkMessage>> autoparkInputSsl(DepartmentProvider provider) {
        return getAutoparkConsumer(provider);
    }
    
    @NotNull
    private Consumer<Message<AutoparkMessage>> getAutoparkConsumer(DepartmentProvider provider) {
        return message -> {
            var payload = message.getPayload();
            if (payload.deleted()) {
                provider.deleteAutopark(payload);
            } else {
                provider.addAutopark(payload);
            }
        };
    }
    
    @Bean
    public Consumer<Message<FirstTitleCreateRequestMessage>> firstTitleCreateInput(EwbProvider ewbProvider) {
        return getFirstTitleCreateConsumer(ewbProvider);
    }
    
    @Bean
    public Consumer<Message<FirstTitleCreateRequestMessage>> firstTitleCreateInputSsl(EwbProvider ewbProvider) {
        return getFirstTitleCreateConsumer(ewbProvider);
    }
    
    @NotNull
    private Consumer<Message<FirstTitleCreateRequestMessage>> getFirstTitleCreateConsumer(EwbProvider ewbProvider) {
        return message -> {
            var payload = message.getPayload();
            ewbProvider.save(payload);
        };
    }
}