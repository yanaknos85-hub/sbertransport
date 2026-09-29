package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Litreage out validation error")
public class LitreageValidationException extends BusinessException {
    
    public static final String INCORRECT_EWB_STATUS_MSG =
            "Показания остатка топлива могут быть внесены только для ЭПЛ в статусе \"Прохождение телемеханика\"";
    public static final String REQUEST_IN_PROGRESS_IS_ABSENT_MSG = "С ЭПЛ должна быть связана заявка на телемеханика в статусе IN_PROGRESS";
    public static final String LITREAGE_OUT_ZERO_VALUE_MSG = "Текущий остаток топлива не может быть меньше 0 литров";
    public static final String LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG =
            "Значение не может быть больше объема топливного бака автомобиля: %s л.";
    public static final String ONLY_EWB_DRIVER_CAN_PROVIDE_LITREAGE_OUT_MSG = "Только водитель по ЭПЛ может проходить проверки";
    
    public LitreageValidationException(String message) {
        super(message);
    }
}
