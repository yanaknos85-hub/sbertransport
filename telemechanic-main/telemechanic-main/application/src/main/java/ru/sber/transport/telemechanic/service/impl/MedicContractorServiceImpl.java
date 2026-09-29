package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.database.dao.MedicContractorRepository;
import ru.sber.transport.telemechanic.database.model.MedicContractor;
import ru.sber.transport.telemechanic.dto.telemedicine.MedicInfo;
import ru.sber.transport.telemechanic.service.MedicContractorService;

@Service
@RequiredArgsConstructor
public class MedicContractorServiceImpl implements MedicContractorService {
    
    private final MedicContractorRepository medicContractorRepository;
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MedicContractor saveIfNotExistsByPersonnelNumber(MedicInfo medicInfo) {
        var medicContractorOptional = medicContractorRepository.findByPersonnelNumber(medicInfo.personalNumber());
        return medicContractorOptional.orElseGet(() -> medicContractorRepository.saveAndFlush(new MedicContractor()
                                                                                                      .setFullName(medicInfo.fio())
                                                                                                      .setPersonnelNumber(medicInfo.personalNumber())
                                                                                                      .setOrganization(medicInfo.organization())
                                                                                                      .setDepartment(medicInfo.department())
                                                                                                      .setPosition(medicInfo.position())
                                                                                                      .setSignKeyNumber(medicInfo.serialNumber())
                                                                                                      .setSignKeyEndDateTime(medicInfo.serialEndDateTime())
                                                                                             ));
    }
}
