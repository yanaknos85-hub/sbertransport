package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sber.transport.contractor.messages.ContractorMessage;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.dto.carsharing.ContractorDTO;
import ru.sberbank.ditsib.transport.request.dto.oto.ContractorShortDTO;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ContractorMapper {

    Contractor messageToEntity(ContractorMessage message);

    ContractorDTO entityToDto(Contractor contractor);

    ContractorShortDTO entityToShortDto(Contractor contractor);
}
