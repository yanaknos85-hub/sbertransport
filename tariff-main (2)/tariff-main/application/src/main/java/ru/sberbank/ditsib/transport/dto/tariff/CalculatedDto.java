package ru.sberbank.ditsib.transport.dto.tariff;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CalculatedDto {
    
    public static final String ENGINE_VOLUME_COEFFICIENT = "engineVolumeCoef";
    
    private String transportType;
    
    @Positive
    private long cost;
    
    @NotNull
    private UUID id;
    
}
