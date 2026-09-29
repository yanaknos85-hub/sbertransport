package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.*;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.dto.RegistryDto;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.Objects;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {EmployeeMapper.class},
        builder = @Builder(disableBuilder = true))
public interface ReportMapper {

    @Mapping(source = "transport.stateNumber", target = "stateNumber")
    @Mapping(source = "transport.brand", target = "brand")
    @Mapping(source = "transport.model", target = "model")
    @Mapping(source = "author.personnelNumber", target = "personnelNumber")
    @Mapping(expression = "java(getFullNameForEmployee(source.getAuthor()))", target = "fullName")
    @Mapping(expression = "java(getInspectionMark(source.getStatus()))", target = "inspectionMark")
    @Mapping(source = "author.organization.officialName", target = "officialName")
    @Mapping(source = "inspector.personnelNumber", target = "inspectorPersonnelNumber")
    @Mapping(expression = "java(getFullNameForEmployee(source.getInspector()))", target = "inspectorFullName")
    RegistryDto requestToRegistryDto(Request source);

    default String getFullNameForEmployee(Employee employee) {
        return Objects.isNull(employee) ? "" : employee.getFIO();
    }

    default String getInspectionMark(RequestStatus status) {
        if (status.equals(RequestStatus.ON_THE_LINE) || status.equals(RequestStatus.FINISHED)) {
            return "Пройден";
        } else if (status.equals(RequestStatus.DECLINED)) {
            return "Не пройден";
        } else {
            return null;
        }
    }
}
