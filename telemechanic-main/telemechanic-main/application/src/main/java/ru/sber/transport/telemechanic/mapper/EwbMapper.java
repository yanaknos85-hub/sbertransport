package ru.sber.transport.telemechanic.mapper;

import org.mapstruct.*;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.RequestDetailsInfo;
import ru.sber.transport.telemechanic.dto.ewb.EwbInfo;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbDetailedDto;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbDto;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.*;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SigningTelemechInfo;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechInformation;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.fourth_title.*;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.ThirdTitleDocument;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.ThirdTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.ThirdTitleInformation;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.VehicleInformation;
import ru.sber.transport.telemechanic.dto.ewb.xjb.DrivingLicense;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;
import ru.sber.transport.telemechanic.helper.EwbHelper;
import ru.sber.transport.telemechanic.messaging.sender.message.EwbClosedMessage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = { LocalDateTime.class, DateTimeFormatter.class, EwbHelper.class, Objects.class, ContactType.class },
        builder = @Builder(disableBuilder = true),
        uses = { CheckMapper.class })
public interface EwbMapper {
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter FULL_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss+00:00");
    DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Mapping(target = "organizationName", source = "organizationName")
    @Mapping(target = "medicSuccess", source = "ewb.status.medicSuccess")
    @Mapping(target = "telemechSuccess", source = "ewb.status.telemechSuccess")
    @Mapping(target = "driverFullName", source = "ewb.driver.employee.FIO")
    EwbSearchResponseDto ewbToEwbSearchResponseDtoWithOrganizationName(Ewb ewb, String organizationName);
    
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "organizationName", source = "organization.officialName")
    @Mapping(target = "creationDate", source = "creationTime")
    @Mapping(target = "regionCode", source = "organization.address.region.code")
    @Mapping(target = "telemechIn.position", source = "telemechIn.position.positionName")
    @Mapping(target = "telemechOut.position", source = "telemechOut.position.positionName")
    @Mapping(target = "msrn", source = "organization.msrn")
    @Mapping(target = "tin", source = "organization.tin")
    @Mapping(target = "transportationType", source = "transportationType")
    @Mapping(target = "transportationSubtype", source = "transportationSubtype")
    @Mapping(target = "communicationType", source = "communicationType")
    @Mapping(target = "driver.lastName", source = "driver.employee.lastName")
    @Mapping(target = "driver.firstName", source = "driver.employee.firstName")
    @Mapping(target = "driver.patronymic", source = "driver.employee.patronymic")
    @Mapping(target = "driver.personnelNumber", source = "driver.employee.personnelNumber")
    @Mapping(target = "driver.mobilePhone", source = "driver.employee.mobilePhone")
    @Mapping(target = "drivingLicense.series", source = "driver.drivingLicense.series")
    @Mapping(target = "drivingLicense.number", source = "driver.drivingLicense.number")
    @Mapping(target = "drivingLicense.issueDate", source = "driver.drivingLicense.issueDate")
    @Mapping(target = "author", expression = "java(employeeToAuthorDto(source))")
    @Mapping(target = "transport", expression = "java(ewbToTransport(source))")
    @Mapping(target = "medic", ignore = true)
    GetEwbDto ewbToGetEwbDto(Ewb source);
    
    @Mapping(target = "brand", source = "transport.brand")
    @Mapping(target = "model", source = "transport.model")
    @Mapping(target = "stateNumber", source = "transport.stateNumber")
    @Mapping(target = "transportType", source = "transport.type")
    @Mapping(target = "odometerOut", source = "odometerOut")
    @Mapping(target = "fuelLitreageOut", source = "fuelLitreageOut")
    @Mapping(target = "fuelTankVolume", source = "transport.fuelTankVolume")
    GetEwbDto.Transport ewbToTransport(Ewb source);
    
    @Mapping(target = "personnelNumber", source = "author.personnelNumber")
    @Mapping(target = "patronymic", source = "author.patronymic")
    @Mapping(target = "mobilePhone", source = "author.mobilePhone")
    @Mapping(target = "lastName", source = "author.lastName")
    @Mapping(target = "firstName", source = "author.firstName")
    @Mapping(target = "attorney", expression = "java(attorneyToAttorneyDto(source.getAttorneyOutId()))")
    GetEwbDto.Author employeeToAuthorDto(Ewb source);
    
    @Mapping(target = "attorneyNumber", source = "number")
    GetEwbDto.Attorney attorneyToAttorneyDto(Attorney source);
    
    @Mapping(target = "callTelemech", ignore = true)
    @Mapping(target = "medicRequestId", source = "medicRequest.id")
    @Mapping(target = "medicStatus", source = "medicRequest.status")
    @Mapping(target = "telemechanic.requestId", source = "request.id")
    @Mapping(target = "telemechanic.status", source = "request.status")
    @Mapping(target = "telemechanic.checks", ignore = true)
    @Mapping(target = "author", source = "driver")
    GetEwbRequestDto ewbToGetEwbRequestDto(Ewb source);
    
    @SuppressWarnings("java:S107")
    default SecondTitleFile secondTitleFormToFile(
            UUID ewbUuid,
            LocalDateTime decisionTime,
            OrganizationMedicalLicense organizationMedicalLicense,
            ru.sber.transport.telemechanic.database.model.Driver driver,
            ru.sber.transport.telemechanic.database.model.DrivingLicense ewbDrivingLicense,
            EwbTitle firstTitle,
            String signature,
            Employee employee
                                                 ) {
        var secondTitleFile = new SecondTitleFile();//fixme use setters and constructor
        secondTitleFile.setVersionProgram("1.0.0")
                       .setVersionForm("5.01");
        var document = new SecondTitleDocument();
        secondTitleFile.setDocument(document);
        document.setKnd("1110381")
                .setInformationDate(LocalDate.now().format(DATE_FORMATTER))
                .setInformationTime(LocalDateTime.now().format(TIME_FORMATTER));
        var firstTitleInformation = new FirstTitleInformation();
        document.setFirstTitleInformation(firstTitleInformation);
        firstTitleInformation.setFileName(firstTitle.getFileName())
                             .setCreationDate(firstTitle.getCreatedAt().toLocalDate().format(DATE_FORMATTER))
                             .setCreationTime(firstTitle.getCreatedAt().format(TIME_FORMATTER))
                             .setSign(signature);
        var secondTitleInformation = new SecondTitleInformation();
        document.setSecondTitleInformation(secondTitleInformation);
        secondTitleInformation.setEwbUuid(ewbUuid.toString())
                              .setMedicineType("2");
        var medicineOrganizationInfo = new MedicineOrganizationInfo();
        secondTitleInformation.setMedicineOrganizationInfo(medicineOrganizationInfo);
        medicineOrganizationInfo.setMedicineOrganizationName(employee.getOrganization().getOfficialName());
        var medicFullName = new FullName();
        medicineOrganizationInfo.setEmployeeFullName(medicFullName);
        medicFullName.setLastName(employee.getLastName())
                     .setFirstName(employee.getFirstName())
                     .setPatronymic(employee.getPatronymic());
        var medicalLicenseInfo = new MedicalLicenseInfo();
        medicineOrganizationInfo.setMedicalLicenseInfo(medicalLicenseInfo);
        medicalLicenseInfo.setSeries(String.valueOf(organizationMedicalLicense.getSeries()))
                          .setNumber(String.valueOf(organizationMedicalLicense.getNumber()))
                          .setIssueDate(organizationMedicalLicense.getIssueDate().format(DATE_FORMATTER))
                          .setExpiryDate(organizationMedicalLicense.getExpiryDate().format(DATE_FORMATTER));
        var telemedicineInfo = new TelemedicineInfo();
        secondTitleInformation.setTelemedicineInfo(telemedicineInfo);
        telemedicineInfo.setDecisionTime(decisionTime.format(FULL_TIME_FORMATTER))
                        .setUtcDifference("0")
                        .setTelemedicineResult("Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен");
        var driverInfo = new DriverInfo();
        telemedicineInfo.setDriverInfo(driverInfo);
        driverInfo.setTin(String.valueOf(driver.getTin()));
        var drivingLicense = new DrivingLicense();
        driverInfo.setDrivingLicense(drivingLicense);
        drivingLicense.setNumber(String.valueOf(ewbDrivingLicense.getNumber()))
                      .setSeries(String.valueOf(ewbDrivingLicense.getSeries()))
                      .setIssueDate(ewbDrivingLicense.getIssueDate().format(DATE_FORMATTER));
        var driverFullName = new FullName();
        driverInfo.setFullName(driverFullName);
        driverFullName.setLastName(driver.getEmployee().getLastName())
                      .setFirstName(driver.getEmployee().getFirstName())
                      .setPatronymic(driver.getEmployee().getPatronymic());
        var signingMedicInfo = new SigningMedicInfo();
        document.setSigningMedicInfo(signingMedicInfo);
        signingMedicInfo.setSignType("1")
                        .setSignConfirmationMethod("1")
                        .setFullName(medicFullName);
        return secondTitleFile;
    }
    
    @Mapping(target = "id", source = "request.id")
    @Mapping(target = "humanReadableId", source = "request.humanReadableId")
    @Mapping(target = "creationTime", source = "request.creationTime")
    @Mapping(target = "requestStatus", source = "request.status")
    @Mapping(target = "ewbPath", constant = "true")
    @Mapping(target = "checks", source = "request.checks")
    @Mapping(target = "ewb.id", source = "id")
    @Mapping(target = "ewb.ewbStatus", source = "status")
    @Mapping(target = "ewb.medicRequestId", source = "medicRequest.id")
    @Mapping(target = "ewb.medicStatus", source = "medicRequest.status")
    @Mapping(target = "author.id", source = "request.author.id")
    @Mapping(target = "author.firstName", source = "request.author.firstName")
    @Mapping(target = "author.lastName", source = "request.author.lastName")
    @Mapping(target = "author.patronymic", source = "request.author.patronymic")
    @Mapping(target = "author.personnelNumber", source = "request.author.personnelNumber")
    RequestDetailsInfo ewbToRequestDetailsInfo(Ewb ewb);
    
    @Mapping(target = "versionProgram", ignore = true)
    @Mapping(target = "versionForm", ignore = true)
    @Mapping(target = "idFile", ignore = true)
    @Mapping(target = "document", expression = "java(ewbInfoToThirdTitleDocument(source, creationTime))")
    ThirdTitleFile ewbInfoToFile(EwbInfo source, LocalDateTime creationTime);
    
    @Mapping(target = "knd", ignore = true)
    @Mapping(target = "informationDate", expression = "java(creationTime.format(DATE_FORMATTER))")
    @Mapping(target = "informationTime", expression = "java(creationTime.format(TIME_FORMATTER))")
    @Mapping(target = "firstTitleInformation", expression = "java(ewbInfoToFirstTitleInformation(source))")
    @Mapping(target = "thirdTitleInformation", expression = "java(ewbInfoToThirdTitleInformation(source))")
    @Mapping(target = "signingTelemechInfo", expression = "java(ewbInfoToSigningTelemechInfo(source))")
    ThirdTitleDocument ewbInfoToThirdTitleDocument(EwbInfo source, LocalDateTime creationTime);
    
    @Mapping(target = "fileName", source = "title.fileName")
    @Mapping(target = "creationDate", expression = "java(source.title().getCreatedAt().format(DATE_FORMATTER))")
    @Mapping(target = "creationTime", expression = "java(source.title().getCreatedAt().format(TIME_FORMATTER))")
    @Mapping(target = "sign", source = "signature")
    FirstTitleInformation ewbInfoToFirstTitleInformation(EwbInfo source);
    
    @Mapping(target = "telemechTimeUtc", ignore = true)
    @Mapping(target = "telemechSuccess", ignore = true)
    @Mapping(target = "telemechDecisionOutTimeUtc", ignore = true)
    @Mapping(target = "ewbUuid", source = "ewb.ewbUuid")
    @Mapping(target = "telemechTime", expression = "java(source.ewb().getRequest().getCreationTime().format(FULL_TIME_FORMATTER))")
    @Mapping(target = "telemechDecisionOutTime", expression = "java(source.ewb().getTelemechDecisionOut().format(FULL_TIME_FORMATTER))")
    @Mapping(target = "telemechInformation", expression = "java(ewbInfoToTelemechInformation(source))")
    @Mapping(target = "vehicleInformation", expression = "java(ewbInfoToVehicleInformation(source))")
    ThirdTitleInformation ewbInfoToThirdTitleInformation(EwbInfo source);
    
    @Mapping(target = "fullName", source = "ewb.telemechOut")
    TelemechInformation ewbInfoToTelemechInformation(EwbInfo source);
    
    @Mapping(target = "vehicle.type", source = "ewb.transport.type")
    @Mapping(target = "vehicle.brand", source = "ewb.transport.brand")
    @Mapping(target = "vehicle.model", source = "ewb.transport.model")
    @Mapping(target = "vehicle.stateNumber", source = "ewb.transport.stateNumber")
    VehicleInformation ewbInfoToVehicleInformation(EwbInfo source);
    
    @Mapping(target = "signType", constant = "1")
    @Mapping(target = "signConfirmationMethod", constant = "1")
    @Mapping(target = "fullName", source = "ewb.telemechOut")
    SigningTelemechInfo ewbInfoToSigningTelemechInfo(EwbInfo source);
    
    @Mapping(target = "idFile", ignore = true)
    @Mapping(target = "document", expression = "java(ewbInfoToFourthTitleDocument(source))")
    FourthTitleFile ewbInfoToFourthTitleFile(EwbInfo source);
    
    @Mapping(target = "informationDate", expression = "java(LocalDate.now().format(DATE_FORMATTER))")
    @Mapping(target = "informationTime", expression = "java(LocalDateTime.now().format(TIME_FORMATTER))")
    @Mapping(target = "thirdTitleInformation", expression = "java(ewbInfoToIdentificationThirdTitle(source))")
    @Mapping(target = "fourthTitleInformation", expression = "java(ewbInfoToFourthTitleInformation(source))")
    @Mapping(target = "signingTelemechInfo", expression = "java(ewbInfoToSigningTelemechInfo(source))")
    FourthTitleDocument ewbInfoToFourthTitleDocument(EwbInfo source);
    
    @Mapping(target = "fileName", source = "title.fileName")
    @Mapping(target = "creationDate", expression = "java(source.title().getCreatedAt().format(DATE_FORMATTER))")
    @Mapping(target = "creationTime", expression = "java(source.title().getCreatedAt().format(TIME_FORMATTER))")
    @Mapping(target = "sign", source = "signature")
    IdentificationThirdTitle ewbInfoToIdentificationThirdTitle(EwbInfo source);
    
    @Mapping(target = "ewbUuid", source = "ewb.ewbUuid")
    @Mapping(target = "odometerOutInformation", expression = "java(ewbInfoToodometerOutInformation(source))")
    @Mapping(target = "telemechInformation", expression = "java(ewbInfoToTelemechInformation(source))")
    FourthTitleInformation ewbInfoToFourthTitleInformation(EwbInfo source);
    
    @Mapping(target = "startRouteDateTime", expression = "java(LocalDateTime.now().format(FULL_TIME_FORMATTER))")
    @Mapping(target = "odometerValue", source = "ewb.odometerOut")
    OdometerOutInformation ewbInfoToodometerOutInformation(EwbInfo source);
    
    @Mapping(target = "creationDate", expression = "java(ewb.getCreationTime().toLocalDate())")
    @Mapping(target = "dispatcherFullName", source = "ewb.author.FIO")
    @Mapping(target = "status", expression = "java(ewb.getStatus().getRusName())")
    @Mapping(target = "driver", expression = "java(ewbToGetEwbDetailedDtoDriver(driver))")
    @Mapping(target = "drivingLicense", source = "driver.drivingLicense")
    @Mapping(target = "medic", expression = "java(ewbToGetEwbDetailedDtoMedic(ewb))")
    @Mapping(target = "transportationType", constant = "Перевозки для собственных нужд")
    @Mapping(target = "communicationType", constant = "Городское")
    @Mapping(target = "telemechOut", expression = "java(ewbToGetEwbDetailedDtoTelemechOut(ewb))")
    GetEwbDetailedDto ewbToGetEwbDetailedDto(Ewb ewb, Driver driver, String organizationName, String tin, String msrn, String phoneNumber);
    
    @Mapping(target = "firstName", source = "source.employee.firstName")
    @Mapping(target = "lastName", source = "source.employee.lastName")
    @Mapping(target = "patronymic", source = "source.employee.patronymic")
    @Mapping(target = "mobilePhone", source = "source.employee.mobilePhone")
    GetEwbDetailedDto.Driver ewbToGetEwbDetailedDtoDriver(Driver source);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ewbId", source = "ewb.id")
    @Mapping(target = "oldStatus", source = "ewb.status")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "changeTime", expression = "java(LocalDateTime.now())")
    EwbHistory ewbToEwbHistory(Ewb ewb, EwbStatus status, String comment, Employee initiator);
    
    default GetEwbDetailedDto.Medic ewbToGetEwbDetailedDtoMedic(Ewb source) {
        String medicFullName = null;
        if (source.getMedic() != null) {
            medicFullName = source.getMedic().getFIO();
        } else if (source.getMedicContractor() != null) {
            medicFullName = source.getMedicContractor().getFullName();
        }
        return new GetEwbDetailedDto.Medic(
                source.getMedicRequest().getStatus().equals(TelemedicineStatus.DONE) ?
                "К работе на линии допущен" : source.getMedicRequest().getStatus().getDescription(),
                medicFullName,
                source.getMedicDecisionTime()
        );
    }
    
    default GetEwbDetailedDto.TelemechOut ewbToGetEwbDetailedDtoTelemechOut(Ewb source) {
        return new GetEwbDetailedDto.TelemechOut(
                source.getStatus().equals(EwbStatus.ON_THE_LINE) ?
                "Пройден" : source.getMedicRequest().getStatus().getDescription(),
                source.getTelemechOut().getFIO(),
                source.getTelemechDecisionOut(),
                source.getOdometerOut()
        );
    }

    @Mapping(target = "id", source = "source.id")
    @Mapping(target = "transportId", source = "source.transport.id")
    @Mapping(target = "organizationId", source = "source.organization.id")
    @Mapping(target = "fuelLitreageOut", source = "source.fuelLitreageOut")
    @Mapping(target = "fuelLitreageIn", source = "source.fuelLitreageIn")
    @Mapping(target = "ewbStartDate", source = "source.startDate")
    @Mapping(target = "ewbFinishDate", source = "source.finishDate")
    @Mapping(target = "driverEmployeeId", source = "source.driver.employee.id")
    EwbClosedMessage ewbToEwbClosedMessage(Ewb source);
}