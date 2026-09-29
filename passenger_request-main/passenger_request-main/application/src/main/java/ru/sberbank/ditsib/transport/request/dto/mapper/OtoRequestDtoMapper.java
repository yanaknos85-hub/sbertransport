package ru.sberbank.ditsib.transport.request.dto.mapper;

import org.mapstruct.*;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.RequestPersonalSearchDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestPublicSearchDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestTaxiSearchDTO;
import ru.sberbank.ditsib.transport.request.dto.oto.CarInfoDTO;
import ru.sberbank.ditsib.transport.request.dto.oto.OtoEngineerStreamRequestDto;
import ru.sberbank.ditsib.transport.request.dto.oto.OtoEngineerStreamResponseDto;

import java.time.Duration;
import java.util.Optional;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OtoRequestDtoMapper {
    RequestTaxiSearchDTO toTaxiSearchDto(OtoEngineerStreamRequestDto otoRequest);

    RequestPersonalSearchDTO toPersonalSearchDto(OtoEngineerStreamRequestDto otoRequest);

    RequestPublicSearchDTO toPublicSearchDto(OtoEngineerStreamRequestDto otoRequest);

    @Mapping(target = "contractor.id", source = "contractorId")
    @Mapping(target = "tripFinishTime", source = "finishedTime")
    @Mapping(target = "deadline", source = "driverArrivedDeadline")
    OtoEngineerStreamResponseDto fromTaxiRequest(RequestForTaxi request);
    
    default Long durationToMin(Duration duration) {
        return Optional.ofNullable(duration).map(Duration::toMinutes).orElse(null);
    }

    OtoEngineerStreamResponseDto fromPublicRequest(RequestForPublic request);

    @Mapping(target = "tripFinishTime", source = "finishedTime")
    OtoEngineerStreamResponseDto fromPersonalRequest(RequestForPersonal request);

    @Mapping(target = "contractor.id", source = "contractorId")
    @Mapping(target = "tripFinishTime", source = "finishedTime")
    OtoEngineerStreamResponseDto fromCarsharingRequest(RequestForCarsharing request);

    OtoEngineerStreamResponseDto fromRequest(Request request);

    EmployeeDTO employeeToDto(Employee employee);

    CarInfo carInfoFromDto(CarInfoDTO carInfoDTO);
}
