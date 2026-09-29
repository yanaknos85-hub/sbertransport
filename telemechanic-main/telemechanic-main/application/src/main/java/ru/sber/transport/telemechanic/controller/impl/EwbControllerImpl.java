package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.controller.EwbController;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.SecondTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.*;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleForm;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SendAndSaveTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleSendResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitlesRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.EwbService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class EwbControllerImpl implements EwbController {
    
    private final EwbService ewbService;
    
    @Override
    public TokenDto getToken() {
        return ewbService.auth();
    }
    
    @Override
    public UuidDto getUUID() {
        return ewbService.getUUID();
    }
    
    @Override
    public FirstTitleResponse generateFirstTitle(FirstTitleRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.generateFirstTitle(request, userId);
    }
    
    @Override
    public SecondTitleResponse generateSecondTitle(SecondTitleForm title, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.generateSecondTitle(title, userId);
    }
    
    @Override
    public TelemechOutTitleResponse generateTelemechOutTitles(
            EwbTitleType titleType,
            TelemechOutTitlesRequest request,
            @E2EUser("principal") Authentication authentication
                                                             ) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.generateTelemechOutTitles(titleType, request, userId);
    }
    
    @Override
    public FifthTitleResponse generateFifthTitle(FifthTitleRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.generateFifthTitle(request, userId);
    }
    
    @Override
    public Page<EwbSearchResponseDto> searchSelfOrganization(EwbSearchSelfOrganizationRequestDto request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.searchSelfOrganization(request, userId);
    }
    
    @Override
    public Page<EwbSearchResponseDto> searchAllOrganizations(EwbSearchAllOrganizationsRequestDto request) {
        return ewbService.searchAllOrganizations(request);
    }
    
    @Override
    public Page<EwbSearchResponseDto> getAll(EwbSearchDto request) {
        return ewbService.search(request);
    }
    
    @Override
    public GetEwbDto getEwb(UUID id) {
        return ewbService.getEwb(id);
    }
    
    @Override
    public ResponseEntity<Void> sendAndSaveFirstTitle(FirstTitleDto request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.sendAndSaveFirstTitle(request, userId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    public ResponseEntity<Void> sendAndSaveSecondTitle(SendAndSaveTitleRequest request, @E2EUser("principal") Authentication authentication) {
        var useId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.sendAndSaveSecondTitle(request, useId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    public TelemechOutTitleSendResponse sendAndSaveTelemechOutTitle(
            SendAndSaveTitleRequest request,
            EwbTitleType titleType,
            @E2EUser("principal") Authentication authentication
                                                                   ) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.sendAndSaveTelemechOutTitle(request, titleType, userId);
    }
    
    @Override
    public ResponseEntity<Void> sendAndSaveFifthTitle(SendAndSaveTitleRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.sendAndSaveFifthTitle(request, userId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    public GetEwbRequestDto getEwbRequest(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.getEwbRequest(userId);
    }
    
    @Override
    public ResponseEntity<Void> closeEwb(EwbCloseRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.closeEwb(request, userId);
        
        return ResponseEntity.ok().build();
    }
    
    @Override
    public CheckResponse addOdometerValue(OdometerValue request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.addOdometerValue(request, userId);
    }
    
    @Override
    public GetQrCodeResponse getQrCode(UUID ewbId, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.getQrCode(ewbId, userId);
    }
    
    @Override
    public Page<EwbRegistryResponse> getEwbRegistryForAllOrganizations(EwbRegistryAllOrganizationsRequest request) {
        return ewbService.searchRegistryForAllOrganizations(request);
    }
    
    @Override
    public Page<EwbRegistryResponse> getEwbRegistrySelfOrganization(EwbRegistrySelfOrganizationRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.searchRegistryForSelfOrganization(request, userId);
    }
    
    @Override
    public GetEwbDetailedDto getEwbDetailed(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return ewbService.getEwbDetailed(userId);
    }
    
    @Override
    public ResponseEntity<Void> cancelEwb(UUID id, EwbCancelRequestDto request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.cancelEwb(id, request, userId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    public CheckResponse litreageOut(EwbLitreageOutRequest request, @E2EUser("principal") Authentication authentication) {
        return ewbService.litreageOut(request, UserAuthorizationHelper.getUserId(authentication));
    }
}
