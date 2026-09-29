package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.enumerate.InspectionType;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Ewb tariff not found")
public class EwbTariffNotFoundException extends BusinessException {
    
    public EwbTariffNotFoundException(UUID id) {
        super("Не найден активный тариф для организации, id:%s".formatted(id));
    }
    
    public EwbTariffNotFoundException(UUID departmentId, InspectionType inspectionType) {
        super("Не найден активный тариф для подразделения, id:%s с типом: %s".formatted(departmentId, inspectionType.name()));
    }
}