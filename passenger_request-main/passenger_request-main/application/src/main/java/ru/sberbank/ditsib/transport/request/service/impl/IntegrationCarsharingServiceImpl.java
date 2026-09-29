package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.integrations.carsharing.grpc.dto.EmployeeInfoOuterClass;
import ru.sber.transport.integrations.carsharing.grpc.service.CarsharingIntegrationServiceGrpc;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingExternalDataRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingInfoRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingExternalData;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingInfo;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.exceptions.CarsharingException;
import ru.sberbank.ditsib.transport.request.service.IntegrationsCarsharingService;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class IntegrationCarsharingServiceImpl implements IntegrationsCarsharingService {
    
    private final CarsharingExternalDataRepository repository;
    private final CarsharingInfoRepository carsharingInfoRepository;
    
    private static final String ERROR_MSG = "Возникли проблемы при присоединение к корпоративному тарифу";
    
    @GrpcClient("integrations-carsharing")
    private CarsharingIntegrationServiceGrpc.CarsharingIntegrationServiceBlockingStub stub;
    
    @Override
    public void joinToTariff(@NonNull Employee employee, @NonNull UUID organizationId) {
        var carsharingInfoOptional = carsharingInfoRepository.getByEmployee(employee);
        if (isPreviouslyUsed(carsharingInfoOptional)) {
            return;
        }
        log.info("Присоединение к корпоративному тарифу: {}", employee.getHumanReadableId());
        var info = mapToEmployeeInfo(employee, organizationId);
        try {
            stub.joinToTariff(info);
        } catch (Exception e) {
            log.error(ERROR_MSG, e);
            throw new CarsharingException(ERROR_MSG);
        }
        var carsharingInfo = carsharingInfoOptional.orElseGet(() -> CarsharingInfo.builder()
                                                                                  .consent(false)
                                                                                  .previouslyUsed(false)
                                                                                  .employee(employee)
                                                                                  .build());
        carsharingInfo.setPreviouslyUsed(true);
        carsharingInfoRepository.saveAndFlush(carsharingInfo);
    }
    
    
    private boolean isPreviouslyUsed(Optional<CarsharingInfo> carsharingInfo) {
        return carsharingInfo.isPresent() && carsharingInfo.get().isPreviouslyUsed();
    }
    
    private EmployeeInfoOuterClass.EmployeeInfo mapToEmployeeInfo(Employee employee, UUID organizationId) {
        var extData = repository.findFirstByOrganizationId(organizationId)
                                .orElseThrow(() -> new EntityNotFoundException(CarsharingExternalData.class, organizationId));
        return EmployeeInfoOuterClass.EmployeeInfo.newBuilder()
                                                  .setCorporateId(extData.getExtOrganizationId())
                                                  .setGroupId(extData.getExtGroupId())
                                                  .setFullName(employee.getFIO())
                                                  .setPosition("Сотрудник")
                                                  .setPhoneNumber(Optional.ofNullable(employee.getMobilePhone())
                                                                          .orElseThrow(
                                                                                  () -> new CarsharingException("Отсутствует номер " +
                                                                                                                "телефона")))
                                                  .build();
    }
}
