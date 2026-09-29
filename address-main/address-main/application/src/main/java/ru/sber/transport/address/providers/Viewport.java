package ru.sber.transport.address.providers;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Viewport {
    
    private final BigDecimal topLeftLatitude;
    
    private final BigDecimal topLeftLongitude;
    
    private final BigDecimal bottomRightLatitude;
    
    private final BigDecimal bottomRightLongitude;
    
}
