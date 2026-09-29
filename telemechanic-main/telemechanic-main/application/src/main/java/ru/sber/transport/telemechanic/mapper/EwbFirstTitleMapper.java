package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.database.model.Attorney;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.dispatcher.GetOrganizationDispatcherResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.xjb.*;
import ru.sber.transport.telemechanic.dto.ewb.xjb.Contact;
import ru.sber.transport.telemechanic.dto.ewb.xjb.DrivingLicense;
import ru.sber.transport.telemechanic.helper.EwbHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(
        componentModel = "spring",
        imports = { DateTimeFormatter.class, EwbHelper.class, ZoneId.class, ZoneOffset.class }
)
public interface EwbFirstTitleMapper {
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Mapping(target = "idFile", source = "fileName")
    @Mapping(target = "versionProgram", constant = "1.0.0")
    @Mapping(target = "versionForm", constant = "5.01")
    @Mapping(target = "document", expression = "java(toDocument(humanReadableId, request, dispatcherOrganization, driver, transport, " +
                                               "organizationAddress, dispatcher, creationTime))")
    File firstTitleRequestToFile(
            String humanReadableId,
            String fileName,
            FirstTitleRequest request,
            GetOrganizationDispatcherResponse dispatcherOrganization,
            Driver driver,
            Transport transport,
            OrganizationAddress organizationAddress,
            Dispatcher dispatcher,
            LocalDateTime creationTime
                                );
    
    @Mapping(target = "knd", constant = "1110380")
    @Mapping(target = "informationDate", expression = "java(creationTime.format(DATE_FORMATTER))")
    @Mapping(target = "informationTime", expression = "java(creationTime.format(TIME_FORMATTER))")
    @Mapping(target = "ewbNumber", source = "humanReadableId")
    @Mapping(target = "startTime", expression = "java(request.startDate().format(DATE_FORMATTER))")
    @Mapping(target = "beginRoute", constant = "1")
    @Mapping(target = "route", expression = "java(toRoute(request, dispatcherOrganization, driver, transport, organizationAddress))")
    @Mapping(target = "signingPersonInfo", expression = "java(toSigningPersonInfo(dispatcher))")
    Document toDocument(
            String humanReadableId,
            FirstTitleRequest request,
            GetOrganizationDispatcherResponse dispatcherOrganization,
            Driver driver,
            Transport transport,
            OrganizationAddress organizationAddress,
            Dispatcher dispatcher,
            LocalDateTime creationTime
                       );
    
    @Mapping(target = "ewbUuid", source = "request.ewbUuid")
    @Mapping(target = "medicalExamination", constant = "2")
    @Mapping(target = "transportationType", source = "request.transportationType")
    @Mapping(target = "communicationType", source = "request.communicationType")
    @Mapping(target = "period", expression = "java(toPeriod(request))")
    @Mapping(target = "owner", expression = "java(toOwner(dispatcherOrganization, organizationAddress))")
    @Mapping(target = "transport", expression = "java(toTransportEwb(transport))")
    @Mapping(target = "driver", expression = "java(toDriver(driver))")
    Route toRoute(
            FirstTitleRequest request,
            GetOrganizationDispatcherResponse dispatcherOrganization,
            Driver driver,
            Transport transport,
            OrganizationAddress organizationAddress
                 );
    
    @Mapping(target = "ewbForADay", expression = "java(EwbHelper.calculateEwbForADay(request.startDate(), request.finishDate()))")
    @Mapping(target = "ewbDateExecution",
             expression = "java(EwbHelper.calculateEwbDateExecution(request.startDate(), request.finishDate()))")
    @Mapping(target = "startTime", expression = "java(EwbHelper.calculateEwbStartDate(request.startDate(), request.finishDate()))")
    @Mapping(target = "endTime", expression = "java(EwbHelper.calculateEwbFinishDate(request.startDate(), request.finishDate()))")
    Period toPeriod(
            FirstTitleRequest request
                   );
    
    @Mapping(target = "ownerName", constant = "С")
    @Mapping(target = "ownerDetails", expression = "java(toOwnerDetails(dispatcherOrganization))")
    @Mapping(target = "address", expression = "java(toAddress(organizationAddress))")
    @Mapping(target = "contact", expression = "java(toContact(dispatcherOrganization))")
    Owner toOwner(
            GetOrganizationDispatcherResponse dispatcherOrganization,
            OrganizationAddress organizationAddress
                 );
    
    @Mapping(target = "organizationInfo", expression = "java(toOrganizationInfo(dispatcherOrganization))")
    OwnerDetails toOwnerDetails(GetOrganizationDispatcherResponse dispatcherOrganization);
    
    @Mapping(target = "organizationName", source = "dispatcherOrganization.organization.name")
    @Mapping(target = "msrn", source = "dispatcherOrganization.organization.msrn")
    @Mapping(target = "tin", source = "dispatcherOrganization.organization.tin")
    OrganizationInfo toOrganizationInfo(GetOrganizationDispatcherResponse dispatcherOrganization);
    
    @Mapping(target = "addressRf", expression = "java(toAddressRf(organizationAddress))")
    Address toAddress(
            OrganizationAddress organizationAddress
                     );
    
    @Mapping(target = "index", source = "organizationAddress.zip")
    @Mapping(target = "regionCode", source = "organizationAddress.region.code")
    @Mapping(target = "street", ignore = true)
    @Mapping(target = "house", ignore = true)
    @Mapping(target = "building", ignore = true)
    @Mapping(target = "flat", ignore = true)
    AddressRf toAddressRf(
            OrganizationAddress organizationAddress
                         );
    
    @Mapping(target = "phone", source = "dispatcherOrganization.organization.phone")
    Contact toContact(GetOrganizationDispatcherResponse dispatcherOrganization);
    
    @Mapping(target = "transport", expression = "java(toTransport(transport))")
    TransportEWB toTransportEwb(Transport transport);
    
    @Mapping(target = "transportType", source = "transport.type")
    @Mapping(target = "brand", source = "transport.brand")
    @Mapping(target = "model", source = "transport.model")
    @Mapping(target = "stateNumber", source = "transport.stateNumber")
    ru.sber.transport.telemechanic.dto.ewb.xjb.Transport toTransport(Transport transport);
    
    @Mapping(target = "tin", source = "driver.tin")
    @Mapping(target = "drivingLicense", expression = "java(toDrivingLicense(driver))")
    @Mapping(target = "fullName", expression = "java(toFullName(driver.getEmployee()))")
    ru.sber.transport.telemechanic.dto.ewb.xjb.Driver toDriver(Driver driver);
    
    @Mapping(target = "number", source = "driver.drivingLicense.number")
    @Mapping(target = "series", source = "driver.drivingLicense.series")
    @Mapping(target = "issueDate", expression = "java(driver.getDrivingLicense().getIssueDate().format(DATE_FORMATTER))")
    DrivingLicense toDrivingLicense(Driver driver);
    
    @Mapping(target = "lastName", source = "employee.lastName")
    @Mapping(target = "firstName", source = "employee.firstName")
    @Mapping(target = "patronymic", source = "employee.patronymic")
    FullName toFullName(Employee employee);
    
    @Mapping(target = "signType", constant = "1")
    @Mapping(target = "accessType", constant = "3")
    @Mapping(target = "fullName", expression = "java(toFullName(dispatcher.getEmployee()))")
    @Mapping(target = "attorney", expression = "java(toAttorney(dispatcher.getAttorney()))")
    SigningPersonInfo toSigningPersonInfo(Dispatcher dispatcher);
    
    @Mapping(target = "id", source = "attorney.number")
    @Mapping(target = "issueDate", expression = "java(attorney.getIssueDate().format(DATE_FORMATTER))")
    @Mapping(target = "creationSystem", source = "attorney.creationSystem")
    ru.sber.transport.telemechanic.dto.ewb.xjb.Attorney toAttorney(Attorney attorney);
}
