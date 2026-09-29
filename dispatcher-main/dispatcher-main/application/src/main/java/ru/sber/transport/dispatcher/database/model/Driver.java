package ru.sber.transport.dispatcher.database.model;

import lombok.*;

import jakarta.persistence.*;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность - Водитель. Один и тот же водитель может одновременно работать на разных контрагентов. Однако, внутри
 * базы конкретного контрагента водитель будет уникален (по фамилии, имени и номеру паспорта)
 */
@Entity
@Table(schema = "dispatcher", name = "driver", uniqueConstraints = @UniqueConstraint(columnNames = {
        "contractor_id", "passport"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Driver implements HasName {

    /**
     * ID водителя
     */
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "humanreadableid")
    private String humanReadableId;

    @ManyToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column
    private String patronymic;

    @Column
    private String passport;

    @Column(name = "contact_phone_number")
    private String contactPhone;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;

    /**
     * Рейтинг водителя
     */
    @Column(name = "rating")
    @Builder.Default
    private Integer rating = 500;

    /**
     * Опыт вождения
     */
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DrivingExperience experience = DrivingExperience.LESS_THEN_FIVE;

    /**
     * Номер лицензии предоставления услуг по пассажирским перевозкам
     */
    @Column
    private String serviceLicenseNumber;

    /**
     * Номер лиценции о предоставлении услуг по грузовым перевозкам
     */
    @Column(name = "cargo_licence_number")
    private String cargoLicenceNumber;

    /**
     * Серия и номер водительского удостоверения
     */
    @Column
    private String driverLicenseNumber;

    /**
     * Категории прав водителя
     */
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "dispatcher", name = "driver_licenses",
            joinColumns = @JoinColumn(name = "driver_id"))
    @Column(name = "license")
    @Builder.Default
    private Set<DriverLicense> driverLicenses = new HashSet<>();

    /**
     * Список признаков водителя
     */
    @ManyToMany
    @JoinTable(schema = "dispatcher", name = "driver_attribute",
            joinColumns = @JoinColumn(name = "driver", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "attribute", nullable = false))
    @Builder.Default
    private Set<Attribute> attributes = new HashSet<>();

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "point_time")
    private ZonedDateTime pointTime;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "email")
    private String email;

    @Column(name = "consent")
    private boolean consent;

    @Column(name = "online")
    private boolean online;

    @Column(name = "phone_confirmed")
    private boolean phoneConfirmed = false;

    /**Специализация водителя*/
    @Enumerated(value = EnumType.STRING)
    @Column(name = "driver_speciality")
    DriverSpecialityType driverSpeciality;

    @Column(name = "oauth_id")
    private UUID oauthId;

    @JoinColumn(name = "autopark_id")
    @ManyToOne
    private Autopark autopark;

    @Column(name = "personnel_number")
    private String personnelNumber;

    @Column(name = "snils")
    private String snils;

    @Column(name = "tin")
    private String tin;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}
