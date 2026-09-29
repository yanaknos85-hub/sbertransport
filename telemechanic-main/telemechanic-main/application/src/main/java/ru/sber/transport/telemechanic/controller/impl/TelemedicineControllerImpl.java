package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.controller.TelemedicineController;
import ru.sber.transport.telemechanic.dto.telemedicine.*;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.TelemedicineService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
public class TelemedicineControllerImpl implements TelemedicineController {
    
    private final TelemedicineService telemedicineService;
    
    @Override
    public Page<TelemedicineSearchResponse> search(TelemedicineSearchRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return telemedicineService.search(request, userId);
    }
    
    @Override
    public void create(UUID ewbId) {
        telemedicineService.create(ewbId);
    }
    
    @Override
    public GetTelemedicineDto getMedicRequest(UUID medicRequestId) {
        return telemedicineService.getMedicRequest(medicRequestId);
    }
    
    @Override
    public void decline(UUID medicRequestId, DeclinedTelemedicineRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        telemedicineService.decline(medicRequestId, request, userId);
    }
    
    @Override
    public void addResult(MultipartFile data, MultipartFile signature, TelemedicineResultRequest request, String apiKey) {
        telemedicineService.addResult(data, signature, request, apiKey);
    }
    
    @Override
    public void decline(UUID ewbUuid, TelemedicineResultRequest request, String apiKey) {
        telemedicineService.decline(ewbUuid, request, apiKey);
    }
}
