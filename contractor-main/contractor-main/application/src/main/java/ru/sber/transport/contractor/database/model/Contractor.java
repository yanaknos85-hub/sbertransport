package ru.sber.transport.contractor.database.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity of contractor.
 */
@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(schema = "contractors", name = "contractor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contractor {

    /**
     * Идентификатор контрагента
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Цифровой идентификатор, используется для генерации человекочитаемого идентификатора ka-*****
     */
    @org.hibernate.annotations.Generated
    @Column(name = "digit_id", insertable = false, updatable = false)
    private Long digitId;

    @Column(name = "employee_count")
    private int employeeCount;

    /**
     * Имя контрагента
     */
    @Column(nullable = false)
    private String name;

    /**
     * ОГРН Контрагента
     */
    @Column(nullable = false)
    private String msrn;

    /**
     * ИНН контрагента
     */
    @Column(nullable = false)
    private String tin;

    /**
     * Информация о контактном лице контрагента
     */
    @Column(name = "contact_phone_number")
    private String contactPersonPhone;

    /**
     * Информация о контактной почте контрагента
     */
    @Column(name = "contact_person_email")
    private String contactPersonEmail;

    /**
     * Рейтинг контрагента
     */
    @Column(name = "rating")
    @Builder.Default
    private Integer rating = 500;

    /**
     * Лого контрагента
     */
    @Column
    private String img;

    /**
     * Тип интеграционного взаимодействия
     */
    @Column(name = "integration_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private IntegrationType integrationType = IntegrationType.EMAIL_XML_API;

    @Builder.Default
    private EmailIntegrationParams integrationParams = new EmailIntegrationParams();

    @Builder.Default
    private JsonIntegrationParams jsonIntegrationParams = new JsonIntegrationParams();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(schema = "contractors", name = "contractor_organization", joinColumns = @JoinColumn(name = "contractor_id"))
    @Column(name = "organization_id")
    @Builder.Default
    private Set<UUID> organizations = new HashSet<>();

    @Column
    @Builder.Default
    boolean autoassign = false;

    @Column
    @Builder.Default
    private boolean active = true;

    /**
     * Тип услуги
     */
    @Column(name = "service_type")
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;

    /**
     * Тип интеграции
     */
    @Column(name = "contractor_type")
    @Enumerated(EnumType.STRING)
    private ContractorType contractorType;

    /**
     * Информация о контактном лице контрагента
     * Для старых КА, теперь используем:
     * {@link #contactPersonFirstName}
     * {@link #contactPersonLastName}
     * {@link #contactPersonPatronymic}
     * <p>
     * Для обратной совместимости используем {@link #getContactPersonInfo()}
     */
    @Deprecated(since = "release 04.009")
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    @Column(name = "contact_person_info")
    private String contactPersonInfoOld;

    /**
     * Имя контактного лица
     */
    @Column(name = "contact_person_first_name")
    private String contactPersonFirstName;

    /**
     * Фамилия контактного лица
     */
    @Column(name = "contact_person_last_name")
    private String contactPersonLastName;

    /**
     * Отчество контактного лица
     */
    @Column(name = "contact_person_patronymic")
    private String contactPersonPatronymic;

    /**
     * id внешней диспетчерской/автосервиса
     */
    @Column(name = "external_id")
    private UUID externalId;

    /**
     * Нормативное количество автомобилей
     */
    @Column(name = "vehicle_count_norm")
    private Integer vehicleCountNorm;

    public String getContactPersonInfo() {
        if (contactPersonInfoOld != null) {
            return contactPersonInfoOld;
        } else if (contactPersonPatronymic != null && !contactPersonPatronymic.isEmpty()) {
            return contactPersonLastName + " " + contactPersonFirstName + " " + contactPersonPatronymic;
        } else {
            return contactPersonLastName + " " + contactPersonFirstName;
        }
    }
}
