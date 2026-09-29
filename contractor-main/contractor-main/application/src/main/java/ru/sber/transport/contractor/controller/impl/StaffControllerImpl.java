package ru.sber.transport.contractor.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.contractor.controller.StaffController;
import ru.sber.transport.contractor.dto.PatchData;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;
import ru.sber.transport.contractor.dto.internal.CreateStaffDto;
import ru.sber.transport.contractor.dto.internal.GetStaffDto;
import ru.sber.transport.contractor.dto.internal.StaffDto;
import ru.sber.transport.contractor.service.InternalAutoParkService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StaffControllerImpl implements StaffController {

    private final InternalAutoParkService internalAutoParkService;

    @Override
    public void createStaff(JwtAuthenticationToken token, CreateStaffDto createStaffDTO, String authorizationHeader) {
        internalAutoParkService.createStaff(token, createStaffDTO, authorizationHeader);
    }

    @Override
    public Page<StaffDto> getStaff(JwtAuthenticationToken token, GetStaffDto getStaffDto, String authorizationHeader) {
        return internalAutoParkService.getStaff(token, getStaffDto, authorizationHeader);
    }

    @Override
    public void deleteStaff(JwtAuthenticationToken token, GetStaffDto.Speciality speciality, UUID organizationId, UUID externalId, String authorizationHeader) {
        internalAutoParkService.deleteStaff(token, speciality, organizationId, externalId, authorizationHeader);
    }

    @Override
    public void patchStaff(JwtAuthenticationToken token, UUID externalId, List<PatchData> data, StaffSpeciality speciality, String authorizationHeader) {
        internalAutoParkService.patchStaff(token, externalId, data, speciality, authorizationHeader);
    }
}
