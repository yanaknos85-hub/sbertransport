package ru.sberbank.ditsib.transport.tariff.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithConflictTariffDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportDTO;

@Mapper
public interface TransportMapper {
    
    TransportWithConflictTariffDTO toDto(TransportDTO dto);
    
    TransportWithCalculateDTO toCalculateDto(TransportDTO dto);
    
}
