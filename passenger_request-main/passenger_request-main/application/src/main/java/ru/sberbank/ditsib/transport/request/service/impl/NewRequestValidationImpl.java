package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.service.NewRequestValidation;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewRequestValidationImpl implements NewRequestValidation {

    private final List<NewRequestValidator> validators;

    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        final var transportType = request.getTransportType();

        log.debug("Запуск валидации заявки: transportType={}", transportType);

        validators
                .stream()
                .filter(it -> checkValidationEnabled(it, transportType))
                .forEach(it -> it.validate(request, employee));

        log.debug("Валидация заявки завершена: transportType={}", transportType);
    }

    private boolean checkValidationEnabled(@NonNull NewRequestValidator validator, @NonNull TransportTypeEnum transportType) {
        final var enabled = validator.validationEnabledFor();
        final var disabled = validator.validationDisabledFor();
        return enabled.stream()
                .filter(it -> !disabled.contains(it))
                .anyMatch(transportType::equals);
    }

}
