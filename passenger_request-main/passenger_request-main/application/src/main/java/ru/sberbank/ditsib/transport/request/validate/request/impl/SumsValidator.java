package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.exceptions.InvalidBonusSumException;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

@Component
public class SumsValidator implements NewRequestValidator {

    @Override
    public void validate(@NonNull NewRequestDTO dto, @NonNull Employee employee) {
        final var expectedData = dto.getExpected();
        if (expectedData == null) {
            return;
        }
        final var bonusCost = expectedData.getBonusCost();
        final var cost = expectedData.getCost();
        if (bonusCost != null && (double) bonusCost > cost) {
            throw new InvalidBonusSumException("Bonus cost cannot be greater than cost");
        }
    }

}
