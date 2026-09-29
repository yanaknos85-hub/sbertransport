package ru.sber.transport.telemechanic.exception.driver;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.exception.BusinessException;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Snils format incorrect")
public class SnilsNotFormattedException extends BusinessException {
    
    public SnilsNotFormattedException(String snils) {
        super("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(snils));
    }
}
