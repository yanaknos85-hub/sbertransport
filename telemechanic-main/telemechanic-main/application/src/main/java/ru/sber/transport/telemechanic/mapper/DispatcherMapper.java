package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.dto.dispatcher.AddDispatcherRequest;
import ru.sber.transport.telemechanic.dto.dispatcher.DispatcherInfo;
import ru.sber.transport.telemechanic.dto.dispatcher.GetDispatcherResponse;
import ru.sber.transport.telemechanic.dto.dispatcher.GetOrganizationDispatcherResponse;

import java.util.UUID;

@Mapper(componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true))
public interface DispatcherMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "employee.id", source = "employeeId")
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "department.id", source = "departmentId")
    @Mapping(target = "attorney.id", ignore = true)
    @Mapping(target = "attorney.number", source = "attorneyNumber")
    @Mapping(target = "attorney.issueDate", source = "issueDate")
    @Mapping(target = "attorney.expiryDate", source = "expiryDate")
    @Mapping(target = "attorney.creationSystem", source = "creationSystem")
    Dispatcher addDispatcherRequestToDispatcher(AddDispatcherRequest source);
    
    @Mapping(target = "dispatcher", expression = "java(dispatcherToDispatcherInfo(source))")
    @Mapping(target = "attorneyNumber", source = "attorney.number")
    @Mapping(target = "issueDate", source = "attorney.issueDate")
    @Mapping(target = "expiryDate", source = "attorney.expiryDate")
    @Mapping(target = "creationSystem", source = "attorney.creationSystem")
    GetDispatcherResponse dispatcherToGetDispatcherResponse(Dispatcher source);
    
    @Mapping(target = "personnelNumber", source = "employee.personnelNumber")
    @Mapping(target = "fullName", source = "employee.FIO")
    @Mapping(target = "organizationName", source = "organization.officialName")
    @Mapping(target = "departmentName", source = "department.departmentName")
    DispatcherInfo dispatcherToDispatcherInfo(Dispatcher source);
    
    default GetOrganizationDispatcherResponse dispatcherToGetOrganizationDispatcherResponse(
            Dispatcher dispatcher,
            String name,
            String msrn,
            String tin,
            String phone,
            String regionCode
                                                                                           ) {
        return new GetOrganizationDispatcherResponse(dispatcher.getId(),
                                                     regionCode,
                                                     new GetOrganizationDispatcherResponse.OrganizationDto(
                                                             dispatcher.getEmployee().getOrganization().getId(),
                                                             name,
                                                             msrn,
                                                             tin,
                                                             phone
                                                     ));
    }
}
