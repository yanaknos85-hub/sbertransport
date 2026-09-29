package ru.sber.transport.dispatcher.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.dispatcher.database.model.ConflictReason;
import ru.sber.transport.dispatcher.database.model.ShiftConflict;
import ru.sber.transport.dispatcher.dto.ShiftConflictResponseDTO;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;

@Mapper
public interface ShiftConflictMapper {

    ShiftConflictResponseDTO toResponseDto(ShiftConflict shiftConflict);

    @Mapping(target = "routeId", ignore = true)
    @Mapping(target = "personnelNumber", source = "shiftFromMaisMessage.driverPersonnelNumber")
    void update(@MappingTarget ShiftConflict shift, ShiftFromMaisMessage shiftFromMaisMessage, ConflictReason conflictReason);

}
