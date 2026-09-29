package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Technic contract not found")
public class TechnicContractNotFoundException extends BusinessException {
    
    public TechnicContractNotFoundException() {
        super("У организации нет активных договоров или тарифов на проведение технических осмотров");
    }
}
