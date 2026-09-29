package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сообщение сотрудника.
 */
@SuperBuilder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMessage implements Message<UUID> {
    
    /**
     * ID.
     */
    private UUID id;
    
    /**
     * ID связанного пользователя.
     */
    private UUID userId;
    
    /**
     * Человекочитаемый ID.
     */
    private String humanReadableId;
    
    /**
     * Имя.
     */
    private String firstName;
    
    /**
     * Фамилия.
     */
    private String lastName;
    
    /**
     * Отчество.
     */
    private String patronymic;
    
    /**
     * ТН.
     */
    private String personnelNumber;
    
    /**
     * ID подразделения.
     */
    private UUID departmentId;

    /**
     * Место возникновения затрат
     */
    private String costCenter;

    /**
     * Разъездной характер.
     */
    private String itinerantType;
    
    /**
     * ID организации.
     */
    private UUID organizationId;
    
    /**
     * ID должности.
     */
    private UUID positionId;
    
    /**
     * isDelegatedTrait
     */
    private boolean delegateTrait;
    
    /**
     * ID делегата.
     */
    private UUID delegatedById;// Не используется?
    
    /**
     * Доступные типы транспорта.
     */
    @Builder.Default
    private Set<String> availableTransportTypes = new HashSet<>();
    
    /**
     * Номер телефона.
     */
    private String mobilePhone;
    
    /**
     * Email.
     */
    private String email;

    /**
     * Свидетельство о браке.
     */
    private String marriageCertificateNumber;
    
    /**
     * ID руководителя.
     */
    private UUID supervisorId;
    
    /**
     * External or internal employee type
     */
    private String employeeType;
    
    /**
     * Consent for PND
     */
    private boolean consent;
    
    /**
     * Флаг удаленного пользователя.
     */
    @Builder.Default
    private boolean deleted = false;
    
    /**
     * Атрибуты сотрудника.
     */
    @Builder.Default
    private Set<String> attributes = new HashSet<>();
}
