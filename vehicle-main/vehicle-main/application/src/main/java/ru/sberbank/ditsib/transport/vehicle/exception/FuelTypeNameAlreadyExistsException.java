package ru.sberbank.ditsib.transport.vehicle.exception;

import java.util.Collection;
import java.util.UUID;

public class FuelTypeNameAlreadyExistsException extends ConflictException {

    public FuelTypeNameAlreadyExistsException(Collection<String> name, Collection<UUID> fuelTypeId) {
        super(String.format("Наименования %s, уже связаны с типами топлива ИД: %s", name, fuelTypeId));
    }
}