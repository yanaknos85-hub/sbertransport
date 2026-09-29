package ru.sberbank.transport.oto.cargo.database.model.drivers_data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.DrivingExperience;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(of = "id")
public class Driver {
    
    @Setter
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
    @Builder.Default
    private final Set<String> tags = new HashSet<>();
    
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
    
    private int rating = 500;
    
    private String serviceProviderLicenseNumber;
    
    private final String experience = DrivingExperience.LESS_THEN_FIVE.name();
    
    private String driverLicenseNumber;
    
    private final Set<String> licenseClasses = new HashSet<>();
    
}
