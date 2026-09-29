package ru.sberbank.ditsib.transport.dto.tariff;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
public class TransportTypeDto {
    
    private final UUID id;
    
    private final String name;
    
}
