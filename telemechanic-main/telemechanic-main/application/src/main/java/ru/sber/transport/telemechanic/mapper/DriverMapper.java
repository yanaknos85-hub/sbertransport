package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.database.model.DriverProjection;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.projection.DriverByFioProjection;
import ru.sber.transport.telemechanic.dto.driver.*;

@Mapper(componentModel = "spring",
        uses = { DrivingLicenseMapper.class },
        imports = { Employee.class })
public interface DriverMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", expression = "java(new Employee().setId(source.driver().employeeId()))")
    @Mapping(target = "drivingLicense", source = "drivingLicense")
    @Mapping(target = "tin", source = "driver.tin")
    @Mapping(target = "snils", source = "driver.snils")
    Driver addDriverRequestToDriver(AddDriverRequest source);
    
    @Mapping(target = "driver", expression = "java(driverByFioToDriverByFioDto(source))")
    @Mapping(target = "drivingLicense", expression = "java(driverByFioToDrivingLicenseByDriverFioDto(source))")
    DriverByFioResponse driverByFioToDriverByFioResponse(DriverByFioProjection source);
    
    @Mapping(target = "driver", expression = "java(driverToDriverInfo(source))")
    @Mapping(target = "drivingLicense", expression = "java(driverToDrivingLicenseInfo(source))")
    DriverByFioResponse driverToDriverByFioResponse(Driver source);
    
    DriverByFioResponse.DriverInfo driverByFioToDriverByFioDto(DriverByFioProjection source);
    
    @Mapping(target = "id", source = "drivingLicenceId")
    DriverByFioResponse.DrivingLicenseInfo driverByFioToDrivingLicenseByDriverFioDto(DriverByFioProjection source);
    
    @Mapping(target = "driver", expression = "java(driverProjectionToDriverSearchDto(source))")
    @Mapping(target = "drivingLicense", expression = "java(driverProjectionToDrivingLicenceDto(source))")
    DriverSearchResponse driverProjectionToDriverSearchResponseDto(DriverProjection source);
    
    DriverSearchDto driverProjectionToDriverSearchDto(DriverProjection source);
    
    DrivingLicenseDto driverProjectionToDrivingLicenceDto(DriverProjection source);
    
    @Mapping(target = "series", source = "drivingLicense.series")
    @Mapping(target = "number", source = "drivingLicense.number")
    @Mapping(target = "issueDate", source = "drivingLicense.issueDate")
    DriverByFioResponse.DrivingLicenseInfo driverToDrivingLicenseInfo(Driver source);
    
    @Mapping(target = "personnelNumber", source = "employee.personnelNumber")
    @Mapping(target = "fullName", expression = "java(mapFullName(source.getEmployee()))")
    @Mapping(target = "organizationName", source = "employee.organization.officialName")
    @Mapping(target = "departmentName", source = "employee.department.autoparkName")
    @Mapping(target = "departmentId", source = "employee.department.id")
    DriverByFioResponse.DriverInfo driverToDriverInfo(Driver source);
    
    @Mapping(target = "driver", source = "source")
    @Mapping(target = "drivingLicense", source = "drivingLicense")
    GetDriverResponse driverToGetDriverResponse(Driver source);
    
    @Mapping(target = "personnelNumber", source = "employee.personnelNumber")
    @Mapping(target = "fullName", source = "employee.FIO")
    @Mapping(target = "organizationName", source = "employee.organization.officialName")
    @Mapping(target = "departmentName", source = "employee.department.departmentName")
    DriverSearchDto driverToDriverSearchDto(Driver source);
    
    default String mapFullName(Employee employee){
        if (employee != null) {
            return employee.getLastName() + " " + employee.getFirstName() +
                   (employee.getPatronymic() == null ? "" : (" " + employee.getPatronymic()));
        } return null;
    }
}
