package ru.sberbank.ditsib.transport.request.database.model.approvals.settings;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

/**
 * Элемент настроек по региону и целям поездки
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurposeAndRegionApprovalSettingsItem {
    
    /** ID цели поездки */
    @Column(name = "trip_purpose_id")
    private UUID tripPurposeId;
    
    /** ID Геозоны. В случае, если она не указана, действие настройки распространяется на все регионы */
    @Column(name = "region_id")
    private UUID regionId;
    
    /** Сумма, не требующая согласования */
    @Column(name = "min_cost_to_be_approved", columnDefinition = "int4 (Types#INTEGER)")
    private long minCostToBeApproved;
}
