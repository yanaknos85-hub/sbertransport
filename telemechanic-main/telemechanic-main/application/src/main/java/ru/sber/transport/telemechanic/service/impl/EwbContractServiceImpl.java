package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.EwbContractRepository;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.service.EwbContractService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class EwbContractServiceImpl implements EwbContractService {
    
    private final EwbContractRepository repository;
    
    @Override
    public EwbContract save(EwbContract entity) {
        return repository.save(entity);
    }

    @Override
    public List<EwbContract> getAllByInspectionTypeAndOrganizationId(Set<InspectionType> inspectionTypes, UUID organizationId) {
        return repository.findAllByInspectionTypeInAndOrganizationId(inspectionTypes, organizationId);
    }
}
