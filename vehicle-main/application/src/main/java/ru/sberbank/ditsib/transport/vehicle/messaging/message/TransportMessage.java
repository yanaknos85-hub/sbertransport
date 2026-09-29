package ru.sberbank.ditsib.transport.vehicle.messaging.message;

import ru.sber.transport.messaging.Message;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;


public record TransportMessage(
        UUID id,
        String stateNumber,
        String brand,
        String model,
        String transportType,
        int year,
        String vin,
        int currentMileage,
        String type,
        String subtype,
        Set<UUID> organizationIds,
        boolean deleted,
        Set<UUID> departmentIds,
        UUID vehicleId,
        LocalDate exploitationStart,
        LocalDate exploitationEnd,
        String status,
        UUID modelId,
        UUID brandId,
        Set<UUID> fuelTypeIds,
        UUID engineTypeId,
        Integer fuelTankVolume,
        String inventoryNumber,
        BigDecimal cityConsumptionRate,
        BigDecimal countryConsumptionRate,
        BigDecimal hybridConsumptionRate,
        UUID accessiblePositionId,
        UUID contractorId,
        UUID autoparkId,
        String balanceUnitNumber,
        String facility,
        String equipmentUnitSystemNumber,
        String locationAddress,
        String parkingAddress,
        String bodyTypeTitle
) implements Message<UUID> {
    
    @Override
    public UUID getId() {
        return id;
    }
}
