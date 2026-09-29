package ru.sberbank.ditsib.transport.request.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxiTariffMessage implements Message<UUID> {
    
    private UUID id;
    
    private String contractorTariffId;
    
    private String humanReadableId;
    
    private String serviceType;
    
    /** ID геозоны */
    private UUID regionId;
    
    private UUID organizationId;
    
    private String transportType;
    
    private UUID contractId;
    
    private UUID contractorId;

    @Builder.Default
    private boolean active = true;
    
    private String taxiClass;
    
    private Integer rideCostPerKm;

    @Builder.Default
    private Integer minRideDistanceCost = 0;
    
    private Integer rideCostPerMin;

    @Builder.Default
    private Integer minRideTimeCost = 0;
    
    private Integer waitCostPerMin;
    
    private Integer carServiceCost;
    
    private Integer waitCostPerMinIntermediate;
    
    private boolean deleted;

    @Builder.Default
    private Double distanceIncluded = 0d;

    @Builder.Default
    private Integer timeIncluded = 0;

    @Builder.Default
    private Integer freeWaitingTime = 0;
    
    private Integer maxDiffComputedDistancePercent;
    private Integer maxDiffFactDistancePercent;
    private Integer maxDiffComputedCostPercent;
    private Integer maxDiffContractorCostPercent;
    private Integer maxDiffComputedWaitingPercent;

    @Builder.Default
    private Double coefWorkDayMorning = 1d;

    @Builder.Default
    private Double coefWorkDayNoon = 1d;

    @Builder.Default
    private Double coefWorkDayEvening = 1d;

    @Builder.Default
    private Double coefWorkDayNight = 1d;

    @Builder.Default
    private Double coefDayOff = 1d;

    @Builder.Default
    private Double savingsDeviationPct = 0d;

    @Builder.Default
    private Double distanceDeviationKm = 0d;

    @Builder.Default
    private Integer timeDeviationMin = 0;

    @Builder.Default
    private Integer minCancelTimeMin = 30;
    
    private String workGroup;
    
    @Builder.Default
    private Integer triggerTime = 60;
    
}
