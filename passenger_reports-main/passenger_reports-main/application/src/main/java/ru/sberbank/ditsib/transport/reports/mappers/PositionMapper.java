package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.ditsib.transport.reports.dto.PositionShortDTO;
import ru.sberbank.ditsib.transport.reports.model.Position;

@Mapper
public interface PositionMapper {
    
    Position positionShortDTOToPosition(PositionShortDTO dto);

    @Mapping(target = "name", source = "positionName")
    Position positionMessageToPosition(PositionMessage positionMessage);
    
    @Mapping(target = "positionName", source = "name")
    PositionShortDTO positionToShortDto(Position source);
    
}
