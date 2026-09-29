package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.sber.transport.telemechanic.database.dao.DriverRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.PageSettingDto;
import ru.sber.transport.telemechanic.dto.driver.AddDriverRequest;
import ru.sber.transport.telemechanic.dto.driver.DriverByFioRequest;
import ru.sber.transport.telemechanic.dto.driver.EditDriverRequest;
import ru.sber.transport.telemechanic.dto.driver.GetDriverResponse;
import ru.sber.transport.telemechanic.enumerate.ContactType;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.exception.DepartmentNotActiveException;
import ru.sber.transport.telemechanic.exception.TransportNotInUse;
import ru.sber.transport.telemechanic.exception.TransportUnboundException;
import ru.sber.transport.telemechanic.exception.UserNotFoundException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerOrganizationException;
import ru.sber.transport.telemechanic.exception.driver.*;
import ru.sber.transport.telemechanic.mapper.DriverMapper;
import ru.sber.transport.telemechanic.mapper.DrivingLicenseMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;
import ru.sber.transport.telemechanic.service.*;
import ru.sberbank.ditsib.transport.messaging.messages.ContactMessage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {
    
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private DrivingLicenseService drivingLicenseService;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private DriverMapper driverMapper;
    @Mock
    private DrivingLicenseMapper drivingLicenseMapper;
    @Mock
    private TransportService transportService;
    private final TransportValidationService transportValidationService = spy(new TransportValidationServiceImpl());
    @Captor
    private ArgumentCaptor<DrivingLicense> drivingLicenseCaptor;
    @Captor
    private ArgumentCaptor<Driver> driverCaptor;
    
    @InjectMocks
    private DriverServiceImpl driverService;
    
    private static final UUID TRANSPORT_ID_4 = UUID.fromString("5fedf8df-d19e-44a0-8528-4e3e81b1e55f");
    
    @Test
    void addDriver() {
        var request1 = Instancio.create(AddDriverRequest.class);
        
        doReturn(Optional.empty()).when(employeeService).get(request1.driver().employeeId());
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> driverService.addDriver(request1))
                .withMessage("Пользователь не найден, id:'%s'".formatted(request1.driver().employeeId()));
        
        var employee = Instancio.of(Employee.class)
                                .supply(field("department"), () -> Instancio.of(Department.class)
                                                                            .supply(field("active"), () -> false)
                                                                            .create())
                                .create();
        doReturn(Optional.of(employee)).when(employeeService).get(request1.driver().employeeId());
        assertThatExceptionOfType(DepartmentNotActiveException.class)
                .isThrownBy(() -> driverService.addDriver(request1))
                .withMessage("Подразделение с идентификатором %s не активно".formatted(employee.getDepartment().getId()));
        
        employee.getDepartment().setActive(true);
        doReturn(false).when(ewbTariffService).existsActiveTariffByOrganizationId(employee.getDepartment().getOrganization().getId());
        assertThatExceptionOfType(FleetOwnerOrganizationException.class)
                .isThrownBy(() -> driverService.addDriver(request1))
                .withMessage("Организация не является владельцем автопарка. ИД организации %s".formatted(
                        employee.getDepartment().getOrganization().getId()));
        
        doReturn(true).when(ewbTariffService).existsActiveTariffByOrganizationId(employee.getDepartment().getOrganization().getId());
        var request2 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "111-111-1 99")
                                                                        .create())
                                .create();
        assertThatExceptionOfType(SnilsNotFormattedException.class)
                .isThrownBy(() -> driverService.addDriver(request2))
                .withMessage("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(
                        request2.driver().snils()));
        
        var request3 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "111-111-111 99")
                                                                        .create())
                                .create();
        assertThatExceptionOfType(SnilsNotFormattedException.class)
                .isThrownBy(() -> driverService.addDriver(request3))
                .withMessage("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(
                        request3.driver().snils()));
        
        var request4 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "900-020-300 99")
                                                                        .create())
                                .create();
        assertThatExceptionOfType(SnilsNotFormattedException.class)
                .isThrownBy(() -> driverService.addDriver(request4))
                .withMessage("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(
                        request4.driver().snils()));
        
        var request5 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "900-020-301 99")
                                                                        .create())
                                .create();
        assertThatExceptionOfType(SnilsNotFormattedException.class)
                .isThrownBy(() -> driverService.addDriver(request5))
                .withMessage("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(
                        request5.driver().snils()));
        
        var request6 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "900-020-302 99")
                                                                        .create())
                                .create();
        assertThatExceptionOfType(SnilsNotFormattedException.class)
                .isThrownBy(() -> driverService.addDriver(request6))
                .withMessage("СНИЛС не соответствует формату (YYY-YYY-YYY XX, где Y - набор цифр ИНН, X - контрольная сумма). %s".formatted(
                        request6.driver().snils()));
        
        var request7 = Instancio.of(AddDriverRequest.class)
                                .supply(field("driver"), () -> Instancio.of(AddDriverRequest.DriverInfo.class)
                                                                        .supply(field("employeeId"), () -> request1.driver().employeeId())
                                                                        .supply(field("snils"), () -> "112-233-445 95")
                                                                        .create())
                                .create();
        doReturn(true).when(driverRepository).existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                request7.driver().employeeId(),
                request7.drivingLicense().series(),
                request7.drivingLicense().number()
                                                                                                                                           );
        assertThatExceptionOfType(DrivingLicenseAlreadyExistsException.class)
                .isThrownBy(() -> driverService.addDriver(request7))
                .withMessage("В системе существует ВУ с указанными данными");
        
        doReturn(false).when(driverRepository).existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                request7.driver().employeeId(),
                request7.drivingLicense().series(),
                request7.drivingLicense().number()
                                                                                                                                            );
        doReturn(true).when(driverRepository).existsByDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrueAndEmployeeIdNot(
                request7.drivingLicense().series(),
                request7.drivingLicense().number(),
                request7.driver().employeeId()
                                                                                                                                              );
        assertThatExceptionOfType(DriverAlreadyExistsException.class)
                .isThrownBy(() -> driverService.addDriver(request7))
                .withMessage("В системе существует сотрудник с серией - %s, номером - %s".formatted(
                        request7.drivingLicense().series(),
                        request7.drivingLicense().number()
                                                                                                   ));
        
        doReturn(false).when(driverRepository).existsByDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrueAndEmployeeIdNot(
                request7.drivingLicense().series(),
                request7.drivingLicense().number(),
                request7.driver().employeeId()
                                                                                                                                               );
        doReturn(true).when(driverRepository).existsByTinAndSnilsAndActiveEmployeeNot(
                request7.driver().tin(),
                request7.driver().snils(),
                request7.driver().employeeId()
                                                                                 );
        assertThatExceptionOfType(DriverAlreadyExistsException.class)
                .isThrownBy(() -> driverService.addDriver(request7))
                .withMessage("В системе существует сотрудник с ИНН - %s, СНИЛС - %s".formatted(request7.driver().tin(), request7.driver().snils()));
        
        doReturn(false).when(driverRepository).existsByTinAndSnilsAndActiveEmployeeNot(
                request7.driver().tin(),
                request7.driver().snils(),
                request7.driver().employeeId()
                                                                                  );
        var driver = Instancio.create(Driver.class);
        var drivingLicense = Instancio.create(DrivingLicense.class);
        doReturn(Optional.of(driver)).when(driverRepository).findWithLicenseByEmployeeId(request7.driver().employeeId());
        doNothing().when(drivingLicenseService).saveOrUpdate(any());
        doReturn(drivingLicense).when(drivingLicenseMapper).drivingLicenseInfoToDrivingLicense(request7.drivingLicense());
        doReturn(driver).when(driverRepository).save(any());
        
        driverService.addDriver(request7);
        
        verify(drivingLicenseService).saveOrUpdate(drivingLicenseCaptor.capture());
        verify(driverRepository).save(driverCaptor.capture());
        
        var savedDrivingLicense = drivingLicenseCaptor.getValue();
        assertThat(savedDrivingLicense.isActive()).isFalse();
        var savedDriver = driverCaptor.getValue();
        assertThat(savedDriver.getDrivingLicense().getPreviousId()).isEqualTo(savedDrivingLicense.getId());
        
        doReturn(Optional.empty()).when(driverRepository).findWithLicenseByEmployeeId(request7.driver().employeeId());
        doReturn(driver).when(driverMapper).addDriverRequestToDriver(any());
        doReturn(driver).when(driverRepository).save(any());
        
        driverService.addDriver(request7);
        
        verify(driverRepository, times(2)).save(driverCaptor.capture());
        savedDriver = driverCaptor.getValue();
        assertThat(savedDriver).isNotNull();
    }
    
    @Test
    void editDriver() {
        var driverId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var request = Instancio.of(EditDriverRequest.class)
                               .supply(field("snils"), () -> "112-233-445 95")
                               .create();
        var driver = Instancio.create(Driver.class);
        doReturn(Optional.empty()).when(driverRepository).findById(driverId);
        assertThatExceptionOfType(DriverNotFoundException.class)
                .isThrownBy(() -> driverService.editDriver(driverId, request, userId))
                .withMessage("Не найден водитель ID:%s".formatted(driverId));
        
        doReturn(Optional.of(driver)).when(driverRepository).findById(driverId);
        doReturn(Optional.empty()).when(employeeService).get(userId);
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> driverService.editDriver(driverId, request, userId))
                .withMessage("Пользователь не найден, id:'%s'".formatted(userId));
        
        var initiator = Instancio.create(Employee.class);
        doReturn(Optional.of(initiator)).when(employeeService).get(userId);
        assertThatExceptionOfType(NotSameOrganizationException.class)
                .isThrownBy(() -> driverService.editDriver(driverId, request, userId))
                .withMessage("Пользователи не из одной организации. organizationId1: %s, organizationId2: %s".formatted(
                        initiator.getOrganization().getId(), driver.getEmployee().getOrganization().getId()
                                                                                                                       ));
        
        doReturn(Optional.of(driver.getEmployee())).when(employeeService).get(userId);
        doReturn(true).when(driverRepository).existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                driver.getEmployee().getId(),
                request.drivingLicense().series(),
                request.drivingLicense().number());
        assertThatExceptionOfType(DrivingLicenseAlreadyExistsException.class)
                .isThrownBy(() -> driverService.editDriver(driverId, request, userId))
                .withMessage("В системе существует ВУ с указанными данными");
        
        doReturn(false).when(driverRepository).existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                driver.getEmployee().getId(),
                request.drivingLicense().series(),
                request.drivingLicense().number());
        doReturn(driver).when(driverRepository).save(any());
        
        driverService.editDriver(driverId, request, userId);
        
        verify(driverRepository).save(driverCaptor.capture());
        var savedDriver = driverCaptor.getValue();
        assertThat(savedDriver)
                .isNotNull()
                .extracting(
                        Driver::getTin,
                        Driver::getSnils,
                        el -> el.getDrivingLicense().getSeries(),
                        el -> el.getDrivingLicense().getNumber(),
                        el -> el.getDrivingLicense().getIssueDate(),
                        el -> el.getDrivingLicense().getExpiryDate(),
                        el -> el.getDrivingLicense().getCategories().stream()
                                .map(Category::getId)
                                .collect(Collectors.toSet())
                           )
                .containsExactly(
                        request.tin(),
                        request.snils(),
                        request.drivingLicense().series(),
                        request.drivingLicense().number(),
                        request.drivingLicense().issueDate(),
                        request.drivingLicense().expiryDate(),
                        request.drivingLicense().categoryIds()
                                );
    }
    
    @Test
    void editDriverInfoNoDriverLicense() {
        var driverId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var request = new EditDriverRequest("325057674283", "644-071-140 63", null);
        var driver = Instancio.create(Driver.class);
        
        doReturn(Optional.of(driver)).when(driverRepository).findById(driverId);
        doReturn(Optional.of(driver.getEmployee())).when(employeeService).get(userId);
        
        driverService.editDriver(driverId, request, userId);
        
        verify(driverRepository).save(driverCaptor.capture());
        var savedDriver = driverCaptor.getValue();
        assertThat(savedDriver)
                .isNotNull()
                .extracting(
                        Driver::getTin,
                        Driver::getSnils,
                        el -> el.getDrivingLicense().getSeries(),
                        el -> el.getDrivingLicense().getNumber(),
                        el -> el.getDrivingLicense().getIssueDate(),
                        el -> el.getDrivingLicense().getExpiryDate(),
                        el -> el.getDrivingLicense().getCategories().stream()
                                .map(Category::getId)
                                .collect(Collectors.toSet())
                           )
                .containsExactly(
                        request.tin(),
                        request.snils(),
                        driver.getDrivingLicense().getSeries(),
                        driver.getDrivingLicense().getNumber(),
                        driver.getDrivingLicense().getIssueDate(),
                        driver.getDrivingLicense().getExpiryDate(),
                        driver.getDrivingLicense().getCategories().stream()
                              .map(Category::getId)
                              .collect(Collectors.toSet()));
    }
    
    @Test
    void deactivateDriver() {
        var driverId = UUID.randomUUID();
        var driver = Instancio.of(Driver.class)
                              .supply(field("drivingLicense"), () -> Instancio.of(DrivingLicense.class)
                                                                              .supply(field("active"), () -> false)
                                                                              .create())
                              .create();
        
        doReturn(Optional.empty()).when(driverRepository).findById(driverId);
        assertThatExceptionOfType(DriverNotFoundException.class)
                .isThrownBy(() -> driverService.deactivateDriver(driverId))
                .withMessage("Не найден водитель ID:%s".formatted(driverId));
        
        doReturn(Optional.of(driver)).when(driverRepository).findById(driverId);
        assertThatExceptionOfType(DrivingLicenseNotActiveException.class)
                .isThrownBy(() -> driverService.deactivateDriver(driverId))
                .withMessage("Неактивна запись для водителя. ВУ ID:%s".formatted(driver.getId()));
        
        driver.setDrivingLicense(Instancio.of(DrivingLicense.class)
                                          .supply(field("active"), () -> true)
                                          .create());
        doReturn(driver).when(driverRepository).save(any());
        driverService.deactivateDriver(driverId);
        
        verify(driverRepository).save(driverCaptor.capture());
        var savedDriver = driverCaptor.getValue();
        assertThat(savedDriver)
                .isNotNull()
                .extracting(el -> el.getDrivingLicense().isActive())
                .isEqualTo(false);
    }
    
    @Test
    void getDriverById() {
        var id = UUID.randomUUID();
        doReturn(Optional.empty()).when(driverRepository).findById(id);
        assertThatExceptionOfType(DriverNotFoundException.class)
                .isThrownBy(() -> driverService.getDriverById(id))
                .withMessage("Не найден водитель ID:%s".formatted(id));
        
        var driver = Instancio.of(Driver.class).create();
        var mappedDriver = Instancio.of(GetDriverResponse.class).create();
        doReturn(Optional.of(driver)).when(driverRepository).findById(id);
        doReturn(mappedDriver).when(driverMapper).driverToGetDriverResponse(driver);
        var actual = driverService.getDriverById(id);
        assertThat(actual)
                .isNotNull()
                .isEqualTo(mappedDriver);
    }
    
    @Test
    void getDriversByFio() {
        var userId = UUID.randomUUID();
        var userOrganizationId = UUID.randomUUID();
        var searchText = UUID.randomUUID().toString();
        var organization = new Organization().withId(userOrganizationId);
        when(employeeService.getByUserId(any())).thenReturn(new Employee().withOrganization(organization));
        when(driverRepository.findByFio(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(transportService.getTransportByIdWithOrganizations(any())).thenReturn(new Transport().setStatus(TransportStatus.IN_USE)
                                                                                                  .setOrganizations(Set.of(organization)));
        driverService.getDriversByFio(new DriverByFioRequest(searchText, TRANSPORT_ID_4, null), userId);
        verify(employeeService, times(1)).getByUserId(userId);
        verify(driverRepository, times(1)).findByFio(eq(userOrganizationId), eq(searchText), any(), any());
    }
    
    @Test
    void getDriversByFio_transportNotInUse_throwsException() {
        var request = new DriverByFioRequest("Иван", TRANSPORT_ID_4, new PageSettingDto(0, 10));
        var transport = new Transport().setId(TRANSPORT_ID_4).setStatus(TransportStatus.NOT_IN_USE);
        
        when(transportService.getTransportByIdWithOrganizations(TRANSPORT_ID_4)).thenReturn(transport);
        
        assertThrowsExactly(TransportNotInUse.class, () -> driverService.getDriversByFio(request, any()),
                            "Транспортное средство " + TRANSPORT_ID_4 + " выведено из эксплуатации");
    }
    
    @Test
    void getDriversByFio_dispatcherOrgNotLinked_throwsException() {
        var request = new DriverByFioRequest("Иван", TRANSPORT_ID_4, new PageSettingDto(0, 10));
        var transport = new Transport().setId(TRANSPORT_ID_4).setStatus(TransportStatus.IN_USE)
                                       .setOrganizations(Set.of(new Organization().withId(UUID.randomUUID())));
        
        var employee = new Employee().withOrganization(new Organization().withId(UUID.randomUUID()));
        
        when(transportService.getTransportByIdWithOrganizations(TRANSPORT_ID_4)).thenReturn(transport);
        when(employeeService.getByUserId(any())).thenReturn(employee);
        
        assertThrowsExactly(TransportUnboundException.class, () -> driverService.getDriversByFio(request, any()),
                            "Транспортное средство " + TRANSPORT_ID_4 + " не связано с организацией диспетчера");
    }
    
    @Test
    void getByEmployeeId() {
        var employeeId = UUID.randomUUID();
        var driver = Instancio.create(Driver.class);
        doReturn(Optional.of(driver)).when(driverRepository).findWithEmployeeAndLicenseByEmployeeIdAndDrivingLicense_ActiveTrue(employeeId);
        
        var actual = driverService.getByEmployeeId(employeeId);
        
        verify(driverRepository).findWithEmployeeAndLicenseByEmployeeIdAndDrivingLicense_ActiveTrue(employeeId);
        assertThat(actual).usingRecursiveAssertion().isEqualTo(driver);
    }
    
    @Test
    void getByEmployeeIdException() {
        var employeeId = UUID.randomUUID();
        assertThrows(DriverNotFoundException.class, () -> driverService.getByEmployeeId(employeeId));
    }
    
    @Test
    void saveMessage() {
        var series = "12 34";
        var number = "555453";
        var message = Instancio.of(DriverMessage.class)
                               .set(field(DriverMessage::driverLicenseNumber), series + " " + number)
                               .create();
        var driver = new Driver();
        driver.setDrivingLicense(new DrivingLicense());
        when(driverRepository.findWithLicenseByEmployeeId(any())).thenReturn(Optional.of(driver));
        
        driverService.saveMessage(message);
        assertEquals(message.autoparkId(), driver.getAutoparkId());
        assertEquals(message.contractorId(), driver.getContractorId());
        assertEquals(message.tin(), driver.getTin());
        assertEquals(message.snils(), driver.getSnils());
        DrivingLicense license = driver.getDrivingLicense();
        assertEquals(message.active(), license.isActive());
        assertEquals(message.expiryDate(), license.getExpiryDate());
        assertEquals(message.issueDate(), license.getIssueDate());
        assertEquals(series, license.getSeries());
        verify(driverRepository).save(any());
        
    }
    
    @Test
    void saveMessage_notFoundDriver() {
        when(driverRepository.findWithLicenseByEmployeeId(any())).thenReturn(Optional.empty());
        Employee employee = new Employee();
        when(employeeService.get(any())).thenReturn(Optional.of(employee));
        
        var series = "12 34";
        var number = "555453";
        var message = Instancio.of(DriverMessage.class)
                               .set(field(DriverMessage::driverLicenseNumber), number + " " + series)
                               .set(field(DriverMessage::oauthId), UUID.randomUUID())
                               .create();
        
        driverService.saveMessage(message);
        verify(driverRepository).save(any());
    }
}
