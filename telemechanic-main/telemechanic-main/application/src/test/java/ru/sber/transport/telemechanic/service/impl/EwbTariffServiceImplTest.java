package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.telemechanic.database.dao.EwbTariffRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.database.projection.EwbContractDetailsProjection;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.BadRequestException;
import ru.sber.transport.telemechanic.exception.EwbTariffConflictException;
import ru.sber.transport.telemechanic.mapper.EwbContractMapper;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EwbTariffServiceImplTest {

    @InjectMocks
    private EwbTariffServiceImpl ewbTariffService;
    @Mock
    private EwbTariffRepository ewbTariffRepository;
    @Mock
    private EwbContractMapper ewbContractMapper;

    @Test
    void existsActiveTariffByOrganizationId() {
        var noOrganizationId = UUID.randomUUID();
        doReturn(false).when(ewbTariffRepository).existsByOrganizationIdAndActiveTrue(noOrganizationId);
        assertThat(ewbTariffService.existsActiveTariffByOrganizationId(noOrganizationId)).isFalse();

        var organizationId = UUID.randomUUID();
        doReturn(true).when(ewbTariffRepository).existsByOrganizationIdAndActiveTrue(organizationId);
        assertThat(ewbTariffService.existsActiveTariffByOrganizationId(organizationId)).isTrue();
    }

    @Test
    void getActiveContractByDepartmentId() {
        var departmentId1 = UUID.randomUUID();
        var departmentId2 = UUID.randomUUID();
        var departmentId3 = UUID.randomUUID();
        var departmentId4 = UUID.randomUUID();
        var departmentId5 = UUID.randomUUID();
        var departmentId6 = UUID.randomUUID();
        var technicContract1 = Instancio.of(EwbContract.class)
                .supply(Select.field("inspectionType"), () -> InspectionType.TECHNIC)
                .create();
        var technicContract2 = Instancio.of(EwbContract.class)
                .supply(Select.field("inspectionType"), () -> InspectionType.TECHNIC)
                .create();
        var medicContract1 = Instancio.of(EwbContract.class)
                .supply(Select.field("inspectionType"), () -> InspectionType.MEDIC)
                .create();
        var medicContract2 = Instancio.of(EwbContract.class)
                .supply(Select.field("inspectionType"), () -> InspectionType.MEDIC)
                .create();
        doReturn(Set.of(technicContract1, medicContract1))
                .when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId1);
        doReturn(Set.of(technicContract1))
                .when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId2);
        doReturn(Set.of(medicContract1))
                .when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId3);
        doReturn(Set.of(technicContract1, medicContract1, technicContract2))
                .when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId4);
        doReturn(Set.of(technicContract1, medicContract1, medicContract2))
                .when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId5);
        doReturn(Collections.emptySet()).when(ewbTariffRepository).findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId6);
        assertThat(ewbTariffService.getActiveContractByDepartmentId(departmentId1, true))
                .usingRecursiveComparison()
                .isEqualTo(Map.of(InspectionType.TECHNIC, technicContract1, InspectionType.MEDIC, medicContract1));
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.getActiveContractByDepartmentId(departmentId2, true))
                .withMessage("У подразделения нет тарифа на оказание услуг: %s", InspectionType.getMedicineTypes());
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.getActiveContractByDepartmentId(departmentId3, true))
                .withMessage("У подразделения нет тарифа на оказание услуг: %s", Set.of(InspectionType.TECHNIC));
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.getActiveContractByDepartmentId(departmentId4, true))
                .withMessage("У подразделения может быть только один активный тариф на оказание услуги: %s", Set.of(InspectionType.TECHNIC));
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.getActiveContractByDepartmentId(departmentId5, true))
                .withMessage("У подразделения может быть только один активный тариф на оказание услуги: %s", InspectionType.getMedicineTypes());
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.getActiveContractByDepartmentId(departmentId6, true))
                .withMessage("Список тарифов не может быть пустым");
        assertThat(ewbTariffService.getActiveContractByDepartmentId(departmentId6, false)).isEmpty();
    }

    @Test
    void getAllByActiveAndByContractIds() {
        var ewbTariffs = Instancio.createList(EwbTariff.class);
        var contractIdList = Instancio.createList(UUID.class);
        doReturn(ewbTariffs).when(ewbTariffRepository).findAllByActiveIsTrueAndContractContractIdIn(contractIdList);
        assertThat(ewbTariffService.getAllByActiveAndByContractIds(contractIdList))
                .usingRecursiveComparison()
                .isEqualTo(ewbTariffs);
    }

    @Test
    void getContractorOrganizationId() {
        var departmentId = UUID.randomUUID();
        var activeTechnicContract = Instancio.of(EwbContract.class)
                .set(Select.field(EwbContract::getInspectionType), InspectionType.TECHNIC)
                .set(Select.field(EwbContract::isActive), true)
                .create();

        doReturn(activeTechnicContract.getOrganizationId()).when(ewbTariffRepository).findContractOrganizationIdByTariffDepartmentIdWithRecursiveHierarchy(departmentId);
        var actual = ewbTariffService.getContractorOrganizationId(departmentId);
        assertThat(actual).isEqualTo(activeTechnicContract.getOrganizationId());
    }

    @Test
    void validateEwbTariff() {
        var departmentId = UUID.randomUUID();
        var medicContract = createProjection(InspectionType.MEDIC);
        var technicContract = createProjection(InspectionType.TECHNIC);
        doReturn(Set.of(medicContract, technicContract))
                .when(ewbTariffRepository)
                .findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId, Set.of(InspectionType.MEDIC.name(),
                        InspectionType.TELEMEDIC.name(),
                        InspectionType.TECHNIC.name()));
        doReturn(new EwbContractDetails(InspectionType.MEDIC, null, null))
                .when(ewbContractMapper)
                .ewbContractDetailsProjectionToEwbContractDetails(medicContract);
        doReturn(new EwbContractDetails(InspectionType.TECHNIC, null, null))
                .when(ewbContractMapper)
                .ewbContractDetailsProjectionToEwbContractDetails(technicContract);

        var result = ewbTariffService.validateEwbTariff(departmentId);
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(EwbContractDetails::inspectionType)
                .containsExactlyInAnyOrder(InspectionType.MEDIC, InspectionType.TECHNIC);
    }

    @Test
    void validateEwbTariffThrowsException() {
        var departmentId = UUID.randomUUID();
        var medicContract = createProjection(InspectionType.MEDIC);
        var technicContract = createProjection(InspectionType.TECHNIC);
        var medicContract2 = createProjection(InspectionType.MEDIC);
        doReturn(Set.of())
                .when(ewbTariffRepository)
                .findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId, Set.of(InspectionType.MEDIC.name(),
                        InspectionType.TELEMEDIC.name(),
                        InspectionType.TECHNIC.name()));
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbTariffService.validateEwbTariff(departmentId))
                .withMessage("Для подразделения id=%s должны быть заведены два тарифа.".formatted(departmentId));

        doReturn(Set.of(medicContract))
                .when(ewbTariffRepository)
                .findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId, Set.of(InspectionType.MEDIC.name(),
                        InspectionType.TELEMEDIC.name(),
                        InspectionType.TECHNIC.name()));
        doReturn(new EwbContractDetails(InspectionType.MEDIC, null, null))
                .when(ewbContractMapper)
                .ewbContractDetailsProjectionToEwbContractDetails(medicContract);
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbTariffService.validateEwbTariff(departmentId))
                .withMessage("Для подразделения id=%s должны быть заведены два тарифа.".formatted(departmentId));

        doReturn(Set.of(technicContract))
                .when(ewbTariffRepository)
                .findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId, Set.of(InspectionType.MEDIC.name(),
                        InspectionType.TELEMEDIC.name(),
                        InspectionType.TECHNIC.name()));
        doReturn(new EwbContractDetails(InspectionType.TECHNIC, null, null))
                .when(ewbContractMapper)
                .ewbContractDetailsProjectionToEwbContractDetails(technicContract);
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> ewbTariffService.validateEwbTariff(departmentId))
                .withMessage("Для подразделения id=%s должны быть заведены два тарифа.".formatted(departmentId));

        doReturn(Set.of(medicContract, technicContract, medicContract2))
                .when(ewbTariffRepository)
                .findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId, Set.of(InspectionType.MEDIC.name(),
                        InspectionType.TELEMEDIC.name(),
                        InspectionType.TECHNIC.name()));
        doReturn(new EwbContractDetails(InspectionType.MEDIC, LocalDate.now(), null))
                .when(ewbContractMapper)
                .ewbContractDetailsProjectionToEwbContractDetails(medicContract2);
        assertThatExceptionOfType(EwbTariffConflictException.class)
                .isThrownBy(() -> ewbTariffService.validateEwbTariff(departmentId))
                .withMessage("У департамента должны быть только два тарифа и один технический, второй медицинский.");
    }

    private EwbContractDetailsProjection createProjection(InspectionType inspectionType) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(EwbContractDetailsProjection.class);
        projection.setInspectionType(inspectionType.name());

        return projection;
    }
}