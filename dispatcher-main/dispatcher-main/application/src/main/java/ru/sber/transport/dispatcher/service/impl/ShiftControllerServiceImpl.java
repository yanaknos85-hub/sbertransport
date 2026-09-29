package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.dispatcher.converters.ConstraintViolationExceptionConverter;
import ru.sber.transport.dispatcher.database.dao.DriverRepository;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ShiftSearchDto;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.exceptions.DataConflictException;
import ru.sber.transport.dispatcher.mappers.ShiftMapper;
import ru.sber.transport.dispatcher.database.model.Dispatcher;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.messages.CreateEwbMessage;
import ru.sber.transport.dispatcher.messages.EwbMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.messaging.senders.ShiftSender;
import ru.sber.transport.dispatcher.service.*;
import ru.sber.transport.exceptions.EntityNotFoundException;

import jakarta.validation.Validation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShiftControllerServiceImpl implements ShiftControllerService {

    private static final String START_DATE_END_DATE = "startDate-endDate";

    private static final String SHIFT_OBJECT_NAME = "Shift";

    private final DriverService driverService;

    private final DriverRepository driverRepository;

    private final VehicleService vehicleService;

    private final AutoparkService autoparkService;

    private final ShiftService shiftService;

    private final ShiftMapper shiftMapper;

    private final DispatcherService dispatcherService;

    private final EwbGrpcService ewbGrpcService;

    private final ShiftSender shiftSender;

    private final EwbUpdateService ewbUpdateService;

    private final ShiftConflictService shiftConflictService;

    @Value("${dispatcher-room.admin.role:ROLE_DISPATCHER_ROOM_ADMIN}")
    private String dispatcherRoomAdminRole;

    @Value("${dispatcher-room.federal-dispatcher.role:ROLE_FEDERAL_DISPATCHER_CONTRACTOR}")
    private String federalDispatcherRole;

    @Value("${dispatcher-room.manager.role:ROLE_MAIN_DISPATCHER_CONTRACTOR}")
    private String mainDispatcherRole;

    @Override
    public List<ShiftDTO> addShift(UUID contractorId, Authentication authentication, List<ShiftDTO> shiftDTOList) {
        var dispatcher = dispatcherAuthCheck(contractorId, authentication);
        var rowId = UUID.randomUUID();
        if (shiftDTOList.size()>1){
            shiftDTOList.parallelStream().forEach(sh -> sh.setRowId(rowId));
        }
        return validateAndSave(contractorId, dispatcher, shiftDTOList, true);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Page<ShiftResponseDTO> getShifts(UUID contractorId, ShiftSearchDto shiftSearchDto, Authentication authentication) {
        dispatcherAuthCheck(contractorId, authentication);
        var foundedShifts = shiftService.findAllByContractorId(contractorId);
        log.info("Найдено смен по контрагенту - "+foundedShifts.size());
        var finalShiftList = new CopyOnWriteArrayList<>(foundedShifts);
        if (!foundedShifts.isEmpty()) {
            log.info("Начало фильтрации по временным рамкам startDate - "+shiftSearchDto.getStartDate()+" и endDate - "+shiftSearchDto.getEndDate());
            if (shiftSearchDto.getStartDate() != null && shiftSearchDto.getEndDate() != null) {
                foundedShifts.parallelStream().forEach(shift -> {
                    if (shiftSearchDto.getStartDate().isAfter(shift.getEndDate()) ||
                            shiftSearchDto.getEndDate().isBefore(shift.getStartDate())) {
                        finalShiftList.remove(shift);
                    }
                });
            }
            log.info("Количество неподходящих для временных рамок смен - "+(foundedShifts.size()-finalShiftList.size()));
        }
        log.info("Найдено смен по контрагенту c учетом временных рамок - "+finalShiftList.size());
        var pageRequest = PageRequest.of(shiftSearchDto.getPage(), shiftSearchDto.getSize());
        return new PageImpl<>(finalShiftList.parallelStream()
                .map(shiftMapper::toResponseDTO)
                .sorted(Comparator.comparing(ShiftResponseDTO::getStartDate))
                .skip(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .toList(),
                pageRequest, finalShiftList.size());
    }

    @Override
    public Page<VehicleShiftResponse> getVehicleShifts(UUID contractorId, VehicleShiftSearchDto searchDto, Authentication authentication) {
        dispatcherAuthCheck(contractorId, authentication);
        return vehicleService.getAllByContractorIdAndFilters(contractorId, searchDto);
    }

    @Override
    public List<VehicleShiftStatusResponse> getVehicleShiftStatus(UUID contractorId, List<UUID> vehicleIds, Authentication authentication) {
        dispatcherAuthCheck(contractorId, authentication);
        return vehicleService.getVehicleShiftStatus(contractorId, vehicleIds);
    }

    @Override
    public void editShift(UUID contractorId, UUID shiftId, Authentication authentication, ShiftDTO shiftDTO) {
        var dispatcher = dispatcherAuthCheck(contractorId, authentication);
        var entity = shiftService.get(shiftId);
        if (entity.isEmpty()) {
            throw new EntityNotFoundException(Shift.class, shiftId);
        }
        var shift = entity.get();
        shiftMapper.update(shift, shiftDTO);
        var shiftDtoList = new ArrayList<ShiftDTO>();
        shiftDtoList.add(shiftMapper.toDTO(shift));
        validateAndSave(contractorId, dispatcher, shiftDtoList, false);
    }

    @Override
    public ShiftResponseDTO getShift(UUID contractorId, UUID shiftId, Authentication authentication) {
        dispatcherAuthCheck(contractorId, authentication);
        var shift = shiftService.get(shiftId);
        return shift.map(shiftMapper::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException(Shift.class, shiftId));
    }

    @Override
    public void deleteShift(UUID contractorId, UUID shiftId, Authentication authentication) {
        dispatcherAuthCheck(contractorId, authentication);
        var shift = shiftService.get(shiftId);
        if (shift.isEmpty()) {
            return;
        }
        var deleted = shift.get();
        deleted.setDeleted(true);
        deleted.setActive(false);
        shiftService.save(deleted, Source.CONTRACTOR);
    }

    @Override
    public void deleteShiftRow(UUID contractorId, UUID rowId, Authentication authentication, DeleteShiftRowDTO dto) {
        dispatcherAuthCheck(contractorId, authentication);
        var shifts = shiftService.getShiftIdsByStartDateAfterAndRowId(dto.getDate(), rowId);
        shifts.forEach(shift -> {
            shift.setDeleted(true);
            shift.setActive(false);
            shiftService.save(shift, Source.CONTRACTOR);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ShiftListResponseDto> getShiftList(OffsetDateTime startDate, Authentication authentication) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        var roles = (Collection<? extends String>) jwtAuthenticationToken.getToken().getClaims().get("roles");
        var dispatcher = dispatcherService.getByOauthId(UUID.fromString(jwtAuthenticationToken.getToken().getId()))
                .orElseThrow(() -> new EntityNotFoundException(Dispatcher.class,
                        UUID.fromString(jwtAuthenticationToken.getToken().getId())));
        if(roles.contains(federalDispatcherRole)){
            return shiftService.getShiftList(null, null, startDate);
        }
        if(roles.contains(mainDispatcherRole)){
            return shiftService.getShiftList(dispatcher.getContractor().getId(), null, startDate);
        } else {
            assert dispatcher.getAutopark() != null;
            return shiftService.getShiftList(dispatcher.getContractor().getId(), dispatcher.getAutopark().getId(), startDate);
        }
    }

    @Override
    public List<FirstTitleResponseDto> getEwbShifts(FirstTitleRequestDto dto, Authentication authentication) {
        var dispatcherId = UUID.fromString(((JwtAuthenticationToken)(authentication)).getToken().getId());
        var shifts = shiftService.findEwbShifts(dto.getShiftIds());
        shifts.forEach(s -> {
            s.setDispatcherId(dispatcherId);
            s.setStartDate(s.getStartDate().withOffsetSameInstant(ZoneOffset.of(dto.getTimeZone())));
            s.setFinishDate(s.getFinishDate().withOffsetSameInstant(ZoneOffset.of(dto.getTimeZone())));
        });
        var firstTitleList = ewbGrpcService.send(shifts);
        ewbUpdateService.addEwbId(firstTitleList);
        return firstTitleList;
    }

    @Override
    public void signEwbDocument(List<SignEwbRequestDto> signRequests, Authentication authentication) {
        var dispatcherId = UUID.fromString(((JwtAuthenticationToken)(authentication)).getToken().getId());
        var shiftsData = shiftService.findEwbShifts(signRequests.stream().map(SignEwbRequestDto::getId).toList());
        signRequests.forEach(signRequest -> {
            var shift = shiftsData.stream().filter(s -> s.getId().equals(signRequest.getId())).findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(Shift.class, signRequest.getId()));
            signRequest.setStartDate(shift.getStartDate().withOffsetSameInstant(ZoneOffset.of(signRequest.getTimeZone())));
            signRequest.setFinishDate(shift.getFinishDate().withOffsetSameInstant(ZoneOffset.of(signRequest.getTimeZone())));
            signRequest.setTransportationType(shift.getTransportationType());
            signRequest.setCommunicationType(shift.getCommunicationType());
            signRequest.setTariffDepartmentId(shift.getTariffDepartmentId());
            signRequest.setTransportId(shift.getTransportId());
            signRequest.setDriverId(shift.getDriverId());
            signRequest.setUserId(dispatcherId);
        });
        shiftSender.sendForEwb(signRequests);
    }

    @Override
    public void sendSocketForEwb(EwbMessage message) {
        var shiftOpt = shiftService.get(message.id());
        if(shiftOpt.isPresent()){
            var shift = shiftOpt.get();
            boolean success = true;
            if(message.errorText() != null && !message.errorText().isEmpty()){
                shift.setEwbId(null);
                shiftService.save(shift, Source.CONTRACTOR);
                success = false;
            }
            shiftSender.sendBySocket(shift, message.errorText(), success);
        }
    }

    @SuppressWarnings("unchecked")
    private Dispatcher dispatcherAuthCheck(UUID contractorId, Authentication authentication) {
        var jwtAuthenticationToken = (JwtAuthenticationToken) authentication;
        var roles = (Collection<? extends String>) jwtAuthenticationToken.getToken().getClaims().get("roles");
        if(roles.contains(dispatcherRoomAdminRole) || roles.contains(federalDispatcherRole)) {
            return null;
        }
        var userId = UUID.fromString((jwtAuthenticationToken).getToken().getId());
        return dispatcherService.get(contractorId, userId)
                .orElseGet(() -> dispatcherService.findByOauthIdAndContractorId(userId, contractorId)
                        .orElseThrow(() -> new UnauthorizedException("Ошибка авторизации")));
    }

    private List<ShiftDTO> validateAndSave(UUID contractorId, Dispatcher dispatcher, List<ShiftDTO> shiftDTOList, boolean newShift) {
        var resultList = new ArrayList<>(shiftDTOList);
        shiftDTOList.stream()
                .filter(shiftDTO -> validate(resultList, shiftDTO, contractorId, dispatcher, newShift))
                .forEach(shiftDTO -> save(resultList, shiftDTO, contractorId));
        return resultList;
    }

    private boolean validate(List<ShiftDTO> resultList, ShiftDTO shiftDTO, UUID contractorId, Dispatcher dispatcher, boolean newShift) {
        return checkValidation(resultList, shiftDTO, newShift)
                && checkStartEndDate(resultList, shiftDTO)
                && checkDriver(resultList, shiftDTO, dispatcher, newShift)
                && checkAutoparks(resultList, shiftDTO, contractorId, dispatcher, newShift)
                && checkVehicleShift(resultList, shiftDTO, newShift)
                && checkDriverAndVehicleAutoparkMatch(resultList, shiftDTO, newShift);
    }

    private void save(List<ShiftDTO> resultList, ShiftDTO shiftDTO, UUID contractorId) {
        var shift = new Shift();
        try {
            if(shiftDTO.getRouteId() != null && !shiftDTO.getRouteId().isEmpty()){
                shiftConflictService.delete(shiftDTO.getRouteId());
            }
            shift = shiftService.save(shiftMapper.toModel(shiftDTO, contractorId), Source.CONTRACTOR);
        } catch (Exception e) {
            addProblem(Collections.emptyList(), resultList, shiftDTO, e.getMessage(), null, e.getClass().getName(), "", "");
            return;
        }
        shiftDTO.setId(shift.getId());
        updateResultList(resultList, shiftDTO, shiftDTO);
    }

    private boolean checkValidation(List<ShiftDTO> resultList, ShiftDTO shiftDTO, boolean newShift) {
        if (newShift) {
            var validation = Validation.buildDefaultValidatorFactory();
            var validationResult = validation.getValidator().validate(shiftDTO);
            if (!validationResult.isEmpty()) {
                updateResultList(resultList, shiftDTO, ShiftViolationsDto.builder().body(
                        ConstraintViolationExceptionConverter.convertToExceptionBody(validationResult)
                ).index(shiftDTO.getIndex()).build());
                return false;
            }
        }
        return true;
    }

    private boolean checkStartEndDate(List<ShiftDTO> resultList, ShiftDTO shiftDTO) {
        if (shiftDTO.getStartDate().isAfter(shiftDTO.getEndDate())) {
            addProblem(Collections.emptyList(), resultList, shiftDTO, "Дата начала смены должна быть раньше даты окончания!",
                    shiftDTO.getVehicleId(), SHIFT_OBJECT_NAME, START_DATE_END_DATE,
                    shiftDTO.getStartDate() + "-" + shiftDTO.getEndDate());
            return false;
        }
        return true;
    }

    private boolean checkDriver(List<ShiftDTO> resultList, ShiftDTO shiftDTO, Dispatcher dispatcher, boolean newShift) {
        var driverId = shiftDTO.getDriverId();
        return (dispatcher == null || checkDriverDispatcherMatch(resultList, shiftDTO, driverId, dispatcher, newShift))
                && checkDriverShifts(resultList, shiftDTO, driverId, newShift);
    }

    private boolean checkDriverDispatcherMatch(List<ShiftDTO> resultList, ShiftDTO shiftDTO, UUID driverId, Dispatcher dispatcher, boolean newShift) {
        var driver = driverService.get(driverId).orElseThrow(() -> new EntityNotFoundException(Driver.class, driverId));
        if (!driver.getContractor().getId().equals(dispatcher.getContractor().getId())) {
            if (newShift) {
                addProblem(Collections.emptyList(), resultList, shiftDTO, "Водитель и диспетчер относятся к разным контрагентам!",
                        driver.getId(), "Driver", "driverId", String.valueOf(driver.getId()));
                return false;
            } else {
                throw new DataConflictException(Driver.class, DataConflictException.ConflictType.DRIVER_DISPATCHER_RELATION_CONFLICT,
                        driver.getContractor().getId(), "dispatcher.contractor.id", dispatcher.getContractor().getId(), Collections.emptyList());
            }
        }
        return true;
    }

    private boolean checkDriverShifts(List<ShiftDTO> resultList, ShiftDTO shiftDTO, UUID driverId, boolean newShift) {
        var existingDriversShifts = shiftService.findAllByDriverIdAndDate(driverId, shiftDTO.getStartDate(), shiftDTO.getEndDate());
        if (!newShift) {
            existingDriversShifts.removeIf(shift -> shift.getId().equals(shiftDTO.getId()));
        }
        if (!existingDriversShifts.isEmpty()) {
            if (newShift) {
                addProblem(existingDriversShifts.stream().map(Shift::getId).toList(), resultList, shiftDTO, "Даты начала смены пересекаются с существующей сменой водителя!",
                        null, SHIFT_OBJECT_NAME, START_DATE_END_DATE, shiftDTO.getStartDate() + "-" + shiftDTO.getEndDate());
                return false;
            } else {
                throw new DataConflictException(Shift.class, DataConflictException.ConflictType.DRIVER_SHIFT_DATE_CONFLICT,
                        shiftDTO.getId(), START_DATE_END_DATE, shiftDTO.getStartDate() + "-" + shiftDTO.getEndDate(), existingDriversShifts.stream().map(Shift::getId).toList());
            }
        }
        return true;
    }

    private boolean checkAutoparks(List<ShiftDTO> resultList, ShiftDTO shiftDTO, UUID contractorId, Dispatcher dispatcher, boolean newShift) {
        var autoparkCollection = autoparkService.getByContractorId(contractorId);
        var autoparkVehicleMatches = new AtomicInteger();
        autoparkVehicleMatches.set(0);
        autoparkCollection.forEach(autopark -> {
            var vehicleCollection = vehicleService.getAllByAutoparkAndActiveTrueAndInExploitation(autopark.getId());
            vehicleCollection.forEach(vehicle -> {
                if (vehicle.getId().equals(shiftDTO.getVehicleId())) {
                    autoparkVehicleMatches.getAndIncrement();
                }
            });
        });
        if (autoparkVehicleMatches.get() == 0) {
            if (newShift) {
                addProblem(Collections.emptyList(), resultList, shiftDTO, "Автомобиль не относится к контрагенту диспетчера или не активен!",
                        shiftDTO.getVehicleId(), "Vehicle", "vehicleId", String.valueOf(shiftDTO.getVehicleId()));
                return false;
            } else {
                throw new DataConflictException(Vehicle.class, DataConflictException.ConflictType.VEHICLE_DISPATCHER_RELATION_CONFLICT,
                        shiftDTO.getVehicleId(), "dispatcher.contractor.id", dispatcher == null ? null : dispatcher.getContractor().getId(), Collections.emptyList());
            }
        }
        return true;
    }

    private boolean checkVehicleShift(List<ShiftDTO> resultList, ShiftDTO shiftDTO, boolean newShift) {
        var existingVehicleShifts = shiftService.findAllByVehicleIdAndDate(shiftDTO.getVehicleId(),
                shiftDTO.getStartDate(), shiftDTO.getEndDate());
        if (!newShift) {
            existingVehicleShifts.removeIf(shift -> shift.getId().equals(shiftDTO.getId()));
        }
        if (!existingVehicleShifts.isEmpty()) {
            if (newShift) {
                addProblem(existingVehicleShifts.stream().map(Shift::getId).toList(), resultList, shiftDTO, "Даты начала смены пересекаются с существующей сменой автомобиля!",
                        null, SHIFT_OBJECT_NAME, START_DATE_END_DATE, shiftDTO.getStartDate() + "-" + shiftDTO.getEndDate());
                return false;
            } else {
                throw new DataConflictException(Shift.class, DataConflictException.ConflictType.VEHICLE_SHIFT_DATE_CONFLICT,
                        shiftDTO.getId(), START_DATE_END_DATE, shiftDTO.getStartDate() + "-" + shiftDTO.getEndDate(), existingVehicleShifts.stream().map(Shift::getId).toList());
            }
        }
        return true;
    }

    private boolean checkDriverAndVehicleAutoparkMatch(List<ShiftDTO> resultList, ShiftDTO shiftDTO, boolean newShift) {
        var driver = driverService.get(shiftDTO.getDriverId()).orElseThrow(() -> new EntityNotFoundException(Driver.class, shiftDTO.getDriverId()));
        var vehicle = vehicleService.findById(shiftDTO.getVehicleId()).orElseThrow(() -> new EntityNotFoundException(Vehicle.class, shiftDTO.getVehicleId()));
        UUID autoparkId = driver.getAutopark() != null ? driver.getAutopark().getId() : null;
        if (autoparkId != null && !autoparkId.equals(vehicle.getAutopark().getId())) {
            if (newShift) {
                addProblem(List.of(driver.getId()), resultList, shiftDTO, "Водитель и автомобиль относятся к разным филиалам!",
                        null, SHIFT_OBJECT_NAME, "driver-vehicle autoparkId", "vehicle.autoparkId - " + vehicle.getAutopark().getId()
                                + "driver.autoparkId - " + autoparkId);
                return false;
            } else {
                throw new DataConflictException(Shift.class, DataConflictException.ConflictType.DRIVER_AND_VEHICLE_AUTOPARK_MATCH,
                        shiftDTO.getId(),
                        "driver-vehicle autoparkId",
                        "vehicle.autoparkId - " + vehicle.getAutopark().getId() + "driver.autoparkId - " + autoparkId,
                        List.of(shiftDTO.getId()));
            }
        } return true;
    }

    private void addProblem(List<UUID> conflictedEntitiesIds, List<ShiftDTO> resultList, ShiftDTO shiftDTO, String message, UUID id, String objectName, String problemName, String problemValue) {
        var shiftConflictBuilder = ShiftConflictDTO.builder();
        shiftConflictBuilder.message(message).entity(new ConflictEntityDTO(id, objectName));
        var problemsList = new ArrayList<ProblemDTO>();
        problemsList.add(new ProblemDTO(conflictedEntitiesIds, problemName, problemValue));
        shiftConflictBuilder.problems(problemsList);
        updateResultList(resultList, shiftDTO, shiftConflictBuilder.index(shiftDTO.getIndex()).build());
    }

    private void updateResultList(List<ShiftDTO> resultList, ShiftDTO shiftDTO, ShiftDTO value) {
        resultList.set(resultList.indexOf(shiftDTO), value);
    }
}
