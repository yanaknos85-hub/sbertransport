package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.database.model.Transport;
import ru.sber.transport.telemechanic.dto.dispatcher.GetOrganizationDispatcherResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.xjb.*;
import ru.sber.transport.telemechanic.dto.ewb.xjb.Attorney;
import ru.sber.transport.telemechanic.dto.ewb.xjb.Contact;
import ru.sber.transport.telemechanic.dto.ewb.xjb.DrivingLicense;
import ru.sber.transport.telemechanic.helper.EwbHelper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EwbFirstTitleMapperTest {
    
    private final EwbFirstTitleMapper mapper = new EwbFirstTitleMapperImpl();
    
    @Test
    void firstTitleRequestToFirstTitleXmlDto() {
        var humanReadableId = "PL-0001-0000000001";
        var fileName = "ON_PTLSSOBTS_2BM_2BM_2BM_01012000_0_a7d095f5-4fe4-4dea-b537-948b341389ae";
        var request = Instancio.create(FirstTitleRequest.class);
        var dispatcherOrganization = Instancio.create(GetOrganizationDispatcherResponse.class);
        var driver = Instancio.create(Driver.class);
        var transport = Instancio.create(Transport.class);
        var organizationAddress = Instancio.create(OrganizationAddress.class);
        var dispatcher = Instancio.create(Dispatcher.class);
        var creationTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
        var timeZone = "UTC+03:00";
        var actual = mapper.firstTitleRequestToFile(
                humanReadableId,
                fileName,
                request,
                dispatcherOrganization,
                driver,
                transport,
                organizationAddress,
                dispatcher,
                creationTime);
        
        assertFile(actual, fileName);
        assertDocument(actual.getDocument(), request, creationTime, humanReadableId, timeZone);
        assertRoute(actual.getDocument().getRoute(), request);
        assertPeriod(actual.getDocument().getRoute().getPeriod(), request);
        assertOwner(actual.getDocument().getRoute().getOwner());
        assertOrganizationInfo(actual.getDocument().getRoute().getOwner().getOwnerDetails(), dispatcherOrganization);
        assertAddressRf(actual.getDocument().getRoute().getOwner().getAddress(), organizationAddress);
        assertContact(actual.getDocument().getRoute().getOwner().getContact(), dispatcherOrganization);
        assertTransport(actual.getDocument().getRoute().getTransport(), transport);
        assertDrivingLicense(actual.getDocument().getRoute().getDriver().getDrivingLicense(), driver);
        assertFullName(actual.getDocument().getRoute().getDriver().getFullName(), driver.getEmployee());
        assertSigningPersonInfo(actual.getDocument().getSigningPersonInfo());
        assertFullName(actual.getDocument().getSigningPersonInfo().getFullName(), dispatcher.getEmployee());
        assertAttorney(actual.getDocument().getSigningPersonInfo().getAttorney(), dispatcher);
        assertThat(actual.getDocument().getRoute().getDriver().getTin()).isEqualTo(driver.getTin());
    }
    
    void assertFile(File actual, String fileName) {
        assertThat(actual)
                .extracting(
                        File::getIdFile,
                        File::getVersionProgram,
                        File::getVersionForm
                        )
                .containsExactly(
                        fileName,
                        "1.0.0",
                        "5.01"
                                );
    }
    
    void assertDocument(Document actual, FirstTitleRequest request, LocalDateTime creationTime, String humanReadableId, String timeZone) {
        assertThat(actual)
                .extracting(
                        Document::getKnd,
                        Document::getInformationDate,
                        Document::getInformationTime,
                        Document::getEwbNumber,
                        Document::getStartTime,
                        Document::getBeginRoute
                           )
                .containsExactly(
                        "1110380",
                        creationTime.format(EwbFirstTitleMapper.DATE_FORMATTER),
                        creationTime.format(EwbFirstTitleMapper.TIME_FORMATTER),
                        humanReadableId,
                        request.startDate().format(EwbFirstTitleMapper.DATE_FORMATTER),
                        "1"
                                );
    }
    
    void assertRoute(Route actual, FirstTitleRequest request) {
        assertThat(actual)
                .extracting(
                        Route::getEwbUuid,
                        Route::getMedicalExamination,
                        Route::getTransportationType,
                        Route::getCommunicationType
                           )
                .containsExactly(
                        request.ewbUuid().toString(),
                        "2",
                        request.transportationType(),
                        request.communicationType()
                                );
    }
    
    void assertPeriod(Period actual, FirstTitleRequest request) {
        assertThat(actual)
                .extracting(
                        Period::getEwbForADay,
                        Period::getEwbDateExecution,
                        Period::getStartTime,
                        Period::getEndTime
                           )
                .containsExactly(
                        EwbHelper.calculateEwbForADay(request.startDate(), request.finishDate()),
                        EwbHelper.calculateEwbDateExecution(request.startDate(), request.finishDate()),
                        EwbHelper.calculateEwbStartDate(request.startDate(), request.finishDate()),
                        EwbHelper.calculateEwbFinishDate(request.startDate(), request.finishDate())
                                );
    }
    
    void assertOwner(Owner actual) {
        assertThat(actual.getOwnerName()).isEqualTo("С");
    }
    
    void assertOrganizationInfo(OwnerDetails actual, GetOrganizationDispatcherResponse dispatcherOrganization) {
        var organizationInfo = actual.getOrganizationInfo();
        assertThat(organizationInfo)
                .extracting(
                        OrganizationInfo::getOrganizationName,
                        OrganizationInfo::getMsrn,
                        OrganizationInfo::getTin
                           )
                .containsExactly(
                        dispatcherOrganization.organization().name(),
                        dispatcherOrganization.organization().msrn(),
                        dispatcherOrganization.organization().tin()
                                );
    }
    
    void assertAddressRf(Address actual, OrganizationAddress organizationAddress) {
        var addressRf = actual.getAddressRf();
        assertThat(addressRf)
                .extracting(
                        AddressRf::getIndex,
                        AddressRf::getRegionCode
                           )
                .containsExactly(
                        organizationAddress.getZip(),
                        organizationAddress.getRegion().getCode()
                                );
    }
    
    void assertContact(Contact actual, GetOrganizationDispatcherResponse dispatcherOrganization) {
        assertThat(actual.getPhone()).isEqualTo(dispatcherOrganization.organization().phone());
    }
    
    void assertTransport(TransportEWB actual, Transport transport) {
        var transportXml = actual.getTransport();
        assertThat(transportXml)
                .extracting(
                        ru.sber.transport.telemechanic.dto.ewb.xjb.Transport::getTransportType,
                        ru.sber.transport.telemechanic.dto.ewb.xjb.Transport::getBrand,
                        ru.sber.transport.telemechanic.dto.ewb.xjb.Transport::getModel,
                        ru.sber.transport.telemechanic.dto.ewb.xjb.Transport::getStateNumber
                           )
                .containsExactly(
                        transport.getType(),
                        transport.getBrand(),
                        transport.getModel(),
                        transport.getStateNumber()
                                );
    }
    
    void assertDrivingLicense(DrivingLicense actual, Driver driver) {
        assertThat(actual)
                .extracting(
                        DrivingLicense::getNumber,
                        DrivingLicense::getSeries,
                        DrivingLicense::getIssueDate
                           )
                .containsExactly(
                        String.valueOf(driver.getDrivingLicense().getNumber()),
                        driver.getDrivingLicense().getSeries(),
                        driver.getDrivingLicense().getIssueDate().format(EwbFirstTitleMapper.DATE_FORMATTER)
                                );
    }
    
    void assertFullName(FullName actual, Employee employee) {
        assertThat(actual)
                .extracting(
                        FullName::getLastName,
                        FullName::getFirstName,
                        FullName::getPatronymic
                           )
                .containsExactly(
                        employee.getLastName(),
                        employee.getFirstName(),
                        employee.getPatronymic()
                                );
    }
    
    void assertSigningPersonInfo(SigningPersonInfo actual) {
        assertThat(actual)
                .extracting(
                        SigningPersonInfo::getSignType,
                        SigningPersonInfo::getAccessType
                           )
                .containsExactly(
                        "1",
                        "3"
                                );
    }
    
    void assertAttorney(Attorney actual, Dispatcher dispatcher) {
        assertThat(actual)
                .extracting(
                        Attorney::getId,
                        Attorney::getIssueDate,
                        Attorney::getCreationSystem
                           )
                .containsExactly(
                        dispatcher.getAttorney().getNumber().toString(),
                        dispatcher.getAttorney().getIssueDate().format(EwbFirstTitleMapper.DATE_FORMATTER),
                        dispatcher.getAttorney().getCreationSystem()
                                );
    }
}
