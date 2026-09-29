package ru.sber.transport.telemechanic.mapper;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.CheckDto;
import ru.sber.transport.telemechanic.dto.ewb.EwbInfo;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbDetailedDto;
import ru.sber.transport.telemechanic.dto.ewb.GetEwbDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.*;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title.ThirdTitleFile;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;

class EwbMapperTest {
    
    private final CheckMapper checkMapper = new CheckMapperImpl();
    private final EwbMapper mapper = new EwbMapperImpl(checkMapper);
    
    @Test
    void ewbToEwbSearchResponseDtoWithOrganizationName() {
        var ewb = Instancio.of(Ewb.class).create();
        var explectedName = Instancio.of(String.class).create();
        var actual = mapper.ewbToEwbSearchResponseDtoWithOrganizationName(ewb, explectedName);
        assertNotNull(actual);
        assertEquals(ewb.getId(), actual.id());
        assertEquals(ewb.getHumanReadableId(), actual.humanReadableId());
        assertEquals(ewb.getStartDate(), actual.startDate());
        assertEquals(ewb.getFinishDate(), actual.finishDate());
        assertEquals(explectedName, actual.organizationName());
        assertEquals(ewb.getStatus(), actual.status());
        assertEquals(ewb.getStatus().isMedicSuccess(), actual.medicSuccess());
        assertEquals(ewb.getStatus().isTelemechSuccess(), actual.telemechSuccess());
        assertEquals(ewb.getTransport().getBrand(), actual.transport().brand());
        assertEquals(ewb.getTransport().getModel(), actual.transport().model());
        assertEquals(ewb.getTransport().getStateNumber(), actual.transport().stateNumber());
        assertEquals(ewb.getDriver().getEmployee().getFIO(), actual.driverFullName());
    }
    
    @Test
    void ewbToGetEwbDto() {
        var ewb = Instancio.create(Ewb.class);
        var actual = mapper.ewbToGetEwbDto(ewb);
        assertEquals(ewb.getId(), actual.getId());
        assertEquals(ewb.getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(ewb.getStartDate(), actual.getStartDate());
        assertEquals(ewb.getFinishDate(), actual.getFinishDate());
        assertEquals(ewb.getOrganization().getAddress().getRegion().getCode(), actual.getRegionCode());
        assertEquals(ewb.getOrganization().getMsrn(), actual.getMsrn());
        assertEquals(ewb.getOrganization().getTin(), actual.getTin());
        assertEquals(ewb.getOrganization().getOfficialName(), actual.getOrganizationName());
        assertEquals(ewb.getStatus(), actual.getStatus());
        assertTransport(ewb.getTransport(), actual.getTransport());
        assertDriver(ewb.getDriver().getEmployee(), actual.getDriver());
        assertDriverLicense(ewb.getDriver().getDrivingLicense(), actual.getDrivingLicense());
        assertAuthor(ewb, actual.getAuthor());
        assertThat(actual.getMedic()).isNull();
        assertTelemech(ewb.getTelemechOut(), actual.getTelemechOut());
        assertTelemech(ewb.getTelemechIn(), actual.getTelemechIn());
        assertThat(actual.getTransport())
                .extracting(
                        GetEwbDto.Transport::getOdometerOut,
                        GetEwbDto.Transport::getFuelLitreageOut
                           )
                .containsExactlyInAnyOrder(
                        ewb.getOdometerOut(),
                        ewb.getFuelLitreageOut()
                                          );
    }
    
    private void assertTransport(Transport expected, GetEwbDto.Transport actual) {
        assertThat(actual)
                .extracting(
                        GetEwbDto.Transport::getBrand,
                        GetEwbDto.Transport::getModel,
                        GetEwbDto.Transport::getStateNumber,
                        GetEwbDto.Transport::getTransportType,
                        GetEwbDto.Transport::getFuelTankVolume
                           )
                .containsExactlyInAnyOrder(
                        expected.getBrand(),
                        expected.getModel(),
                        expected.getStateNumber(),
                        expected.getType(),
                        expected.getFuelTankVolume()
                                          );
    }
    
    private void assertDriver(Employee expected, GetEwbDto.Driver actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getPatronymic(), actual.getPatronymic());
        assertEquals(expected.getPersonnelNumber(), actual.getPersonnelNumber());
        assertEquals(expected.getMobilePhone(), actual.getMobilePhone());
    }
    
    private void assertDriverLicense(DrivingLicense expected, GetEwbDto.DrivingLicense actual) {
        assertEquals(expected.getSeries(), actual.getSeries());
        assertEquals(expected.getNumber(), actual.getNumber());
        assertEquals(expected.getIssueDate(), actual.getIssueDate());
    }
    
    private void assertAuthor(Ewb ewb, GetEwbDto.Author actual) {
        var expectedAuthor = ewb.getAuthor();
        assertEquals(expectedAuthor.getFirstName(), actual.getFirstName());
        assertEquals(expectedAuthor.getLastName(), actual.getLastName());
        assertEquals(expectedAuthor.getPatronymic(), actual.getPatronymic());
        assertEquals(expectedAuthor.getPersonnelNumber(), actual.getPersonnelNumber());
        assertAttorney(ewb.getAttorneyOutId(), actual.getAttorney());
    }
    
    private void assertAttorney(Attorney expected, GetEwbDto.Attorney actual) {
        assertEquals(expected.getNumber(), actual.getAttorneyNumber());
        assertEquals(expected.getIssueDate(), actual.getIssueDate());
        assertEquals(expected.getCreationSystem(), actual.getCreationSystem());
    }
    
    private void assertTelemech(Employee expected, GetEwbDto.Telemech actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getPatronymic(), actual.getPatronymic());
        assertEquals(expected.getPosition().getPositionName(), actual.getPosition());
        assertFalse(actual.isTelemechSuccess());
        assertNull(actual.getDecisionTime());
        assertEquals(0, actual.getMileage());
    }
    
    @Test
    void secondTitleForm() {
        var dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        var timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        var fullTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy'T'HH:mm:ss+00:00");
        var ewbUuid = UUID.randomUUID();
        var decisionTime = LocalDateTime.now();
        var organizationMedicalLicense = Instancio.create(OrganizationMedicalLicense.class);
        var driver = Instancio.create(Driver.class);
        var drivingLicense = Instancio.create(DrivingLicense.class);
        var firstTitle = Instancio.create(EwbTitle.class);
        var signature = UUID.randomUUID().toString();
        var employee = Instancio.create(Employee.class);
        var medicFullName = new FullName()
                .setFirstName(employee.getFirstName())
                .setLastName(employee.getLastName())
                .setPatronymic(employee.getPatronymic());
        var expected = new SecondTitleFile(
                null,
                "1.0.0",
                "5.01",
                new SecondTitleDocument("1110381",
                                        LocalDate.now().format(dateTimeFormatter),
                                        LocalDateTime.now().format(timeFormatter),
                                        new FirstTitleInformation(
                                                firstTitle.getFileName(),
                                                firstTitle.getCreatedAt().toLocalDate().format(dateTimeFormatter),
                                                firstTitle.getCreatedAt().format(timeFormatter),
                                                signature
                                        ),
                                        new SecondTitleInformation(
                                                ewbUuid.toString(),
                                                "2",
                                                new MedicineOrganizationInfo(
                                                        employee.getOrganization().getOfficialName(),
                                                        medicFullName,
                                                        new MedicalLicenseInfo(organizationMedicalLicense.getSeries(),
                                                                               organizationMedicalLicense.getNumber(),
                                                                               organizationMedicalLicense.getIssueDate().format(dateTimeFormatter),
                                                                               organizationMedicalLicense.getExpiryDate().format(dateTimeFormatter))),
                                                new TelemedicineInfo(decisionTime.format(fullTimeFormatter),
                                                                     "0",
                                                                     "Прошел предсменный медицинский осмотр, к исполнению трудовых обязанностей допущен",
                                                                     new DriverInfo(driver.getTin(),
                                                                                    new ru.sber.transport.telemechanic.dto.ewb.xjb.DrivingLicense()
                                                                                            .setNumber(String.valueOf(drivingLicense.getNumber()))
                                                                                            .setSeries(drivingLicense.getSeries())
                                                                                            .setIssueDate(drivingLicense.getIssueDate()
                                                                                                                        .format(dateTimeFormatter)),
                                                                                    new FullName()
                                                                                            .setFirstName(driver.getEmployee().getFirstName())
                                                                                            .setLastName(driver.getEmployee().getLastName())
                                                                                            .setPatronymic(driver.getEmployee().getPatronymic())
                                                                     ))
                                        ),
                                        new SigningMedicInfo("1",
                                                             "1",
                                                             medicFullName
                                        )
                )
        );
        var actual = mapper.secondTitleFormToFile(ewbUuid,
                                                  decisionTime,
                                                  organizationMedicalLicense,
                                                  driver,
                                                  drivingLicense,
                                                  firstTitle,
                                                  signature,
                                                  employee);
        assertThat(actual)
                .usingRecursiveComparison(RecursiveComparisonConfiguration
                                                  .builder()
                                                  .withComparatorForType(
                                                          Comparator.comparing((LocalDateTime o) -> o.truncatedTo(
                                                                  ChronoUnit.SECONDS)),
                                                          LocalDateTime.class)
                                                  .build())
                .isEqualTo(expected);
    }
    
    @Test
    void ewbToAddOdometerValueResponse() {
        var ewb = Instancio.of(Ewb.class).create();
        var actual = mapper.ewbToRequestDetailsInfo(ewb);
        
        assertEquals(ewb.getRequest().getId(), actual.getId());
        assertEquals(ewb.getRequest().getHumanReadableId(), actual.getHumanReadableId());
        assertEquals(ewb.getRequest().getCreationTime(), actual.getCreationTime());
        assertEquals(ewb.getRequest().getStatus(), actual.getRequestStatus());
        assertEquals(ewb.getOdometerOut(), actual.getOdometerOut());
        assertTrue(actual.isEwbPath());
        
        assertEquals(ewb.getRequest().getChecks().size(), actual.getChecks().size());
        assertEquals(ewb.getRequest().getChecks().stream().map(Check::getCheckType).toList(),
                     actual.getChecks().stream().map(CheckDto::checkType).toList());
        
        assertEquals(ewb.getId(), actual.getEwb().getId());
        assertEquals(ewb.getStatus(), actual.getEwb().getEwbStatus());
        assertEquals(ewb.getMedicRequest().getId(), actual.getEwb().getMedicRequestId());
        assertEquals(ewb.getMedicRequest().getStatus(), actual.getEwb().getMedicStatus());
        
        assertEquals(ewb.getRequest().getAuthor().getId(), actual.getAuthor().getId());
        assertEquals(ewb.getRequest().getAuthor().getFirstName(), actual.getAuthor().getFirstName());
        assertEquals(ewb.getRequest().getAuthor().getLastName(), actual.getAuthor().getLastName());
        assertEquals(ewb.getRequest().getAuthor().getPatronymic(), actual.getAuthor().getPatronymic());
        assertEquals(ewb.getRequest().getAuthor().getPersonnelNumber(), actual.getAuthor().getPersonnelNumber());
        
        assertEquals(ewb.getTransport().getId(), actual.getTransport().getId());
        assertEquals(ewb.getTransport().getStateNumber(), actual.getTransport().getStateNumber());
        assertEquals(ewb.getTransport().getBrand(), actual.getTransport().getBrand());
        assertEquals(ewb.getTransport().getModel(), actual.getTransport().getModel());
        assertEquals(ewb.getTransport().getMileage(), actual.getTransport().getMileage());
    }
    
    @Test
    void ewbToGetEwbRequestDto() {
        var ewb = Instancio.of(Ewb.class).create();
        var actual = mapper.ewbToGetEwbRequestDto(ewb);
        
        assertEquals(ewb.getId(), actual.id());
        assertEquals(ewb.getStatus(), actual.status());
        assertEquals(ewb.getMedicRequest().getId(), actual.medicRequestId());
        assertEquals(ewb.getMedicRequest().getStatus(), actual.medicStatus());
        assertEquals(ewb.getHumanReadableId(), actual.humanReadableId());
        assertEquals(ewb.getCreationTime(), actual.creationTime());
        
        assertEquals(ewb.getTransport().getId(), actual.transport().id());
        assertEquals(ewb.getTransport().getStateNumber(), actual.transport().stateNumber());
        assertEquals(ewb.getTransport().getBrand(), actual.transport().brand());
        assertEquals(ewb.getTransport().getModel(), actual.transport().model());
        assertEquals(ewb.getTransport().getMileage(), actual.transport().mileage());
        assertEquals(ewb.getTransport().getFuelTankVolume(), actual.transport().fuelTankVolume());
        assertEquals(ewb.getTransport().getFuelLitreage(), actual.transport().fuelLitreage());
        assertEquals(ewb.getRequest().getId(), actual.telemechanic().requestId());
        assertEquals(ewb.getRequest().getStatus(), actual.telemechanic().status());
    }
    
    @Test
    void thirdTitleToFileTest() {
        var ewb = Instancio.of(Ewb.class).create();
        var firstTitle = Instancio.of(EwbTitle.class).create();
        var signature = Instancio.of(String.class).create();
        
        var ewbInfo = new EwbInfo(ewb, firstTitle, signature);
        
        var fileCreationTime = LocalDateTime.now();
        ThirdTitleFile actual = mapper.ewbInfoToFile(ewbInfo, fileCreationTime);
        
        assertEquals("1.0.0", actual.getVersionProgram());
        assertEquals("5.01", actual.getVersionForm());
        assertEquals("1110382", actual.getDocument().getKnd());
        assertEquals(fileCreationTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), actual.getDocument().getInformationDate());
        assertEquals(fileCreationTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")), actual.getDocument().getInformationTime());
        assertEquals(firstTitle.getFileName(), actual.getDocument().getFirstTitleInformation().getFileName());
        assertEquals(firstTitle.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                     actual.getDocument().getFirstTitleInformation().getCreationDate());
        assertEquals(firstTitle.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                     actual.getDocument().getFirstTitleInformation().getCreationTime());
        assertEquals(signature, actual.getDocument().getFirstTitleInformation().getSign());
        assertThirdTitleInfo(ewb, actual);
        assertEquals("1", actual.getDocument().getSigningTelemechInfo().getSignType());
        assertEquals("1", actual.getDocument().getSigningTelemechInfo().getSignConfirmationMethod());
        assertEquals(ewb.getTelemechOut().getLastName(), actual.getDocument().getSigningTelemechInfo().getFullName().getLastName());
        assertEquals(ewb.getTelemechOut().getFirstName(), actual.getDocument().getSigningTelemechInfo().getFullName().getFirstName());
        assertEquals(ewb.getTelemechOut().getPatronymic(), actual.getDocument().getSigningTelemechInfo().getFullName().getPatronymic());
    }
    
    @Test
    void fourthTitleToFileTest() {
        var ewb = Instancio.of(Ewb.class).create();
        var prevTitle = Instancio.of(EwbTitle.class).create();
        var signature = Instancio.of(String.class).create();
        
        var ewbInfo = new EwbInfo(ewb, prevTitle, signature);
        
        var actual = mapper.ewbInfoToFourthTitleFile(ewbInfo);
        var thirdTitleCreationDate = actual.getDocument().getThirdTitleInformation().getCreationDate();
        var thirdTitleCreationTime = actual.getDocument().getThirdTitleInformation().getCreationTime();
        
        assertThat(actual.getVersionProgram()).isEqualTo("1.0.0");
        assertThat(actual.getVersionForm()).isEqualTo("5.01");
        assertThat(actual.getDocument().getKnd()).isEqualTo("1110383");
        
        assertThat(thirdTitleCreationDate).isEqualTo(prevTitle.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        assertThat(thirdTitleCreationTime).isEqualTo(prevTitle.getCreatedAt().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        assertThat(actual.getDocument().getThirdTitleInformation().getFileName()).isEqualTo(prevTitle.getFileName());
        assertThat(actual.getDocument().getThirdTitleInformation().getSign()).isEqualTo(signature);
        
        assertThat(actual.getDocument().getSigningTelemechInfo().getSignType()).isEqualTo("1");
        assertThat(actual.getDocument().getSigningTelemechInfo().getSignConfirmationMethod()).isEqualTo("1");
        assertThat(actual.getDocument().getSigningTelemechInfo().getFullName().getLastName()).isEqualTo(ewb.getTelemechOut().getLastName());
        assertThat(actual.getDocument().getSigningTelemechInfo().getFullName().getFirstName()).isEqualTo(ewb.getTelemechOut().getFirstName());
        assertThat(actual.getDocument().getSigningTelemechInfo().getFullName().getPatronymic()).isEqualTo(ewb.getTelemechOut().getPatronymic());
        
        assertThat(actual.getDocument().getFourthTitleInformation().getEwbUuid()).isEqualTo(ewb.getEwbUuid().toString());
        assertThat(actual.getDocument().getFourthTitleInformation().getIsRouteStart()).isEqualTo("1");
        assertThat(actual.getDocument().getFourthTitleInformation().getOdometerOutInformation().getOdometerValue())
                .isEqualTo(ewb.getOdometerOut().toString());
        assertThat(actual.getDocument().getFourthTitleInformation().getTelemechInformation().getFullName().getFirstName())
                .isEqualTo(ewb.getTelemechOut().getFirstName());
        assertThat(actual.getDocument().getFourthTitleInformation().getTelemechInformation().getFullName().getLastName())
                .isEqualTo(ewb.getTelemechOut().getLastName());
    }
    
    @Test
    void ewbToGetEwbDetailedDto() {
        var ewb = Instancio.create(Ewb.class);
        var driver = Instancio.create(Driver.class);
        var organizationName = Instancio.create(String.class);
        var tin = Instancio.create(String.class);
        var msrn = Instancio.create(String.class);
        var phoneNumber = Instancio.create(String.class);
        
        var actual = mapper.ewbToGetEwbDetailedDto(ewb, driver, organizationName, tin, msrn, phoneNumber);
        
        assertEquals(ewb.getCreationTime().toLocalDate(), actual.creationDate());
        assertEquals(ewb.getAuthor().getFIO(), actual.dispatcherFullName());
        assertEquals(ewb.getStatus().getRusName(), actual.status());
        assertEquals(mapper.ewbToGetEwbDetailedDtoDriver(driver), actual.driver());
        assertEquals(driver.getDrivingLicense().getNumber(), actual.drivingLicense().number());
        assertEquals(driver.getDrivingLicense().getSeries(), actual.drivingLicense().series());
        assertEquals(mapper.ewbToGetEwbDetailedDtoMedic(ewb), actual.medic());
        assertEquals("Перевозки для собственных нужд", actual.transportationType());
        assertEquals("Городское", actual.communicationType());
        assertEquals(mapper.ewbToGetEwbDetailedDtoTelemechOut(ewb), actual.telemechOut());
    }
    
    static Stream<Arguments> ewbToGetEwbDetailedDtoMedic() {
        return Stream.of(
                Arguments.of(
                        "Медик организации",
                        Instancio.of(Ewb.class)
                                 .set(field(Ewb::getMedic), Instancio.of(Employee.class)
                                                                     .set(field(Employee::getLastName), "F")
                                                                     .set(field(Employee::getFirstName), "A")
                                                                     .set(field(Employee::getPatronymic), "V")
                                                                     .create())
                                 .set(field(Ewb::getMedicContractor), null)
                                 .set(field(Ewb::getMedicRequest), Instancio.of(MedicRequest.class)
                                                                            .set(field(MedicRequest::getStatus),
                                                                                 TelemedicineStatus.DONE)
                                                                            .create())
                                 .create(),
                        "F A V"
                            ),
                Arguments.of(
                        "Медик контрагента",
                        Instancio.of(Ewb.class)
                                 .set(field(Ewb::getMedic), null)
                                 .set(field(Ewb::getMedicContractor), Instancio.of(MedicContractor.class)
                                                                               .set(field(MedicContractor::getFullName), "F A V")
                                                                               .create())
                                 .set(field(Ewb::getMedicRequest), Instancio.of(MedicRequest.class)
                                                                            .set(field(MedicRequest::getStatus),
                                                                                 TelemedicineStatus.IN_PROGRESS)
                                                                            .create())
                                 .create(),
                        "F A V"
                            ),
                Arguments.of(
                        "null оба медика",
                        Instancio.of(Ewb.class)
                                 .set(field(Ewb::getMedic), null)
                                 .set(field(Ewb::getMedicContractor), null)
                                 .create(),
                        null
                            )
                        );
    }
    
    @ParameterizedTest(name = "{index} {0}")
    @MethodSource
    void ewbToGetEwbDetailedDtoMedic(String testName, Ewb ewb, String medicName) {
        var medicRequestStatus = ewb.getMedicRequest().getStatus();
        var actual = mapper.ewbToGetEwbDetailedDtoMedic(ewb);
        assertThat(actual)
                .extracting(
                        GetEwbDetailedDto.Medic::status,
                        GetEwbDetailedDto.Medic::fullName,
                        GetEwbDetailedDto.Medic::decisionTime
                           )
                .containsExactly(
                        medicRequestStatus.equals(TelemedicineStatus.DONE) ? "К работе на линии допущен" : medicRequestStatus.getDescription(),
                        medicName,
                        ewb.getMedicDecisionTime()
                                );
    }
    
    @Test
    void ewbToEwbHistory() {
        var ewb = Instancio.create(Ewb.class);
        var initiator = Instancio.create(Employee.class);
        
        var actual = mapper.ewbToEwbHistory(ewb, EwbStatus.EXPIRED, "comment", initiator);
        
        assertThat(actual).isNotNull();
        assertThat(actual.getEwbId()).isEqualTo(ewb.getId());
        assertThat(actual.getStatus()).isEqualTo(EwbStatus.EXPIRED);
        assertThat(actual.getOldStatus()).isEqualTo(ewb.getStatus());
        assertThat(actual.getComment()).isEqualTo("comment");
        assertThat(actual.getInitiator().getId()).isEqualTo(initiator.getId());
    }

    @Test
    void ewbToEwbClosedMessage() {
        var ewb = Instancio.create(Ewb.class);
        var actual = mapper.ewbToEwbClosedMessage(ewb);
        assertThat(actual).isNotNull();
        assertThat(actual.id()).isEqualTo(ewb.getId());
        assertThat(actual.humanReadableId()).isEqualTo(ewb.getHumanReadableId());
        assertThat(actual.ewbStartDate()).isEqualTo(ewb.getStartDate());
        assertThat(actual.ewbFinishDate()).isEqualTo(ewb.getFinishDate());
        assertThat(actual.organizationId()).isEqualTo(ewb.getOrganization().getId());
        assertThat(actual.transportId()).isEqualTo(ewb.getTransport().getId());
        assertThat(actual.driverEmployeeId()).isEqualTo(ewb.getDriver().getEmployee().getId());
        assertThat(actual.odometerOut()).isEqualTo(ewb.getOdometerOut());
        assertThat(actual.odometerIn()).isEqualTo(ewb.getOdometerIn());
        assertThat(actual.fuelLitreageOut()).isEqualTo(ewb.getFuelLitreageOut());
        assertThat(actual.fuelLitreageIn()).isEqualTo(ewb.getFuelLitreageIn());
    }

    private void assertThirdTitleInfo(Ewb ewb, ThirdTitleFile actual) {
        assertEquals(ewb.getEwbUuid().toString(), actual.getDocument().getThirdTitleInformation().getEwbUuid());
        assertEquals(ewb.getRequest().getCreationTime().format(EwbMapper.FULL_TIME_FORMATTER),
                     actual.getDocument().getThirdTitleInformation().getTelemechTime());
        assertEquals("0", actual.getDocument().getThirdTitleInformation().getTelemechTimeUtc());
        assertEquals("1", actual.getDocument().getThirdTitleInformation().getTelemechSuccess());
        assertEquals(ewb.getTelemechDecisionOut().format(EwbMapper.FULL_TIME_FORMATTER),
                     actual.getDocument().getThirdTitleInformation().getTelemechDecisionOutTime());
        assertEquals("0", actual.getDocument().getThirdTitleInformation().getTelemechDecisionOutTimeUtc());
        assertEquals(ewb.getTelemechOut().getLastName(),
                     actual.getDocument().getThirdTitleInformation().getTelemechInformation().getFullName().getLastName());
        assertEquals(ewb.getTelemechOut().getFirstName(),
                     actual.getDocument().getThirdTitleInformation().getTelemechInformation().getFullName().getFirstName());
        assertEquals(ewb.getTelemechOut().getPatronymic(),
                     actual.getDocument().getThirdTitleInformation().getTelemechInformation().getFullName().getPatronymic());
        assertEquals(ewb.getTransport().getType(), actual.getDocument().getThirdTitleInformation().getVehicleInformation().getVehicle().getType());
        assertEquals(ewb.getTransport().getBrand(), actual.getDocument().getThirdTitleInformation().getVehicleInformation().getVehicle().getBrand());
        assertEquals(ewb.getTransport().getModel(), actual.getDocument().getThirdTitleInformation().getVehicleInformation().getVehicle().getModel());
        assertEquals(ewb.getTransport().getStateNumber(),
                     actual.getDocument().getThirdTitleInformation().getVehicleInformation().getVehicle().getStateNumber());
    }
}
