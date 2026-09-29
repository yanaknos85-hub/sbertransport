package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;
import ru.sber.transport.telemechanic.dto.ewb.OdometerValue;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.exception.BadRequestException;
import ru.sber.transport.telemechanic.exception.ForbiddenOrganizationException;
import ru.sber.transport.telemechanic.exception.LitreageValidationException;
import ru.sber.transport.telemechanic.exception.OdmeterValueNotAcceptedException;
import ru.sber.transport.telemechanic.service.EwbValidationService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.ON_THE_LINE;
import static ru.sber.transport.telemechanic.enumerate.EwbStatus.TELEMECH_IN_PROGRESS;
import static ru.sber.transport.telemechanic.enumerate.RequestStatus.DONE;
import static ru.sber.transport.telemechanic.exception.LitreageValidationException.*;

@ExtendWith(MockitoExtension.class)
class EwbValidationServiceTest {
    
    @Test
    void validateOdometerInValue_WhenStatusIsNotOnTheLine_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .create();
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerInValue(10000, ewb));
        assertEquals("Показания одометра могут быть внесены только для ЭПЛ в статусе \"На линии\"", exception.getMessage());
    }
    
    @Test
    void validateOdometerInValue_WhenOdometerOutIsNull_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .set(field(Ewb::getOdometerOut), null)
                           .create();
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerInValue(10000, ewb));
        assertEquals("Необходимо внести показания одометра при выпуске на линию", exception.getMessage());
    }
    
    @Test
    void validateOdometerInValue_WhenOdometerInLessThanOdometerOut_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .set(field(Ewb::getOdometerOut), 15000)
                           .create();
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerInValue(10000, ewb));
        assertEquals("Показания одометра при выпуске на линию меньше предоставленных показаний", exception.getMessage());
    }
    
    @Test
    void validateOdometerInValue_WhenOdometerInExceedsMaxDifference_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .set(field(Ewb::getOdometerOut), 10000)
                           .create();
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerInValue(12501, ewb));
        assertEquals("Показания одометра при возвращении в гараж должны отличаться от значений при выпуске на линию не более чем на 2500 км",
                     exception.getMessage());
    }
    
    @Test
    void validateOdometerInValue_WhenValid_Succeeds() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .set(field(Ewb::getOdometerOut), 10000)
                           .create();
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerInValue(12000, ewb));
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerInValue(10000, ewb));
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerInValue(12500, ewb));
    }
    
    @Test
    void validateOdometerOutValue_WhenStatusIsNotTelemechInProgress_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .create();
        var request = new OdometerValue(ewb.getId(), 10000);
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerOutValue(request, ewb));
        assertEquals("Показания одометра могут быть внесены только для ЭПЛ в статусе \"Прохождение телемеханика\"", exception.getMessage());
    }
    
    @Test
    void validateOdometerOutValue_WhenTransportMileageIsNotNullAndNewValueIsLess_ThrowsException() {
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getMileage), 15000)
                                 .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getTransport), transport)
                           .create();
        var request = new OdometerValue(ewb.getId(), 10000);
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerOutValue(request, ewb));
        assertEquals("Текущие показания одометра больше, чем вносимые", exception.getMessage());
    }
    
    @Test
    void validateOdometerOutValue_WhenNewValueExceedsMaxDifference_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getTransport), Instancio.of(Transport.class)
                                                                   .set(field(Transport::getMileage), 10000)
                                                                   .create())
                           .create();
        var request = new OdometerValue(ewb.getId(), 12501);
        var exception = assertThrows(OdmeterValueNotAcceptedException.class, () -> EwbValidationService.validateOdometerOutValue(request, ewb));
        assertEquals("Новые показания одометра не могут превышать значение предыдущего показания + 2500км", exception.getMessage());
    }
    
    @Test
    void validateOdometerOutValue_WhenTransportMileageIsNull_Succeeds() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getTransport), Instancio.of(Transport.class)
                                                                   .set(field(Transport::getMileage), null)
                                                                   .create())
                           .create();
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerOutValue(new OdometerValue(ewb.getId(), 10000), ewb));
    }
    
    @Test
    void validateOdometerOutValue_WhenValid_Succeeds() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getTransport), Instancio.of(Transport.class)
                                                                   .set(field(Transport::getMileage), 10000)
                                                                   .create())
                           .create();
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerOutValue(new OdometerValue(ewb.getId(), 10000), ewb));
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerOutValue(new OdometerValue(ewb.getId(), 12000), ewb));
        assertDoesNotThrow(() -> EwbValidationService.validateOdometerOutValue(new OdometerValue(ewb.getId(), 12500), ewb));
    }
    
    @Test
    void validateLitreageInValue_WhenValueIsZeroOrNegative_ThrowsException() {
        var ewb = Instancio.create(Ewb.class);
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageInValue(0, ewb));
        assertEquals(LITREAGE_OUT_ZERO_VALUE_MSG, exception.getMessage());
        
        exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageInValue(-5, ewb));
        assertEquals(LITREAGE_OUT_ZERO_VALUE_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageInValue_WhenValueExceedsFuelTankVolume_ThrowsException() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getTransport), Instancio.of(Transport.class)
                                                                   .set(field(Transport::getFuelTankVolume), 50)
                                                                   .create())
                           .create();
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageInValue(51, ewb));
        assertEquals(LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG.formatted(50), exception.getMessage());
    }
    
    @Test
    void validateLitreageInValue_WhenValid_Succeeds() {
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getTransport), Instancio.of(Transport.class)
                                                                   .set(field(Transport::getFuelTankVolume), 50)
                                                                   .create())
                           .create();
        assertDoesNotThrow(() -> EwbValidationService.validateLitreageInValue(1, ewb));
        assertDoesNotThrow(() -> EwbValidationService.validateLitreageInValue(50, ewb));
    }
    
    @Test
    void validateLitreageOutValue_WhenValueIsZeroOrNegative_ThrowsException() {
        var ewb = Instancio.create(Ewb.class);
        var userId = UUID.randomUUID();
        
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(-5, ewb, userId));
        assertEquals(LITREAGE_OUT_ZERO_VALUE_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenUserIdDoesNotMatchDriverId_ThrowsException() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getDriver), driver)
                           .create();
        
        var otherUserId = UUID.randomUUID();
        assertNotEquals(employee.getId(), otherUserId);
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(10, ewb, otherUserId));
        assertEquals(ONLY_EWB_DRIVER_CAN_PROVIDE_LITREAGE_OUT_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenRequestIsNull_ThrowsException() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::getRequest), null)
                           .create();
        var userId = employee.getId();
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(10, ewb, userId));
        assertEquals(REQUEST_IN_PROGRESS_IS_ABSENT_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenRequestStatusIsNotInProgress_ThrowsException() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var request = Instancio.of(Request.class)
                               .set(field(Request::getStatus), DONE)
                               .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::getRequest), request)
                           .create();
        var userId = employee.getId();
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(10, ewb, userId));
        assertEquals(REQUEST_IN_PROGRESS_IS_ABSENT_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenEwbStatusIsNotTelemechInProgress_ThrowsException() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var request = Instancio.of(Request.class)
                               .set(field(Request::getStatus), RequestStatus.IN_PROGRESS)
                               .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), ON_THE_LINE)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::getRequest), request)
                           .create();
        var userId = employee.getId();
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(10, ewb, userId));
        assertEquals(INCORRECT_EWB_STATUS_MSG, exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenValueExceedsFuelTankVolume_ThrowsException() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var request = Instancio.of(Request.class)
                               .set(field(Request::getStatus), RequestStatus.IN_PROGRESS)
                               .create();
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getFuelTankVolume), 50)
                                 .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::getRequest), request)
                           .set(field(Ewb::getTransport), transport)
                           .create();
        var userId = employee.getId();
        var exception = assertThrows(LitreageValidationException.class, () -> EwbValidationService.validateLitreageOutValue(51, ewb, userId));
        assertEquals(LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG.formatted(50), exception.getMessage());
    }
    
    @Test
    void validateLitreageOutValue_WhenValid_Succeeds() {
        var employee = Instancio.create(Employee.class);
        var driver = Instancio.of(Driver.class)
                              .set(field(Driver::getEmployee), employee)
                              .create();
        var request = Instancio.of(Request.class)
                               .set(field(Request::getStatus), RequestStatus.IN_PROGRESS)
                               .create();
        var transport = Instancio.of(Transport.class)
                                 .set(field(Transport::getFuelTankVolume), 50)
                                 .create();
        var ewb = Instancio.of(Ewb.class)
                           .set(field(Ewb::getStatus), TELEMECH_IN_PROGRESS)
                           .set(field(Ewb::getDriver), driver)
                           .set(field(Ewb::getRequest), request)
                           .set(field(Ewb::getTransport), transport)
                           .create();
        var userId = employee.getId();
        assertDoesNotThrow(() -> EwbValidationService.validateLitreageOutValue(1, ewb, userId));
        assertDoesNotThrow(() -> EwbValidationService.validateLitreageOutValue(50, ewb, userId));
    }
    
    @ParameterizedTest
    @EnumSource(value = EwbStatus.class, names = { "EWB_CREATED", "TELEMECH_IN_PROGRESS", "MEDIC_IN_PROGRESS" })
    void validateEwbStatusForCancelling_Succeeds(EwbStatus status) {
        assertDoesNotThrow(() -> EwbValidationService.validateEwbStatusForCancelling(status));
    }
    
    @ParameterizedTest
    @EnumSource(value = EwbStatus.class, names = { "EWB_CREATED", "TELEMECH_IN_PROGRESS", "MEDIC_IN_PROGRESS" }, mode = EnumSource.Mode.EXCLUDE)
    void validateEwbStatusForCancelling_ThrowsException(EwbStatus status) {
        var exception = assertThrows(BadRequestException.class, () -> EwbValidationService.validateEwbStatusForCancelling(status));
        assertEquals("Не возможна отмена ЭПЛ в статусе " + status.name(), exception.getMessage());
    }
    
    @Test
    void validateDates_Succeeds() {
        assertDoesNotThrow(() -> EwbValidationService.validateDates(LocalDate.now(), LocalDate.now()));
        assertDoesNotThrow(() -> EwbValidationService.validateDates(LocalDate.now(), LocalDate.now().plusDays(1)));
    }
    
    @Test
    void validateDates_WhenEndIsNotStartOrStartPlusOneDay_ThrowsException() {
        var start = LocalDate.now();
        var end1 = start.plusDays(2);
        var end2 = start.minusDays(1);
        var exception = assertThrows(BadRequestException.class, () -> EwbValidationService.validateDates(start, end1));
        assertEquals("Дата окончания действия путевого листа должна быть равна Дате начала либо Дате начала плюс один день", exception.getMessage());
        
        exception = assertThrows(BadRequestException.class, () -> EwbValidationService.validateDates(start, end2));
        assertEquals("Дата окончания действия путевого листа должна быть равна Дате начала либо Дате начала плюс один день", exception.getMessage());
    }
    
    @ParameterizedTest
    @EnumSource(value = RequestStatus.class, names = { "DONE", "WARNING" }, mode = EnumSource.Mode.EXCLUDE)
    void validateRequestStatus_WhenStatusIsFinal_ThrowsException(RequestStatus status) {
        var exception = assertThrows(IllegalStateResponseException.class, () -> EwbValidationService.validateRequestStatus(status));
        assertEquals("Невозможно изменить заявку в статусе " + status, exception.getMessage());
    }
    
    @ParameterizedTest
    @EnumSource(value = RequestStatus.class, names = { "DONE", "WARNING" })
    void validateRequestStatus_WhenStatusIsNotFinal_Succeeds(RequestStatus status) {
        assertDoesNotThrow(() -> EwbValidationService.validateRequestStatus(status));
    }
    
    @Test
    void validateEwbDates_WhenEwbDatesWithinContractRange_Succeeds() {
        var ewbStart = LocalDate.now();
        var ewbEnd = ewbStart.plusDays(1);
        var contractStart = ewbStart.minusDays(1);
        var contractEnd = ewbEnd.plusDays(1);
        var contractDetails = Set.of(Instancio.of(EwbContractDetails.class)
                                              .set(field(EwbContractDetails::contractStart), contractStart)
                                              .set(field(EwbContractDetails::contractEnd), contractEnd)
                                              .create());
        assertDoesNotThrow(() -> EwbValidationService.validateEwbDates(ewbStart, ewbEnd, contractDetails));
    }
    
    @Test
    void validateEwbDates_WhenEwbStartDateBeforeContractStart_ThrowsException() {
        var ewbStart = LocalDate.now();
        var ewbEnd = ewbStart.plusDays(1);
        var contractStart = ewbStart.plusDays(1);
        var contractEnd = ewbEnd.plusDays(2);
        var contractDetails = Set.of(Instancio.of(EwbContractDetails.class)
                                              .set(field(EwbContractDetails::contractStart), contractStart)
                                              .set(field(EwbContractDetails::contractEnd), contractEnd)
                                              .create());
        var exception = assertThrows(BadRequestException.class, () -> EwbValidationService.validateEwbDates(ewbStart, ewbEnd, contractDetails));
        assertTrue(exception.getMessage().contains("Даты ЭПЛ не попадают в диапазон дат договора:"));
    }
}