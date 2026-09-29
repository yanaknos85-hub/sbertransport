package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.service.DriverService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbValidationServiceImplTest {
    
    @InjectMocks
    private EwbValidationServiceImpl ewbValidationService;
    @Mock
    private EwbRepository ewbRepository;
    @Mock
    private DriverService driverService;
    
    
    @Test
    @DisplayName("Проверка на наличие незакрытого ЭПЛ для водителя")
    void validateEwb_ShouldThrowEwbNotClosedException_WhenActiveEwbExists() {
        var driverId = UUID.randomUUID();
        var startDate = LocalDate.now();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getId), driverId)
                              .create();
        var request = Instancio.of(FirstTitleRequest.class)
                               .set(field(FirstTitleRequest::driverId), driverId)
                               .set(field(FirstTitleRequest::startDate), startDate)
                               .set(field(FirstTitleRequest::finishDate), startDate)
                               .create();
        when(ewbRepository.existsByDriverIdAndStartDateAndStatusIn(eq(driverId), eq(startDate), anySet())).thenReturn(true);
        var exception = assertThrows(EwbNotClosedException.class, () -> ewbValidationService.validateEwb(request, driver));
        assertEquals(String.format("У водителя с табельным номером: %s есть незакрытый ЭПЛ на дату %s",
                                   driver.getEmployee().getPersonnelNumber(),
                                   startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))),
                     exception.getMessage());
        verify(ewbRepository).existsByDriverIdAndStartDateAndStatusIn(eq(driverId), eq(startDate), anySet());
    }
    
    @Test
    @DisplayName("Проверка на отсутствие незакрытого ЭПЛ для водителя")
    void validateEwb_ShouldNotThrowException_WhenNoActiveEwbExists() {
        var driverId = UUID.randomUUID();
        var startDate = LocalDate.now();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getId), driverId)
                              .create();
        var request = Instancio.of(FirstTitleRequest.class)
                               .set(field(FirstTitleRequest::driverId), driverId)
                               .set(field(FirstTitleRequest::startDate), startDate)
                               .set(field(FirstTitleRequest::finishDate), startDate)
                               .create();
        when(ewbRepository.existsByDriverIdAndStartDateAndStatusIn(eq(driverId), eq(startDate), anySet())).thenReturn(false);
        assertDoesNotThrow(() -> ewbValidationService.validateEwb(request, driver));
        verify(ewbRepository).existsByDriverIdAndStartDateAndStatusIn(
                eq(driverId),
                eq(startDate),
                anySet());
    }
    
    @Test
    @DisplayName("Валидация QR-кода: успешная валидация")
    void validateEwbQrCode_ShouldNotThrowException_WhenValid() {
        var ewbId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                        .set(field(Employee::getId), userId)
                                                                        .create())
                              .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getId), ewbId)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::isQrCode), true)
                           .create();
        when(ewbRepository.findById(ewbId)).thenReturn(Optional.of(ewb));
        assertDoesNotThrow(() -> ewbValidationService.validateEwbQrCode(ewbId, userId));
        verify(ewbRepository).findById(ewbId);
    }
    
    @Test
    @DisplayName("Валидация QR-кода: ЭПЛ не найден")
    void validateEwbQrCode_ShouldThrowEwbNotFoundException_WhenEwbNotFound() {
        var ewbId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(ewbRepository.findById(ewbId)).thenReturn(Optional.empty());
        var exception = assertThrows(EwbNotFoundException.class, () -> ewbValidationService.validateEwbQrCode(ewbId, userId));
        assertEquals(String.format("ЭПЛ с идентификатором id=%s не найден!", ewbId), exception.getMessage());
        verify(ewbRepository).findById(ewbId);
    }
    
    @Test
    @DisplayName("Валидация QR-кода: не тот водитель")
    void validateEwbQrCode_ShouldThrowBadRequestException_WhenWrongDriver() {
        var ewbId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var wrongUserId = UUID.randomUUID();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                        .set(field(Employee::getId), wrongUserId)
                                                                        .create())
                              .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getId), ewbId)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::isQrCode), true)
                           .create();
        
        when(ewbRepository.findById(ewbId)).thenReturn(Optional.of(ewb));
        
        var exception = assertThrows(BadRequestException.class, () -> ewbValidationService.validateEwbQrCode(ewbId, userId));
        assertEquals("Только водитель ЭПЛ может получить QR-код", exception.getMessage());
        verify(ewbRepository).findById(ewbId);
    }
    
    @Test
    @DisplayName("Валидация QR-кода: QR-код не сформирован")
    void validateEwbQrCode_ShouldThrowBadRequestException_WhenQrCodeNotGenerated() {
        var ewbId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                        .set(field(Employee::getId), userId)
                                                                        .create())
                              .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getId), ewbId)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::isQrCode), false)
                           .create();
        
        when(ewbRepository.findById(ewbId)).thenReturn(Optional.of(ewb));
        
        var exception = assertThrows(BadRequestException.class, () -> ewbValidationService.validateEwbQrCode(ewbId, userId));
        assertEquals("Невозможно получить QR-код! Титул 4 не сформирован.", exception.getMessage());
        verify(ewbRepository).findById(ewbId);
    }
    
    @ParameterizedTest
    @MethodSource("provideDriverValidationData")
    @DisplayName("Валидация водителя: проверка на наличие незакрытого ЭПЛ")
    void validateDriver_ShouldThrowEwbNotClosedException_WhenActiveEwbExists(
            UUID driverId,
            LocalDate ewbStartDate,
            LocalDate ewbEndDate,
            String expectedMessageContains
                                                                            ) {
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getId), driverId)
                              .set(field(Driver::getEmployee), Instancio.of(Employee.class)
                                                                        .set(field(Employee::getPersonnelNumber), "12345")
                                                                        .create())
                              .create();
        var existingEwb = new Ewb();
        when(ewbRepository.findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
                eq(driverId),
                eq(ewbStartDate),
                eq(ewbEndDate),
                anySet())).thenReturn(Optional.of(existingEwb));
        when(driverService.getActiveDriverById(driverId)).thenReturn(driver);
        
        var exception = assertThrows(EwbNotClosedException.class, () -> ewbValidationService.validateDriver(driverId, ewbStartDate, ewbEndDate));
        
        assertThat(exception.getMessage()).isEqualTo(expectedMessageContains);
        verify(ewbRepository).findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
                eq(driverId),
                eq(ewbStartDate),
                eq(ewbEndDate),
                anySet());
        verify(driverService).getActiveDriverById(driverId);
    }
    
    @Test
    @DisplayName("Валидация водителя: нет активных ЭПЛ")
    void validateDriver_ShouldNotThrowException_WhenNoActiveEwbExists() {
        var driverId = UUID.randomUUID();
        var ewbStartDate = LocalDate.now();
        var ewbEndDate = LocalDate.now().plusDays(1);
        
        when(ewbRepository.findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
                eq(driverId),
                eq(ewbStartDate),
                eq(ewbEndDate),
                anySet())).thenReturn(Optional.empty());
        
        assertDoesNotThrow(() -> ewbValidationService.validateDriver(driverId, ewbStartDate, ewbEndDate));
        verify(ewbRepository).findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
                eq(driverId),
                eq(ewbStartDate),
                eq(ewbEndDate),
                anySet());
        verify(driverService, never()).getActiveDriverById(any(UUID.class));
    }
    
    @Test
    void testValidateDriverForSecondTitleSuccess() {
        var orgId = UUID.randomUUID();
        var organization = Instancio.of(Organization.class)
                                    .supply(field(Organization::getId), () -> orgId)
                                    .create();
        
        var employee = Instancio.of(Employee.class)
                                .supply(field(Employee::getOrganization), () -> organization)
                                .create();
        
        var driver = Instancio.of(Driver.class)
                              .supply(field(Driver::getEmployee), () -> employee)
                              .create();
        
        try {
            ewbValidationService.validateDriverForSecondTitle(driver, organization);
        } catch (Exception e) {
            fail("Неожиданное исключение");
        }
    }
    
    @Test
    void testValidateDriverForSecondTitleFailure() {
        var organization = Instancio.of(Organization.class).create();
        
        var otherOrganization = Instancio.of(Organization.class).create();
        
        var employee = Instancio.of(Employee.class)
                                .supply(field(Employee::getOrganization), () -> otherOrganization)
                                .create();
        
        var driver = Instancio.of(Driver.class)
                              .supply(field(Driver::getEmployee), () -> employee)
                              .create();
        
        assertThatThrownBy(() -> ewbValidationService.validateDriverForSecondTitle(driver, organization))
                .isInstanceOf(DriverDoesNotAssociatedWithOrganizationException.class);
    }
    
    private static Stream<Arguments> provideDriverValidationData() {
        var driverId = UUID.randomUUID();
        var today = LocalDate.now();
        return Stream.of(
                Arguments.of(driverId,
                             today,
                             today.plusDays(1),
                             "У водителя с табельным номером: 12345 есть незакрытый ЭПЛ на дату " +
                             today.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }
}
