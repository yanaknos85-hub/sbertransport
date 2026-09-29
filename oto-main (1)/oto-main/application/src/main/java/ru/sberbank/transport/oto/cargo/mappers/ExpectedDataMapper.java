package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.Mapper;
import ru.sberbank.transport.oto.cargo.dto.ExpectedDataDTO;
import ru.sberbank.transport.oto.cargo.database.model.ExpectedData;
import ru.sberbank.transport.oto.cargo.dto.cargo.ExpectedCargoDataDTO;

@Mapper
public interface ExpectedDataMapper {
    
    ExpectedDataDTO toDto(ExpectedData source);

    ExpectedCargoDataDTO toCargoDto(ExpectedData source);
    
}
