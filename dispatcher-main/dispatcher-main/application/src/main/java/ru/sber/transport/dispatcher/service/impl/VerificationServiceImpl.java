package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.service.VerificationService;
import ru.sber.transport.dispatcher.database.model.Shift;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class VerificationServiceImpl implements VerificationService {

    @Override
    public void checkShiftsExistence(List<Shift> shifts) {
        if(shifts.isEmpty()){
            throw new ConflictException("У водителя нет смен на сегодняшний день или смена еще не началась");
        }
    }

    @Override
    public void checkAutoParkToContractorRelation(String autoParkName, UUID contractorId, boolean exists) {
        if (!exists) {
            throw new ConflictException(
                    String.format("Автопарк %s не принадлежит контрагенту c ID %s", autoParkName,
                            contractorId.toString()));
        }
    }

    @Override
    public void checkVehicleBusyness(Shift shift) {
        if(shift.isActive()) {
            throw new ConflictException("Автомобиль "+shift.getVehicle().getStateNumber()+" находится в активной смене. Удалить автопарк нельзя.");
        }
    }
}
