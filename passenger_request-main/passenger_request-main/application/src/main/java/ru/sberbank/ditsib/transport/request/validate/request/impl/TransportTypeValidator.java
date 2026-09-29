package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UnsupportedTransportTypeException;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.util.Set;

@Component
public class TransportTypeValidator implements NewRequestValidator {

    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        throw new UnsupportedTransportTypeException(request.getTransportType());
    }

    @Override
    public Set<TransportTypeEnum> validationDisabledFor() {
        return Set.of(TransportTypeEnum.TAXI, TransportTypeEnum.PERSONAL, TransportTypeEnum.CARSHARING, TransportTypeEnum.GROUP_TRANSFER);
    }

}
