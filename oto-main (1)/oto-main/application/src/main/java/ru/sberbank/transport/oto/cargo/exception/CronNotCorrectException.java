package ru.sberbank.transport.oto.cargo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Cron not correct")
public class CronNotCorrectException extends RuntimeException {
    
    public static final String MSG_FORMAT = "For period not found parameters  %s";
    
    /**
     * Create a new exception.
     *
     * @param periodType period template.
     */
    public CronNotCorrectException(String periodType) {
        super(String.format(MSG_FORMAT, periodType));
    }
}
