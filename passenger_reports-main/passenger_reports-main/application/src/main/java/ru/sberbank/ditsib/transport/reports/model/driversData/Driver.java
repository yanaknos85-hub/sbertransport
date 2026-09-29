package ru.sberbank.ditsib.transport.reports.model.driversData;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.DrivingExperience;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id")
public class Driver {
    private UUID id;
    
    /**
     * Контрагент
     */
    private UUID contractorId;
    
    /**
     * Автопарк
     */
    private UUID autoparkId;
    
    /** Список признаков водителя */
    private Set<String> tags = new HashSet<>();
    
    /**
     * Фамилия
     */
    private String lastName;
    
    /**
     * Имя
     */
    private String firstName;
    
    /**
     * Отчество
     */
    private String patronymic;
    
    /**
     * Серия и номер паспорта
     */
    private String passport;
    
    private String contactPhone;
    
    private Integer rating = 500;
    
    private String serviceProviderLicenseNumber;
    
    private String experience = DrivingExperience.LESS_THEN_FIVE.name();
    
    private String driverLicenseNumber;
    
    private Set<String> licenseClasses = new HashSet<>();
    
    private boolean active;
    
}
