package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.dto.AutoparkDTO;
import ru.sber.transport.dispatcher.messages.AutoparkMessage;

@Mapper(uses = ContractorMapper.class)
public interface AutoparkMapper {

    @Mapping(target = "contractor", source = "contractor", qualifiedByName = "short")
    AutoparkDTO toAutoparkDTO(Autopark entity);

    @Mapping(target = "contractorId", source = "contractor.id")
    @Mapping(target = "deleted", expression = "java(!entity.isActive())")
    AutoparkMessage toAutoparkMessage(Autopark entity);

}
