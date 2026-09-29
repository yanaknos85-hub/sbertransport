package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.Map;
import java.util.UUID;

/**
 * Сообщение с информацией о тарифе
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TariffMessage implements Message<UUID> {
    
    /**
     * ID
     */
    private UUID id;
    
    /**
     * Человекочитаемый ID
     */
    private String humanReadableId;
    
    /** Организация владелец тарифа */
    private UUID organizationId;
    
    /**
     * Регион
     * @deprecated Устаревшее. Используйте `regionId`. Будет удалено через 2 релиза (2021-06-10).
     */
    @Deprecated
    private String region;
    
    /**
     *  ID геозоны
     */
    private UUID regionId;
    
    /**
     * Дополнительные данные по ценам
     */
    @Singular
    private Map<String, Object> priceDetails;
    
    /**
     * ID типа транспорта
     */
    private UUID transportTypeId;
    
    /**
     * ID контракта с контрагентом
     */
    private UUID contractId;
    
    //Рабочая группа
    private String workGroup;
    
    /**
     * Флаг удаления
     */
    protected boolean deleted;

    /**
     * ID запаска
     */
    private UUID cloneId;
}
