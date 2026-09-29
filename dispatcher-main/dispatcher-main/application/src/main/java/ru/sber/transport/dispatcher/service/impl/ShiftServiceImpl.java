package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftConflictRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.ShiftConflict;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.dispatcher.dto.ShiftListResponseDto;
import ru.sber.transport.dispatcher.mappers.ShiftConflictMapper;
import ru.sber.transport.dispatcher.mappers.ShiftMapper;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.ShiftSender;
import ru.sber.transport.dispatcher.service.ShiftService;
import ru.sber.transport.dispatcher.validation.ShiftValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;

    private final DriverRepository driverRepository;

    private final VehicleRepository vehicleRepository;

    private final ShiftConflictRepository shiftConflictRepository;

    private final ShiftSender shiftSender;

    private final ShiftValidator shiftFromMaisValidator;

    private final ShiftMapper shiftMapper;

    private final ShiftConflictMapper shiftConflictMapper;

    @Override
    public List<Shift> findAllByDriverIdAndDate(UUID driverId, LocalDateTime start, LocalDateTime end) {
        return shiftRepository.findAllByDriverIdAndDateBetweenAndDeleted(driverId, start, end, false);
    }

    @Override
    public List<Shift> findAllByVehicleIdAndDate(UUID vehicleId, LocalDateTime start, LocalDateTime end) {
        return shiftRepository.findAllByVehicleIdAndDateBetweenAndDeleted(vehicleId, start, end, false);
    }

    @Override
    public Shift save(Shift shift, Source source) {
        shift = shiftRepository.save(shift);
        shiftSender.send(shift, source);
        return shift;
    }

    @Override
    public Page<Shift> findAll(Specification<Shift> spec, Pageable pageable) {
        return shiftRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<Shift> get(UUID shiftId) {
        return Optional.ofNullable(shiftId).flatMap(shiftRepository::findById);
    }

    @Override
    public List<Shift> getShiftByDriverIdAndCurrentDate(Driver driver, LocalDateTime date) {
        return shiftRepository.findAllByDriverAndStartDateBeforeAndEndDateAfterAndDeleted(driver, date,
                date,false);
    }

    @Override
    public List<Shift> findAllByContractorId(UUID contractorId) {
        return shiftRepository.findAllByContractorIdAndDeletedFalse(contractorId);
    }

    @Override
    public List<Shift> getShiftIdsByStartDateAfterAndRowId(LocalDate date, UUID rowId) {
        return shiftRepository.findAllByRowIdAndStartDateAfter(rowId, date.atStartOfDay());
    }

    @Override
    public void handleShiftFromMais(ShiftFromMaisMessage shiftFromMaisMessage) {
        switch (shiftFromMaisMessage.action()){
            case CREATE, UPDATE -> {
                var driverOptional = driverRepository.findByPersonnelNumberIgnoreCaseAndActiveTrue(shiftFromMaisMessage.driverPersonnelNumber());
                var vehicleOptional = vehicleRepository.findByStateNumberIgnoreCaseAndInExploitationTrue(shiftFromMaisMessage.stateNumber());
                var validationResult = shiftFromMaisValidator.validate(shiftFromMaisMessage, driverOptional, vehicleOptional);
                if(validationResult.valid()){
                    var shift = shiftRepository.findByRouteId(shiftFromMaisMessage.routeId()).orElseGet(Shift::new);
                    shiftMapper.update(shift,
                            shiftFromMaisMessage,
                            driverOptional.get().getId(),
                            vehicleOptional.get().getId(),
                            driverOptional.get().getContractor().getId());
                    shiftRepository.save(shift);
                    shiftSender.send(shift, Source.CONTRACTOR);
                    shiftConflictRepository.deleteById(shiftFromMaisMessage.routeId());
                } else {
                    var shiftConflict = shiftConflictRepository.findById(shiftFromMaisMessage.routeId()).orElseGet(ShiftConflict::new);
                    shiftConflictMapper.update(shiftConflict, shiftFromMaisMessage, validationResult.reason());
                    if(shiftConflict.getRouteId() == null){
                        shiftConflict.setRouteId(shiftFromMaisMessage.routeId());
                    }
                    shiftConflictRepository.save(shiftConflict);
                }
            }
            case DELETE -> {
                var shiftOpt = shiftRepository.findByRouteId(shiftFromMaisMessage.routeId());
                if (shiftOpt.isPresent()){
                    var shift = shiftOpt.get();
                    shift.setDeleted(true);
                    shift.setActive(false);
                    shiftRepository.save(shift);
                    shiftSender.send(shift, Source.CONTRACTOR);
                }
                shiftConflictRepository.deleteById(shiftFromMaisMessage.routeId());
            }
        }
    }

    @Override
    public List<ShiftListResponseDto> getShiftList(UUID contractorId, UUID autoparkId, OffsetDateTime startDate){
        if (autoparkId != null){
            return shiftRepository.findAllByContractorIdAndAutoparkIdAndStartDate(contractorId, autoparkId,
                    startDate.toLocalDateTime(),
                    startDate.plusDays(1).minusNanos(1).toLocalDateTime());
        }
        if (contractorId != null) {
            return shiftRepository.findAllByContractorIdAndStartDate(contractorId,
                    startDate.toLocalDateTime(),
                    startDate.plusDays(1).minusNanos(1).toLocalDateTime());
        } else return shiftRepository.findAllByStartDate(startDate.toLocalDateTime(),
                startDate.plusDays(1).minusNanos(1).toLocalDateTime());
    }

    @Override
    public List<ShiftForEwbDto> findEwbShifts(List<UUID> shiftIds) {
        return shiftRepository.findAllById(shiftIds);
    }
}
