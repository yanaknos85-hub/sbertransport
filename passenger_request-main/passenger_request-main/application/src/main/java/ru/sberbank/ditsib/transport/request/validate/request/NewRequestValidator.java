package ru.sberbank.ditsib.transport.request.validate.request;

import lombok.NonNull;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.NewRequestDTO;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Интерфейс валидатора новых заявок
 */
public interface NewRequestValidator {

    /**
     * Метод валидации новой заявки
     *
     * @param request заявка
     * @param employee сотрудник
     */
    void validate(@NonNull NewRequestDTO request, @NonNull Employee employee);

    /**
     * Cписок типов транспорта, для которых валидация включена
     *
     * @return список типов транспорта, для которых валидация включена
     */
    default Set<TransportTypeEnum> validationDisabledFor() {
        return Set.of();
    }

    /**
     * Cписок типов транспорта, для которых валидация включена
     *
     * @return список типов транспорта, для которых валидация включена
     */
    default Set<TransportTypeEnum> validationEnabledFor() {
        return Arrays.stream(TransportTypeEnum.values()).collect(Collectors.toUnmodifiableSet());
    }

}
