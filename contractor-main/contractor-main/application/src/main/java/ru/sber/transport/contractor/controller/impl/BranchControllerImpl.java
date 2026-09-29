package ru.sber.transport.contractor.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.contractor.controller.BranchController;
import ru.sber.transport.contractor.dto.internal.BranchResponseDto;
import ru.sber.transport.contractor.dto.internal.CreateBranchDto;
import ru.sber.transport.contractor.dto.internal.GetBranchDto;
import ru.sber.transport.contractor.service.InternalAutoParkService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BranchControllerImpl implements BranchController {

    private final InternalAutoParkService internalAutoParkService;

    @Override
    public void createBranch(JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorizationHeader) {
        internalAutoParkService.createBranch(token, createBranchDto, authorizationHeader);
    }

    @Override
    public void updateBranch(UUID externalId, JwtAuthenticationToken token, CreateBranchDto createBranchDto, String authorizationHeader) {
        internalAutoParkService.updateBranch(externalId, token, createBranchDto, authorizationHeader);
    }

    @Override
    public void deleteBranch(UUID externalId, JwtAuthenticationToken token, UUID organizationId, String authorizationHeader) {
        internalAutoParkService.deleteBranch(externalId, token, organizationId, authorizationHeader);
    }

    @Override
    public Page<BranchResponseDto> getAllBranches(JwtAuthenticationToken token, GetBranchDto getBranchDto, String authorizationHeader) {
        return internalAutoParkService.getBranches(token, getBranchDto, authorizationHeader);
    }

}
