package ru.sber.transport.contractor.exceptions.business;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Have active contracts")
public class HaveActiveContractsException extends BusinessException {

    public static final String DEACTIVATE_CONTRACTOR_WITH_ACTIVE_CONTRACTS_MSG = "Нельзя удалить Контрагента. " +
            "Существуют активные Договоры по этому контрагенту. Удалите договоры и повторите попытку";

    public HaveActiveContractsException(String message) {
        super(message);
    }
}
