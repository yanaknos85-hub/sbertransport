package ru.sber.transport.dispatcher.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.ConflictReason;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.files.shift.ShiftFile;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.validation.model.ValidationShiftFromMaisResult;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ShiftValidator {

    private final ShiftRepository shiftRepository;

    private final AutoparkRepository autoparkRepository;

    public ValidationShiftFromMaisResult validate(
            ShiftFromMaisMessage shiftFromMaisMessage,
            Optional<Driver> driverOptional,
            Optional<Vehicle> vehicleOptional
    ) {
        return validateData(driverOptional, vehicleOptional, shiftFromMaisMessage.stateNumber(), shiftFromMaisMessage.driverPersonnelNumber(),
                shiftFromMaisMessage.startDate(), shiftFromMaisMessage.endDate(),
                shiftFromMaisMessage.routeId(), shiftFromMaisMessage.action(), true);
    }

    public void validate(ShiftFile shiftFile,  Optional<Driver> driverOptional, Optional<Vehicle> vehicleOptional){
        validateData(driverOptional, vehicleOptional, shiftFile.getStateNumber(), shiftFile.getPersonnelNumber(),
                shiftFile.getStartDate().atStartOfDay(), shiftFile.getStartDate().atStartOfDay().plusDays(1).minusSeconds(1),
                null, null, false);
    }

    public static boolean isAcceptable(ShiftFromMaisMessage shiftMessage) {

        if (shiftMessage == null) {
            log.warn("Skip: message from MAIS is null");
            return false;
        }

        var routeId = shiftMessage.routeId();
        if (isBlank(routeId))  {
            log.warn("Skip message: routeId is null or blank, message id = {}", shiftMessage.id());
            return false;
        }

        var driverPersonnelNumber = shiftMessage.driverPersonnelNumber();
        if (isBlank(driverPersonnelNumber)) {
            log.warn("Skip message: driverPersonnelNumber is null or blank, message id = {}", shiftMessage.id());
            return false;
        }

        var stateNumber = shiftMessage.stateNumber();
        if (isBlank(stateNumber)) {
            log.warn("Skip message: stateNumber is null or blank, message id = {}", shiftMessage.id());
            return false;
        }

        var startDate = shiftMessage.startDate();
        var endDate   = shiftMessage.endDate();
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            log.warn("Skip message: startDate or endDate is null or endDate before startDate, message id = {}", shiftMessage.id());
            return false;
        }

        if (shiftMessage.action() == null) {
            log.warn("Skip message: action is null, message id = {}", shiftMessage.id());
            return false;
        }

        return true;
    }

    private ValidationShiftFromMaisResult validateData(Optional<Driver> driverOptional, Optional<Vehicle> vehicleOptional,
                                                       String stateNumber, String personnelNumber, LocalDateTime startDate,
                                                       LocalDateTime endDate, String routeId, ShiftFromMaisMessage.ActionType action,
                                                       boolean fromMais){
        if (driverOptional.isEmpty()) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.DRIVER_NOT_FOUND.getDescriptionForException().formatted(personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.DRIVER_NOT_FOUND);
        }
        var driver = driverOptional.get();
        if (!driver.isActive()) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.DRIVER_INACTIVE.getDescriptionForException().formatted(personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.DRIVER_INACTIVE);
        }
        if (vehicleOptional.isEmpty()) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.VEHICLE_NOT_FOUND.getDescriptionForException().formatted(stateNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.VEHICLE_NOT_FOUND);
        }
        var vehicle = vehicleOptional.get();
        if (!vehicle.isInExploitation()) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.VEHICLE_NOT_IN_EXPLOITATION.getDescriptionForException().formatted(stateNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.VEHICLE_NOT_IN_EXPLOITATION);
        }
        var vehicleAutopark = autoparkRepository.findById(vehicle.getAutopark().getId())
                .orElseThrow(() -> new EntityNotFoundException(Autopark.class, vehicle.getAutopark().getId()));
        if (!driver.getContractor().getId().equals(vehicleAutopark.getContractor().getId())) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.DIFFERENT_CONTRACTORS.getDescriptionForException().formatted(stateNumber, personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.DIFFERENT_CONTRACTORS);
        }
        if (driver.getAutopark() != null && !driver.getAutopark().getId().equals(vehicleAutopark.getId())) {
            if(!fromMais){
                throw new ConflictException(ConflictReason.DIFFERENT_AUTOPARKS.getDescriptionForException().formatted(stateNumber, personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.DIFFERENT_AUTOPARKS);
        }
        if(fromMais) {
            if (action.equals(ShiftFromMaisMessage.ActionType.UPDATE)) {
                return checkShiftExistenceForUpdate(vehicle.getId(), driver.getId(), startDate, endDate, stateNumber, personnelNumber, routeId);
            } else {
                return  checkShiftExistenceForCreate(vehicle.getId(), driver.getId(), startDate, endDate, stateNumber, personnelNumber,false);
            }
        } else {
            return checkShiftExistenceForCreate(vehicle.getId(), driver.getId(), startDate, endDate, stateNumber, personnelNumber,true);
        }
    }

    private ValidationShiftFromMaisResult checkShiftExistenceForCreate(UUID vehicleId, UUID driverId, LocalDateTime startDate,
                                                                       LocalDateTime endDate, String stateNumber, String personnelNumber,
                                                                       boolean throwException){
        var driverConflict = shiftRepository.existsDriverShiftConflict(driverId, startDate, endDate);
        var vehicleConflict = shiftRepository.existsVehicleShiftConflict(vehicleId, startDate, endDate);
        return checkShiftExistenceResult(driverConflict, vehicleConflict, stateNumber, personnelNumber, throwException);
    }

    private ValidationShiftFromMaisResult checkShiftExistenceForUpdate(UUID vehicleId, UUID driverId, LocalDateTime startDate,
                                                                       LocalDateTime endDate, String stateNumber, String personnelNumber,
                                                                       String routeId){
        var driverConflict = shiftRepository.existsDriverShiftConflictForUpdate(driverId, startDate, endDate, routeId);
        var vehicleConflict = shiftRepository.existsDriverShiftConflictForUpdate(vehicleId, startDate, endDate, routeId);
        return checkShiftExistenceResult(driverConflict, vehicleConflict, stateNumber, personnelNumber,false);
    }

    private ValidationShiftFromMaisResult checkShiftExistenceResult(boolean driverConflict, boolean vehicleConflict,
                                                                    String stateNumber, String personnelNumber, boolean throwException){
        if (driverConflict && vehicleConflict) {
            if(throwException){
                throw new ConflictException(ConflictReason.BOTH_EXISTING_ACTIVE_SHIFT.getDescriptionForException().formatted(stateNumber, personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.BOTH_EXISTING_ACTIVE_SHIFT);
        }
        if (driverConflict) {
            if(throwException){
                throw new ConflictException(ConflictReason.DRIVER_EXISTING_ACTIVE_SHIFT.getDescriptionForException().formatted(personnelNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.DRIVER_EXISTING_ACTIVE_SHIFT);
        }
        if (vehicleConflict) {
            if(throwException){
                throw new ConflictException(ConflictReason.VEHICLE_EXISTING_ACTIVE_SHIFT.getDescriptionForException().formatted(stateNumber));
            }
            return ValidationShiftFromMaisResult.fail(ConflictReason.VEHICLE_EXISTING_ACTIVE_SHIFT);
        }
        return ValidationShiftFromMaisResult.success();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
