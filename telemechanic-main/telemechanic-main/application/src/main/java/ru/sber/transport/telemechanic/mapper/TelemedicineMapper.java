package ru.sber.transport.telemechanic.mapper;

import java.time.LocalDateTime;
import java.time.ZoneId;

import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.MedicRequest;
import ru.sber.transport.telemechanic.database.model.MedicRequestHistory;
import ru.sber.transport.telemechanic.database.projection.TelemedicineSearchProjection;
import ru.sber.transport.telemechanic.dto.telemedicine.*;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = {
                ZoneId.class
        }
)
public interface TelemedicineMapper {
    @Mapping(target = "id", source = "medicRequest.id")
    @Mapping(target = "ewbId", source = "id")
    @Mapping(target = "ewbHumanReadableId", source = "humanReadableId")
    @Mapping(target = "humanReadableId", source = "medicRequest.humanReadableId")
    @Mapping(target = "medCheckupType", constant = "1-Предрейсовый")
    @Mapping(target = "status", source = "medicRequest.status")
    @Mapping(target = "medic", expression = "java(ewbToGetTelemedicineDtoMedic(source))")
    @Mapping(target = "driver.id", source = "driver.employee.id")
    @Mapping(target = "driver.tin", ignore = true)
    @Mapping(target = "driver.organizationName", source = "driver.employee.organization.officialName")
    @Mapping(target = "driver.fullName", source = "driver.employee.FIO")
    @Mapping(target = "driver.drivingLicenseId", ignore = true)
    @Mapping(target = "driver.series", ignore = true)
    @Mapping(target = "driver.number", ignore = true)
    @Mapping(target = "driver.issueDate", ignore = true)
    @Mapping(target = "request.systPressure", source = "medicRequest.systPressure")
    @Mapping(target = "request.dyastPressure", source = "medicRequest.dyastPressure")
    @Mapping(target = "request.pulse", source = "medicRequest.pulse")
    @Mapping(target = "request.temperature", source = "medicRequest.temperature")
    @Mapping(target = "request.bloodAlcohol", source = "medicRequest.bloodAlcohol")
    @Mapping(target = "request.comment", source = "medicRequest.comment")
    GetTelemedicineDto ewbToTelemedicineDto(Ewb source);
    
    @Mapping(target = "creationTime", source = "creationTime", qualifiedByName = "creationTimeToZoneCreationTime")
    TelemedicineSearchResponse telemedicineSearchProjectionToTelemedicineSearchResponse(TelemedicineSearchProjection source);
    
    List<TelemedicineSearchResponse> listTelemedicineSearchProjectionToListTelemedicineSearchResponse(List<TelemedicineSearchProjection> source);
    
    @Named("creationTimeToZoneCreationTime")
    default LocalDateTime mapLocalDateTimeToZonedDateTime(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("UTC+03:00")).toLocalDateTime();
    }
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medicRequestId", source = "medicRequest.id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "oldStatus", source = "medicRequest.status")
    @Mapping(target = "changeTime", expression = "java(LocalDateTime.now())")
    MedicRequestHistory medicRequestToMedicRequestHistory(MedicRequest medicRequest, TelemedicineStatus status, String comment, Employee initiator);
    
    @Mapping(target = "alcohol", source = "bloodAlcohol")
    @Mapping(target = "decisionTime", expression = "java(LocalDateTime.now())")
    TelemedicineDeclineResultDto declinedTelemedicineRequestToTelemedicineDeclineResultDto(DeclinedTelemedicineRequest source);
    
    @Mapping(target = "decisionTime", source = "medicDecisionDateTime")
    TelemedicineDeclineResultDto telemedicineResultRequestExamToTelemedicineDeclineResultDto(ExamInfo source);

    default GetTelemedicineDto.Medic ewbToGetTelemedicineDtoMedic(Ewb source) {
        if (source.getMedic() != null) {
            var medic = source.getMedic();
            return new GetTelemedicineDto.Medic(
                    medic.getId(),
                    medic.getOrganization().getOfficialName(),
                    medic.getPosition().getPositionName(),
                    medic.getFIO(),
                    null, null, null, null, null
            );
        } else if (source.getMedicContractor() != null) {
            var medic = source.getMedicContractor();
            return new GetTelemedicineDto.Medic(
                    medic.getId(),
                    medic.getOrganization(),
                    medic.getPosition(),
                    medic.getFullName(),
                    null, null, null, null, null
            );
        } else {
            return null;
        }
    }
}
