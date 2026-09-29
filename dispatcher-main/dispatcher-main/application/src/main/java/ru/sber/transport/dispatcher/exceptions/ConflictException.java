package ru.sber.transport.dispatcher.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {
    public static final String VEHICLE_COUNT_NORM_AVAILABLE_EXCEPTION_MESSAGE = "Переданное значение норматива количества автомобилей в филиале (%s) не может превышать " +
            "доступное для ввода количество автомобилей (%s)";
    public static final String VEHICLE_COUNT_NORM_TOTAL_EXCEPTION_MESSAGE = "Переданное значение норматива количества автомобилей во внутреннем автопарке (%s) не может быть меньше " +
            "общего количества автомобилей по филиалам автопарка (%s)";
    public static final String DISPATCHER_ATTORNEY_NUMBER_ALREADY_EXISTS_EXCEPTION_MESSAGE = "Конфликт данных. В системе уже существует диспетчер с номером доверенности %s";
    public ConflictException(String message) {
        super(message);
    }
}
