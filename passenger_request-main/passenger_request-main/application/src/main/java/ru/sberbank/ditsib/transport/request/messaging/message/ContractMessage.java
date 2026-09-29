package ru.sberbank.ditsib.transport.request.messaging.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import ru.sber.transport.messaging.Message;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Contract message.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractMessage implements Message<UUID> {
    
    private UUID id;
    
    /**
     * Вид транспортной услуги
     */
    private String serviceType;
    
    /**
     * Регион
     */
    private String region;
    
    /**
     * тип транспорта
     */
    private String transportType;
    
    /**
     * ID контрагента
     */
    private UUID contractorId;
    
    /**
     * Сумма
     */
    private Long sum;
    
    /**
     * Дата начала.
     */
    private LocalDate startDate;
    
    /**
     * Дата конца.
     */
    private LocalDate endDate;
    
    /**
     * Автор записи
     */
    private UUID userId;
    
    /**
     * Время создания.
     */
    private LocalDateTime creationTime;

    /**
     * Организации
     */
    private Set<UUID> organizations;
    
    
    /**
     * Тип интеграции для контракта. Заполняется только для такси
     */
    @Builder.Default
    @Schema(description = "Используемый тип интеграции. Поля заполняется для такси, для других видов транспорта " +
                          "игнорируется", defaultValue = TaxiExternalIntegrationType.Constants.EMAIL_XML_API_STRING)
    private TaxiExternalIntegrationType integrationType = TaxiExternalIntegrationType.EMAIL_XML_API;
    
    /**
     * Флаг активности.
     */
    @Builder.Default
    private boolean active = true;
    
    /**
     * Флаг удаления контракта.
     */
    @Builder.Default
    private boolean deleted = false;
    
    /**
     * Номер договора УВХД
     */
    @Schema(description = "Номер договора УВХД")
    private String uvhd;
    
    /**
     * Флаг наличия НДС.
     */
    @Builder.Default
    private boolean includeVat = false;
    
    /**
     * Номер договора.
     */
    private String contractNumber;
}
