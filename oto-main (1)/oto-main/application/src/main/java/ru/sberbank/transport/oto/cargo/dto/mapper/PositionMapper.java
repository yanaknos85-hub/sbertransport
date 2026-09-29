package ru.sberbank.transport.oto.cargo.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;
import ru.sberbank.transport.oto.cargo.database.model.Position;

@Mapper
public interface PositionMapper {
    @Mapping(target = "name", source = "positionName")
    Position positionMessageToPosition(PositionMessage positionMessage);
    
}
