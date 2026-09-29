package ru.sberbank.ditsib.transport.tariff.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception of tariff already added.
 */
@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Tariff already added")
public class TariffAlreadyExistsException extends RuntimeException {
    
    private static String TARIFF_NAME_FORMAT = "Tariff with name '%s' already exists";
    
    public static String TARIFF_COMBO_FORMAT = "В системе уже существует активный тариф с запрашиваемой комбинацией %s";
    
    /**
     * Create a new exception.
     *
     * @param tariffName name of tariff.
     */
    public TariffAlreadyExistsException(String tariffName) {
        super(String.format(TARIFF_NAME_FORMAT, tariffName));
    }
    
    public TariffAlreadyExistsException(String pattern, String... agrs) {
        super(String.format(pattern, agrs));
    }
}
