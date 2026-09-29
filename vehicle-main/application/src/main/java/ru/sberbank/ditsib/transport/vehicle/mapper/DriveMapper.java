package ru.sberbank.ditsib.transport.vehicle.mapper;

import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;

import java.util.List;

/**
 * @author skakun-a
 */
@Mapper(componentModel = "spring")
public interface DriveMapper {
    DriveDto driveToDriveDto(Drive entity);
    
    Drive driveToDriveDto(DriveDto dto);
    
    List<DriveDto> listDriveToListDriveDto(List<Drive> entities);
}
