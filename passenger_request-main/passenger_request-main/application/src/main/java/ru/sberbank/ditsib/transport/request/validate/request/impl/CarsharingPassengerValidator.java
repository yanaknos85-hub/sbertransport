package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.util.Set;

@Component
public class CarsharingPassengerValidator implements NewRequestValidator {

    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        if (!employee.getId().equals(request.getPassenger().id())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Заказ каршеринга коллеге невозможен.");
        }
    }

    @Override
    public Set<TransportTypeEnum> validationEnabledFor() {
        return Set.of(TransportTypeEnum.CARSHARING);
    }
}
