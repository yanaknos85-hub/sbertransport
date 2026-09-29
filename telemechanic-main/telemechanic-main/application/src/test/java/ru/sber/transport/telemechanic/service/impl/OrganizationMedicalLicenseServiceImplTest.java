package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.OrganizationMedicalLicenseRepository;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.database.model.OrganizationMedicalLicense;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.EwbContractNotActiveException;
import ru.sber.transport.telemechanic.exception.EwbTariffNotFoundException;
import ru.sber.transport.telemechanic.exception.OrganizationMedicalLicenseNotActiveException;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.EwbContractService;
import ru.sber.transport.telemechanic.service.EwbTariffService;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationMedicalLicenseServiceImplTest {
    
    @InjectMocks
    private OrganizationMedicalLicenseServiceImpl service;
    @Mock
    private OrganizationMedicalLicenseRepository repository;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private EwbContractService ewbContractService;
    @Mock
    private EwbTariffService ewbTariffService;
    
    @Test
    void save() {
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        doReturn(organizationMedicalLicense).when(repository).save(organizationMedicalLicense);
        assertThat(service.save(organizationMedicalLicense))
                .usingRecursiveComparison()
                .isEqualTo(organizationMedicalLicense);
    }
    
    @Test
    void existsAndActive() {
        var id = UUID.randomUUID();
        doReturn(true).when(repository).existsAndActive(id);
        assertThat(service.existsAndActive(id)).isTrue();
        assertThat(service.existsAndActive(UUID.randomUUID())).isFalse();
    }
    
//    @Test
//    void getById() {
//        var id = UUID.randomUUID();
//        var wrongId = UUID.randomUUID();
//        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
//        doReturn(Optional.of(organizationMedicalLicense)).when(repository).findByIdAndActiveIsTrue(id);
//        doReturn(Optional.empty()).when(repository).findByIdAndActiveIsTrue(wrongId);
//        assertThat(service.getById(id)).isEqualTo(organizationMedicalLicense);
//        assertThatThrownBy(() -> service.getById(wrongId))
//                .isInstanceOf(OrganizationMedicalLicenseNotActiveException.class)
//                .hasMessage("Не найден действующая лицензия на оказание медицинских услуг для организации, id:%s", wrongId);
//    }
    
    @Test
    void checkMedicalLicense() {
        var userId1 = UUID.randomUUID();
        var userId2 = UUID.randomUUID();
        var userId3 = UUID.randomUUID();
        var userId4 = UUID.randomUUID();
        var employee1 = Instancio.create(Employee.class);
        var employee2 = Instancio.create(Employee.class);
        var employee3 = Instancio.create(Employee.class);
        var employee4 = Instancio.create(Employee.class);
        var organizationId1 = employee1.getOrganization().getId();
        var organizationId2 = employee2.getOrganization().getId();
        var organizationId3 = employee3.getOrganization().getId();
        var organizationId4 = employee4.getOrganization().getId();
        var ewbContracts1 = Instancio.ofList(EwbContract.class)
                                     .size(3)
                                     .set(field(EwbContract::isActive), true)
                                     .create();
        var ewbContracts2 = Instancio.ofList(EwbContract.class)
                                     .size(3)
                                     .set(field(EwbContract::isActive), false)
                                     .create();
        var ewbContracts3 = Instancio.ofList(EwbContract.class)
                                     .size(3)
                                     .set(field(EwbContract::isActive), true)
                                     .create();
        var ewbContracts4 = Instancio.ofList(EwbContract.class)
                                     .size(3)
                                     .set(field(EwbContract::isActive), true)
                                     .create();
        var ewbTariffs1 = Instancio.ofList(EwbTariff.class)
                                   .size(3)
                                   .create();
        var ewbTariffs2 = Instancio.ofList(EwbTariff.class)
                                   .size(3)
                                   .create();
        doReturn(employee1).when(employeeService).getByUserId(userId1);
        doReturn(employee2).when(employeeService).getByUserId(userId2);
        doReturn(employee3).when(employeeService).getByUserId(userId3);
        doReturn(employee4).when(employeeService).getByUserId(userId4);
        doReturn(ewbContracts1).when(ewbContractService).getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId1);
        doReturn(ewbContracts2).when(ewbContractService).getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId2);
        doReturn(ewbContracts3).when(ewbContractService).getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId3);
        doReturn(ewbContracts4).when(ewbContractService).getAllByInspectionTypeAndOrganizationId(InspectionType.getMedicineTypes(), organizationId4);
        doReturn(ewbTariffs1).when(ewbTariffService).getAllByActiveAndByContractIds(ewbContracts1.stream()
                                                                                                 .map(EwbContract::getContractId)
                                                                                                 .toList());
        doReturn(Collections.emptyList()).when(ewbTariffService).getAllByActiveAndByContractIds(ewbContracts3.stream()
                                                                                                             .map(EwbContract::getContractId)
                                                                                                             .toList());
        doReturn(ewbTariffs2).when(ewbTariffService).getAllByActiveAndByContractIds(ewbContracts4.stream()
                                                                                                 .map(EwbContract::getContractId)
                                                                                                 .toList());
        doReturn(true)
                .when(repository).existsAndActive(ewbTariffs1.get(0).getContract().getOrganizationMedicalLicenseId());
        ewbTariffs2.forEach(ewbTariff -> doReturn(false)
                .when(repository).existsAndActive(ewbTariff.getContract().getOrganizationMedicalLicenseId()));
        service.checkMedicalLicense(userId1);
        assertThatThrownBy(() -> service.checkMedicalLicense(userId2))
                .isInstanceOf(EwbContractNotActiveException.class)
                .hasMessage("Не найден активный договор для организации, id:%s".formatted(organizationId2));
        assertThatThrownBy(() -> service.checkMedicalLicense(userId3))
                .isInstanceOf(EwbTariffNotFoundException.class)
                .hasMessage("Не найден активный тариф для организации, id:%s".formatted(organizationId3));
        assertThatThrownBy(() -> service.checkMedicalLicense(userId4))
                .isInstanceOf(OrganizationMedicalLicenseNotActiveException.class)
                .hasMessage(
                        "Не найдена действующая лицензия на оказание медицинских услуг для организации, id организации:%s".formatted(organizationId4));
        verify(employeeService, times(4)).getByUserId(any(UUID.class));
        verify(ewbContractService, times(4)).getAllByInspectionTypeAndOrganizationId(anySet(), any(UUID.class));
        verify(ewbTariffService, times(3)).getAllByActiveAndByContractIds(anyList());
        verify(repository, times(4)).existsAndActive(any(UUID.class));
    }
    
    @Test
    void getMedicalLicense() {
        var departmentId1 = UUID.randomUUID();
        var departmentId2 = UUID.randomUUID();
        var technicContract = Instancio.of(EwbContract.class)
                                       .set(field(EwbContract::getInspectionType), InspectionType.TECHNIC)
                                       .create();
        var medicContract = Instancio.of(EwbContract.class)
                                     .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                                     .create();
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        doReturn(Map.of(
                InspectionType.TECHNIC, technicContract,
                InspectionType.MEDIC, medicContract
                       )).when(ewbTariffService).getActiveContractByDepartmentId(departmentId1, true);
        doReturn(Collections.emptyMap()).when(ewbTariffService).getActiveContractByDepartmentId(departmentId2, true);
        doReturn(Optional.of(organizationMedicalLicense)).when(repository).findByIdAndActiveIsTrue(medicContract.getOrganizationMedicalLicenseId());
        assertThat(service.getMedicalLicense(departmentId1)).isEqualTo(organizationMedicalLicense);
        assertThat(service.getMedicalLicense(departmentId2)).isNull();
    }
    
    @Test
    void getMedicalLicenseForTelemedic() {
        var departmentId1 = UUID.randomUUID();
        var technicContract = Instancio.of(EwbContract.class)
                                       .set(field(EwbContract::getInspectionType), InspectionType.TECHNIC)
                                       .create();
        var telemedicContract = Instancio.of(EwbContract.class)
                                     .set(field(EwbContract::getInspectionType), InspectionType.TELEMEDIC)
                                     .create();
        doReturn(Map.of(
                InspectionType.TELEMEDIC, telemedicContract,
                InspectionType.TECHNIC, technicContract
                       )).when(ewbTariffService).getActiveContractByDepartmentId(departmentId1, true);
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        doReturn(Optional.of(organizationMedicalLicense)).when(repository).findByIdAndActiveIsTrue(telemedicContract.getOrganizationMedicalLicenseId());
        assertThat(service.getMedicalLicense(departmentId1))
                .isEqualTo(organizationMedicalLicense);
    }
}