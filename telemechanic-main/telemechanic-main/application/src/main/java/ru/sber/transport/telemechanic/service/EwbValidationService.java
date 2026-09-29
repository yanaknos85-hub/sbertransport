package ru.sber.transport.telemechanic.service;

import org.apache.commons.lang3.Range;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.EwbContractDetails;
import ru.sber.transport.telemechanic.dto.ewb.OdometerValue;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.exception.BadRequestException;
import ru.sber.transport.telemechanic.exception.ForbiddenOrganizationException;
import ru.sber.transport.telemechanic.exception.LitreageValidationException;
import ru.sber.transport.telemechanic.exception.OdmeterValueNotAcceptedException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static ru.sber.transport.telemechanic.enumerate.EwbStatus.*;
import static ru.sber.transport.telemechanic.exception.LitreageValidationException.*;
import static ru.sber.transport.telemechanic.service.impl.EwbServiceImpl.DATE_FORMAT;

public interface EwbValidationService {
    
    int ODOMETER_MAX_DIFFERENCE = 2500;
    Set<EwbStatus> IN_PROGRESS_STATUSES = Set.of(EWB_CREATED, MEDIC_IN_PROGRESS, TELEMECH_IN_PROGRESS, ON_THE_LINE);
    
    /**
     * Проверяет корректность данных ЭПЛ
     *
     * @param request запрос первого титула
     * @param driver водитель
     */
    void validateEwb(FirstTitleRequest request, Driver driver);
    
    /**
     * Проверяет корректность получения QR-кода
     *
     * @param ewbId идентификатор ЭПЛ
     * @param userId идентификатор пользователя
     */
    void validateEwbQrCode(UUID ewbId, UUID userId);
    
    /**
     * Проверяет корректность данных водителя
     *
     * @param driverId идентификатор водителя
     * @param ewbStartDate дата начала ЭПЛ
     * @param ewbEndDate дата окончания ЭПЛ
     */
    void validateDriver(UUID driverId, LocalDate ewbStartDate, LocalDate ewbEndDate);
    
    
    static void validateOdometerInValue(Integer odometerIn, Ewb ewb) {
        if (ON_THE_LINE != ewb.getStatus()) {
            throw new OdmeterValueNotAcceptedException("Показания одометра могут быть внесены только для ЭПЛ в статусе \"На линии\"");
        }
        if (Objects.isNull(ewb.getOdometerOut())) {
            throw new OdmeterValueNotAcceptedException("Необходимо внести показания одометра при выпуске на линию");
        }
        if (ewb.getOdometerOut() > odometerIn) {
            throw new OdmeterValueNotAcceptedException("Показания одометра при выпуске на линию меньше предоставленных показаний");
        }
        if (ewb.getOdometerOut() + ODOMETER_MAX_DIFFERENCE < odometerIn) {
            throw new OdmeterValueNotAcceptedException("Показания одометра при возвращении в гараж должны отличаться " +
                                                       "от значений при выпуске на линию не более чем на %s км".formatted(ODOMETER_MAX_DIFFERENCE));
        }
    }
    
    static void validateOdometerOutValue(OdometerValue request, Ewb ewb) {
        if (TELEMECH_IN_PROGRESS != ewb.getStatus()) {
            throw new OdmeterValueNotAcceptedException("Показания одометра могут быть внесены только для ЭПЛ в статусе \"Прохождение телемеханика\"");
        }
        
        var transport = ewb.getTransport();
        if (transport.getMileage() != null) {
            if (transport.getMileage() > request.value()) {
                throw new OdmeterValueNotAcceptedException("Текущие показания одометра больше, чем вносимые");
            } else if (transport.getMileage() + ODOMETER_MAX_DIFFERENCE < request.value()) {
                throw new OdmeterValueNotAcceptedException("Новые показания одометра не могут превышать значение предыдущего показания + %sкм"
                                                                   .formatted(ODOMETER_MAX_DIFFERENCE));
            }
        }
    }
    
    static void validateLitreageInValue(int value, Ewb ewb) {
        if (value <= 0) {
            throw new LitreageValidationException(LITREAGE_OUT_ZERO_VALUE_MSG);
        }
        
        var fuelTankVolume = ewb.getTransport().getFuelTankVolume();
        if (fuelTankVolume < value) {
            throw new LitreageValidationException(LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG.formatted(fuelTankVolume));
        }
    }
    
    static void validateLitreageOutValue(int value, Ewb ewb, UUID userId) {
        if (value < 0) {
            throw new LitreageValidationException(LITREAGE_OUT_ZERO_VALUE_MSG);
        }
        
        if (!Objects.equals(ewb.getDriver().getEmployee().getId(), userId)) {
            throw new LitreageValidationException(ONLY_EWB_DRIVER_CAN_PROVIDE_LITREAGE_OUT_MSG);
        }
        
        if (ewb.getRequest() == null || ewb.getRequest().getStatus() != RequestStatus.IN_PROGRESS) {
            throw new LitreageValidationException(REQUEST_IN_PROGRESS_IS_ABSENT_MSG);
        }
        
        if (TELEMECH_IN_PROGRESS != ewb.getStatus()) {
            throw new LitreageValidationException(INCORRECT_EWB_STATUS_MSG);
        }
        
        var fuelTankVolume = ewb.getTransport().getFuelTankVolume();
        if (fuelTankVolume < value) {
            throw new LitreageValidationException(LITREAGE_OUT_GREATER_THAN_POSSIBLE_VALUE_MSG.formatted(fuelTankVolume));
        }
    }
    
    static void validateEwbStatusForCancelling(EwbStatus status) {
        if (status.equals(EWB_CREATED) || status.equals(TELEMECH_IN_PROGRESS) || status.equals(MEDIC_IN_PROGRESS)) {
            return;
        }
        throw new BadRequestException("Не возможна отмена ЭПЛ в статусе %s".formatted(status));
    }
    
    static void validateDates(LocalDate start, LocalDate end) {
        if (!(start.equals(end) || start.plusDays(1).equals(end))) {
            throw new BadRequestException("Дата окончания действия путевого листа должна быть равна Дате начала либо Дате начала плюс один день");
        }
    }
    
    static void validateRequestStatus(RequestStatus status) throws IllegalStateResponseException {
        if (Stream.of(RequestStatus.DONE, RequestStatus.WARNING).noneMatch(Predicate.isEqual(status))) {
            throw new IllegalStateResponseException(String.format("Невозможно изменить заявку в статусе %s", status));
        }
    }
    
    static void validateEwbDates(LocalDate ewbStartDate, LocalDate ewbEndDate, Set<EwbContractDetails> ewbContractDetails) {
        var ewbRange = Range.of(ewbStartDate, ewbEndDate);
        ewbContractDetails.forEach(ewbContractDetail -> {
            if (!Range.of(ewbContractDetail.contractStart(), ewbContractDetail.contractEnd()).containsRange(ewbRange)) {
                throw new BadRequestException("Даты ЭПЛ не попадают в диапазон дат договора: %s - %s".formatted(
                        ewbContractDetail.contractStart().format(DATE_FORMAT),
                        ewbContractDetail.contractEnd().format(DATE_FORMAT)));
            }
        });
    }
    
    /**
     * Проверяет корректность данных водителя для второй титула
     * @param driver водитель
     * @param organization организация
     */
    void validateDriverForSecondTitle(Driver driver, Organization organization);
}
