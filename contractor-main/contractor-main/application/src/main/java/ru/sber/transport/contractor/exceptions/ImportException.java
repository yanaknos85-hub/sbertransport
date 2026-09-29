package ru.sber.transport.contractor.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Import failure")
public class ImportException extends RuntimeException{
    
    public ImportException(String s) {
    
    }
}
