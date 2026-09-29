package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.EwbTariffRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.EwbTariff;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.exception.BadRequestException;
import ru.sber.transport.telemechanic.exception.EwbTariffConflictException;
import ru.sber.transport.telemechanic.mapper.EwbContractMapper;
import ru.sber.transport.telemechanic.service.EwbTariffService;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class EwbTariffServiceImpl implements EwbTariffService {
    
    private final EwbTariffRepository repository;
    private final EwbContractMapper ewbContractMapper;
    
    @Override
    public EwbTariff save(EwbTariff entity) {
        return repository.save(entity);
    }
    
    @Override
    public boolean existsActiveTariffByOrganizationId(UUID organizationId) {
        return repository.existsByOrganizationIdAndActiveTrue(organizationId);
    }
    
    @Override
    public Map<InspectionType, EwbContract> getActiveContractByDepartmentId(UUID departmentId, boolean checkNoTariffs) {
        var contracts = repository.findContractsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(departmentId);
        var result = new EnumMap<InspectionType, EwbContract>(InspectionType.class);
        Optional.ofNullable(extractContractByInspectionType(contracts, InspectionType.getMedicineTypes(), checkNoTariffs))
                .ifPresent(projection -> result.put(projection.getInspectionType(), projection));
        Optional.ofNullable(extractContractByInspectionType(contracts, Set.of(InspectionType.TECHNIC), checkNoTariffs))
                .ifPresent(projection -> result.put(InspectionType.TECHNIC, projection));
        return result;
    }

    @Override
    public Set<EwbContractDetails> validateEwbTariff(UUID departmentId) {
        var contractDetails = repository.findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(
                        departmentId,
                        Set.of(InspectionType.MEDIC.name(), InspectionType.TELEMEDIC.name(), InspectionType.TECHNIC.name()))
                .stream()
                .map(ewbContractMapper::ewbContractDetailsProjectionToEwbContractDetails)
                .collect(Collectors.toSet());
        if (contractDetails.isEmpty() || (contractDetails.size() == 1 && isTechnicOrMedic(contractDetails))) {
            throw new BadRequestException("Для подразделения id=%s должны быть заведены два тарифа.".formatted(departmentId));
        }
        if (contractDetails.size() != 2 && (contractDetails.stream()
                                                    .map(EwbContractDetails::inspectionType)
                                                    .collect(Collectors.toSet())
                                                    .containsAll(Set.of(InspectionType.MEDIC, InspectionType.TECHNIC))
                                            || contractDetails.stream()
                                                    .map(EwbContractDetails::inspectionType)
                                                    .collect(Collectors.toSet())
                                                    .containsAll(Set.of(InspectionType.TELEMEDIC, InspectionType.TECHNIC)))) {
            throw new EwbTariffConflictException("У департамента должны быть только два тарифа и один технический, второй медицинский.");
        }

        return contractDetails;
    }
    
    private boolean isTechnicOrMedic(Set<EwbContractDetails> contractDetails) {
        return contractDetails.stream()
                .map(EwbContractDetails::inspectionType)
                .allMatch(inspectionType -> InspectionType.getMedicineTypes().contains(inspectionType) || inspectionType.equals(InspectionType.TECHNIC));
    }
    
    @Override
    public List<EwbTariff> getAllByActiveAndByContractIds(List<UUID> contractIdList) {
        return repository.findAllByActiveIsTrueAndContractContractIdIn(contractIdList);
    }
    
    @Override
    public UUID getContractorOrganizationId(UUID tariffDepartmentId) {
        return repository.findContractOrganizationIdByTariffDepartmentIdWithRecursiveHierarchy(tariffDepartmentId);
    }

    @Override
    public Set<InspectionType> getInspectionTypesByDriverDepartmentId(UUID tariffDepartmentId) {
        return repository.findContractsWithDetailsByTariffDepartmentIdAndTypesWithRecursiveHierarchy(
                        tariffDepartmentId,
                        Set.of(InspectionType.MEDIC.name(),
                                InspectionType.TELEMEDIC.name(),
                                InspectionType.TECHNIC.name())).stream()
                .map(ewbContractMapper::ewbContractDetailsProjectionToEwbContractDetails)
                .map(EwbContractDetails::inspectionType)
                .collect(Collectors.toSet());
    }
    
    private EwbContract extractContractByInspectionType(Set<EwbContract> contracts, Set<InspectionType> inspectionTypes, boolean checkNoTariffs) {
        if (contracts.isEmpty() && checkNoTariffs) {
            throw new EwbTariffConflictException("Список тарифов не может быть пустым");
        }
        var filteredContracts = contracts.stream()
                              .filter(ewbContract -> inspectionTypes.contains(ewbContract.getInspectionType()))
                              .toList();
        if (filteredContracts.size() > 1) {
            throw new EwbTariffConflictException(
                    "У подразделения может быть только один активный тариф на оказание услуги: %s".formatted(inspectionTypes));
        } else if (filteredContracts.isEmpty() && checkNoTariffs) {
            throw new EwbTariffConflictException("У подразделения нет тарифа на оказание услуг: %s".formatted(inspectionTypes));
        } else if (filteredContracts.size() == 1) {
            return filteredContracts.getFirst();
        } else {
            return null;
        }
    }
}
