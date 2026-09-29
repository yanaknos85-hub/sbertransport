package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.OrganizationMedicalLicenseRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.EwbContractNotActiveException;
import ru.sber.transport.telemechanic.exception.EwbTariffNotFoundException;
import ru.sber.transport.telemechanic.exception.OrganizationMedicalLicenseNotActiveException;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.EwbContractService;
import ru.sber.transport.telemechanic.service.EwbTariffService;
import ru.sber.transport.telemechanic.service.OrganizationMedicalLicenseService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class OrganizationMedicalLicenseServiceImpl implements OrganizationMedicalLicenseService {
    
    public static final String LICENSE_NOT_FOUND = "Не найдена действующая лицензия на оказание медицинских услуг для организации, id организации:%s";
    private final OrganizationMedicalLicenseRepository repository;
    private final EmployeeService employeeService;
    private final EwbContractService ewbContractService;
    private final EwbTariffService ewbTariffService;
    
    @Override
    public OrganizationMedicalLicense save(OrganizationMedicalLicense entity) {
        return repository.save(entity);
    }
    
    @Override
    public boolean existsAndActive(UUID id) {
        return repository.existsAndActive(id);
    }
    
    
    @Override
    public void checkMedicalLicense(UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var organizationId = employee.getOrganization().getId();
        var ewbContracts = ewbContractService.getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId);
        if (ewbContracts.stream()
                        .noneMatch(EwbContract::isActive)) {
            throw new EwbContractNotActiveException(organizationId);
        }
        var ewbTariffs = ewbTariffService.getAllByActiveAndByContractIds(ewbContracts.stream()
                                                                                     .map(EwbContract::getContractId)
                                                                                     .toList());
        if (ewbTariffs.isEmpty()) {
            throw new EwbTariffNotFoundException(organizationId);
        }
        if (ewbTariffs.stream()
                      .noneMatch(ewbTariff -> repository.existsAndActive(
                              ewbTariff.getContract().getOrganizationMedicalLicenseId()))) {
            throw new OrganizationMedicalLicenseNotActiveException(LICENSE_NOT_FOUND.formatted(organizationId));
        }
    }
    
    @Override
    public OrganizationMedicalLicense getMedicalLicense(UUID departmentId) {
        var mapContractOptional = Optional.ofNullable(ewbTariffService.getActiveContractByDepartmentId(departmentId, true));
        if (mapContractOptional.isEmpty()) {
            return null;
        }
        var organizationMedicalLicenseOptional = mapContractOptional.get().entrySet().stream()
                .filter(entry -> InspectionType.getMedicineTypes().contains(entry.getKey()))
                .map(entry -> getById(entry.getValue().getOrganizationMedicalLicenseId()))
                .findFirst();
        return organizationMedicalLicenseOptional.orElse(null);
    }
    
    /**
     * Получение лицензии по идентификатору
     *
     * @param id идентификатор записи об медицинской лицензии организации
     *
     * @return {@link OrganizationMedicalLicense}
     */
    private OrganizationMedicalLicense getById(UUID id) {
        return repository.findByIdAndActiveIsTrue(id)
                         .orElseThrow(() -> new OrganizationMedicalLicenseNotActiveException(LICENSE_NOT_FOUND.formatted(id)));
    }
}
