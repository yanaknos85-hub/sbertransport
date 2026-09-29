package ru.sber.transport.telemechanic.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.service.EwbFilenameService;
import ru.sber.transport.telemechanic.service.EwbTariffService;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EwbFilenameServiceImpl implements EwbFilenameService {
    
    private final EwbTariffService ewbTariffService;
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    private final Clock clock;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ZERO = "0";
    
    @Override
    @Transactional
    public String generateFilename(EwbTitleType titleType, UUID organizationId, UUID departmentId) {
        var fleetOwnerOrganization = fleetOwnerOrganizationService.get(organizationId);
        var inspectionTypeEwbContractMap = ewbTariffService.getActiveContractByDepartmentId(departmentId, true);
        return switch (titleType) {
            case FIRST -> buildFilename(titleType.getPrefix(),
                                        contractEdfPart(getMedicineContract(inspectionTypeEwbContractMap)),
                                        contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                        contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                        fleetOwnerOrganizationEdfPart(fleetOwnerOrganization),
                                        ZERO,
                                        currentDate(),
                                        randomUUID());
            case SECOND -> buildFilename(titleType.getPrefix(),
                                         fleetOwnerOrganizationEdfPart(fleetOwnerOrganization),
                                         contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                         contractEdfPart(getMedicineContract(inspectionTypeEwbContractMap)),
                                         currentDate(),
                                         randomUUID());
            case THIRD,
                 FOURTH -> buildFilename(titleType.getPrefix(),
                                         fleetOwnerOrganizationEdfPart(fleetOwnerOrganization),
                                         contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                         contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                         ZERO,
                                         currentDate(),
                                         randomUUID());
            case FIFTH -> buildFilename(titleType.getPrefix(),
                                        fleetOwnerOrganizationEdfPart(fleetOwnerOrganization),
                                        contractEdfPart(getMedicineContract(inspectionTypeEwbContractMap)),
                                        contractEdfPart(inspectionTypeEwbContractMap.get(InspectionType.TECHNIC)),
                                        ZERO,
                                        currentDate(),
                                        randomUUID());
        };
    }
    
    private String buildFilename(Object... parts) {
        var sParts = Arrays.stream(parts)
                           .map(Object::toString)
                           .toArray(String[]::new);
        return String.join("_", sParts);
    }
    
    private String contractEdfPart(EwbContract contract) {
        return contract.getEdfOperatorId() + "-" + contract.getEdfCode();
    }
    
    private String fleetOwnerOrganizationEdfPart(FleetOwnerOrganization fleetOwnerOrganization) {
        return fleetOwnerOrganization.getEdfOperatorId() + "-" + fleetOwnerOrganization.getEdfCode();
    }
    
    private String currentDate() {
        return LocalDate.now(clock).format(DATE_FORMATTER);
    }
    
    private String randomUUID() {
        return UUID.randomUUID().toString();
    }
    
    private EwbContract getMedicineContract(Map<InspectionType, EwbContract> inspectionTypeEwbContractMap) {
        return inspectionTypeEwbContractMap.containsKey(InspectionType.MEDIC)
                ? inspectionTypeEwbContractMap.get(InspectionType.MEDIC)
                : inspectionTypeEwbContractMap.get(InspectionType.TELEMEDIC);
    }
}
