package ru.sberbank.ditsib.transport.request.validate.request.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.EmployeeDTO;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.request.validate.request.NewRequestValidator;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class MobilePhoneValidator implements NewRequestValidator {

    private final EmployeeService employeeService;

    @Override
    public void validate(@NonNull NewRequestDTO request, @NonNull Employee employee) {
        var mobilePhone = Optional.of(request)
                .map(NewRequestDTO::getPassenger)
                .map(EmployeeDTO::id)
                .flatMap(employeeService::get)
                .map(Employee::getMobilePhone)
                .orElse("");
        if (mobilePhone.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Не найден номер мобильного телефона пассажира - заказ невозможен");
        }
    }
}
