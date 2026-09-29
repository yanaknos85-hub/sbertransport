package ru.sberbank.ditsib.transport.request.database.model.driversData;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.DriverLicenseClass;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(schema = "request", name = "driver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Driver {
    
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    
    /**
     * Контрагент
     */
    @Column
    private UUID contractorId;
    
    /**
     * Автопарк
     */
    @Column
    private UUID autoparkId;
    
    /**
     * Список признаков водителя
     */
    @ManyToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.EAGER)
    @JoinTable(schema = "request", name = "driver_tag_connector",
               joinColumns = @JoinColumn(name = "driver_id", nullable = false),
               inverseJoinColumns = @JoinColumn(name = "tag_id", nullable = false))
    @Builder.Default
    private Set<DriverTag> tags = new HashSet<>();
    
    /**
     * Фамилия
     */
    @Column(name = "last_name")
    private String lastName;
    
    /**
     * Имя
     */
    @Column(name = "first_name")
    private String firstName;
    
    /**
     * Отчество
     */
    @Column
    private String patronymic;
    
    /**
     * Человекочитаемый идентификатор
     */
    @Column(name = "humanreadableid")
    private String humanReadableId;
    
    /**
     * Серия и номер паспорта
     */
    @Column
    private String passport;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;
    
    /**
     * Рейтинг водителя
     */
    @Column(name = "rating")
    @Min(0)
    @Max(500)
    @Builder.Default
    private Integer rating = 500;
    
    /**
     * Номер лицензии предоставления услуг
     */
    @Column
    private String serviceProviderLicenseNumber;
    
    /**
     * Опыт вождения
     */
    @Column
    @Builder.Default
    private String experience = "LESS_THEN_FIVE";
    
    /**
     * Серия и номер водительского удостоверения
     */
    @Column
    private String driverLicenseNumber;
    
    /**
     * Категории прав водителя
     */
    
    @ElementCollection(targetClass = DriverLicenseClass.class, fetch = FetchType.EAGER)
    @CollectionTable(schema = "request", name = "driver_license_classes", joinColumns = @JoinColumn(name = "driver_id"))
    @Column(name = "class")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DriverLicenseClass> licenseClasses = new HashSet<>();
}
