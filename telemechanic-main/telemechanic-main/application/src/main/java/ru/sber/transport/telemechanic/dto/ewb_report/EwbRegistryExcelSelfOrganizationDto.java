package ru.sber.transport.telemechanic.dto.ewb_report;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class EwbRegistryExcelSelfOrganizationDto {
    
    private Map<String, String> registry;
}
