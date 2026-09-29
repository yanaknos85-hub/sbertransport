package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.dto.CheckDto;
import ru.sber.transport.telemechanic.dto.MonitorCheckTreeDto;
import ru.sber.transport.telemechanic.dto.MonitoringCheckDto;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ru.sber.transport.telemechanic.enumerate.CheckStatus.DECLINE;

@Mapper(componentModel = "spring")
public interface CheckMapper {

    @Mapping(expression = "java(source.getCheckType().getOrdinal())", target = "ordinal")
    CheckDto checkToCheckDto(Check source);
    
    List<CheckDto> listCheckToListCheckDto(List<Check> source);

    @Mapping(target = "photos", expression = "java(getPhotos(source.getPhotos()))")
    @Mapping(target = "maxAttempt", source = "checkType.maxAttempt")
    @Mapping(target = "children", ignore = true)
    MonitoringCheckDto checkToMonitoringCheckDto(Check source);
    
    @Mapping(target = "maxAttempt", source = "checkType.maxAttempt")
    @Mapping(target = "comment", expression = "java(getComment(source))")
    @Mapping(target = "children", ignore = true)
    ChecksTreeDto.CheckDto checkToChecksTreeDto(Check source);
    
    @Mapping(target = "maxAttempt", source = "checkType.maxAttempt")
    @Mapping(target = "photos", expression = "java(getPhotos(source.getPhotos()))")
    @Mapping(target = "children", ignore = true)
    MonitorCheckTreeDto checkToMonitorCheckTreeDto(Check source);

    default List<UUID> getPhotos(Set<CheckPhoto> photos) {
        return photos.stream()
                .sorted(Comparator.comparing(CheckPhoto::getCreationTime))
                .map(CheckPhoto::getId)
                .toList();
    }
    
    default String getComment(Check source) {
        if (source.getCheckStatus().equals(DECLINE)) {
            return source.getComment();
        }
        return null;
    }
}
