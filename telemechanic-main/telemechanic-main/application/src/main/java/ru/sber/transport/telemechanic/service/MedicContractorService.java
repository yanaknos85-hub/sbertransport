package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.MedicContractor;
import ru.sber.transport.telemechanic.dto.telemedicine.MedicInfo;

public interface MedicContractorService {
    
    MedicContractor saveIfNotExistsByPersonnelNumber(MedicInfo medicInfo);
}
