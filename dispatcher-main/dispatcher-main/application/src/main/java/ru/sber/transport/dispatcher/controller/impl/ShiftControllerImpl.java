package ru.sber.transport.dispatcher.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ShiftSearchDto;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.service.ShiftControllerService;
import ru.sber.transport.dispatcher.controller.ShiftController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class ShiftControllerImpl implements ShiftController {

    private final ShiftControllerService shiftControllerService;

    @Override
    public List<ShiftDTO> addShift(UUID contractorId, Authentication authentication, List<ShiftDTO> shiftDTOList) {
        return shiftControllerService.addShift(contractorId, authentication, shiftDTOList);
    }

    @Override
    public Page<ShiftResponseDTO> getShifts(UUID contractorId, ShiftSearchDto shiftSearchDto, Authentication authentication) {
        return shiftControllerService.getShifts(contractorId,shiftSearchDto, authentication);
    }

    @Override
    public Page<VehicleShiftResponse> getVehicleShifts(UUID contractorId, VehicleShiftSearchDto searchDto, Authentication authentication) {
        return shiftControllerService.getVehicleShifts(contractorId, searchDto, authentication);
    }

    @Override
    public List<VehicleShiftStatusResponse> getVehicleShiftsStatus(UUID contractorId, VehicleShiftsStatusDto vehicleShiftsStatusDto, Authentication authentication) {
        return shiftControllerService.getVehicleShiftStatus(contractorId, vehicleShiftsStatusDto.getVehicleIds(), authentication);
    }

    @Override
    public void editShift(UUID contractorId, UUID shiftId, Authentication authentication, ShiftDTO shiftDTO) {
        shiftControllerService.editShift(contractorId, shiftId, authentication, shiftDTO);
    }

    @Override
    public ShiftResponseDTO getShift(UUID contractorId, UUID shiftId, Authentication authentication) {
        return shiftControllerService.getShift(contractorId, shiftId, authentication);
    }

    @Override
    public void deleteShift(UUID contractorId, UUID shiftId, Authentication authentication) {
        shiftControllerService.deleteShift(contractorId, shiftId, authentication);
    }

    @Override
    public void deleteRowOfShifts(UUID contractorId, UUID rowId, Authentication authentication, DeleteShiftRowDTO dto) {
        shiftControllerService.deleteShiftRow(contractorId, rowId, authentication, dto);
    }

    @Override
    public List<FirstTitleResponseDto> getEwbShifts(@Valid FirstTitleRequestDto dto, Authentication authentication) {
        return shiftControllerService.getEwbShifts(dto, authentication);
    }

    @Override
    public List<ShiftListResponseDto> getShiftList(OffsetDateTime startDate, Authentication authentication) {
        return shiftControllerService.getShiftList(startDate, authentication);
    }

    @Override
    public void signEwbDocument(List<SignEwbRequestDto> signRequests, Authentication authentication) {
        shiftControllerService.signEwbDocument(signRequests, authentication);
    }
}
