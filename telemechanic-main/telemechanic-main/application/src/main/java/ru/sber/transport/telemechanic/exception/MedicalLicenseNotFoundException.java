package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "medicla license not found")
public class MedicalLicenseNotFoundException extends RuntimeException{
    
    public static final String MSG_FORMAT_BY_ID = "Медицинская лицензия для сотрудника с идентификатором id=%s не найдена!";
    
    public static final String MSG_FORMAT_BY_ID_AND_DATE = "Медицинская лицензия для сотрудника с идентификатором id=%s не найдена! " +
                                                           "Или срок действия лицензии истёк! (дата окончания лицензии < %s)";
      
    
    public MedicalLicenseNotFoundException(UUID medicId) {
        super(MSG_FORMAT_BY_ID.formatted(medicId));
    }
    
    public MedicalLicenseNotFoundException(UUID medicId, LocalDate currentDate) {
        super(MSG_FORMAT_BY_ID_AND_DATE.formatted(medicId, currentDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }
}
