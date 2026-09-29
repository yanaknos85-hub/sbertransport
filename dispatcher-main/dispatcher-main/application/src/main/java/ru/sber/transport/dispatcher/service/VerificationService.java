package ru.sber.transport.dispatcher.service;

import ru.sber.transport.dispatcher.database.model.Shift;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для верификации взаимодействия с данными
 */
public interface VerificationService {

    void checkShiftsExistence(List<Shift> shifts);

    void checkAutoParkToContractorRelation(String autoParkName, UUID contractorId, boolean exists);

    void checkVehicleBusyness(Shift shift);
}
