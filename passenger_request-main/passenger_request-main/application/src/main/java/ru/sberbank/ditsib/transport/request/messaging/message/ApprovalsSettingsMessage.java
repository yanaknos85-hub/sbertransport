package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.messaging.Message;

import java.util.List;
import java.util.UUID;

/**
 * Сообщение с настройками согласований
 */
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalsSettingsMessage implements Message<UUID> {
    
    /** Идентификатор */
    private UUID id;
    
    /** Ссылка на корп.клиента */
    private UUID organizationId;
    
    /** Необходимость этапа согласования */
    private boolean approvalActive;
    
    /** Сумма, не требующая согласования */
    @Builder.Default
    private long minCostToBeApproved = 0;
    
    /** Тип транспорта */
    private String transportType;
    
    /** Список элементов настроек по региону и целям поездки. Опциональная настройка */
    private List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems;

    /** Признак удаления настроек */
    private boolean deleted;
    
    /**
     * Элемент настроек по региону и целям поездки
     */
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurposeAndRegionApprovalSettingsItem {
        
        /** ID цели поездки */
        private UUID tripPurposeId;
        
        /** ID Геозоны. В случае, если она не указана, действие настройки распространяется на все регионы */
        private UUID regionId;
    
        /** Сумма, не требующая согласования */
        private long minCostToBeApproved;
    }
}
