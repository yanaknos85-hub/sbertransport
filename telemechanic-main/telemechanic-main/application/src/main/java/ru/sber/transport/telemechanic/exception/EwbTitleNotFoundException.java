package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sber.transport.telemechanic.common.EwbTitleType;

import java.util.UUID;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "EwbTitle not found")
public class EwbTitleNotFoundException extends RuntimeException {

    public EwbTitleNotFoundException(UUID ewbId, EwbTitleType titleType) {
        super("Для ЭПЛ=%s не найден %s".formatted(ewbId, titleType.getName()));
    }
    
    public EwbTitleNotFoundException(UUID id) {
        super("По id=%s не найден титул".formatted(id));
    }
}
