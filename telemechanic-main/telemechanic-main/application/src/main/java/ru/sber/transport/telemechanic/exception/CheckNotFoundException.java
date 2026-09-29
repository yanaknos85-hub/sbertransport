package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Check not found")
public class CheckNotFoundException extends BusinessException {

    public static final String MSG_FORMAT = "Проверка не найдена, id заявки:'%s', тип проверки:%s";

    /**
     * Create a new exception.
     *
     * @param requestId ID of RequestCheckLis.
     * @param checkType type of requested Check.
     */
    public CheckNotFoundException(UUID requestId, CheckType checkType) {
        super(String.format(MSG_FORMAT, requestId, checkType));
    }
}
