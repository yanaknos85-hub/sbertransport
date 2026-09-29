package ru.sber.transport.telemechanic.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.controller.RequestController;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyRequest;
import ru.sber.transport.telemechanic.dto.ewb.ChecksTreeDto;
import ru.sber.transport.telemechanic.dto.request.ActiveResponse;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.exception.ActiveRequestFound;
import ru.sber.transport.telemechanic.helper.CheckHelper;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.mapper.CheckMapper;
import ru.sber.transport.telemechanic.mapper.RequestMapper;
import ru.sber.transport.telemechanic.service.CheckService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.RequestService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@E2EController
public class RequestControllerImpl implements RequestController {
    private final RequestService requestService;
    private final CheckService checkService;
    private final EmployeeService employeeService;
    private final CheckMapper checkMapper;
    private final RequestMapper requestMapper;
    private final CheckHelper checkHelper;

    @Override
    @Transactional
    public CreatedRequestDto create(CreateRequestDto createRequestDto, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        var requestList = requestService.getInStatusByAuthor(authenticatedEmployee, Collections.singletonList(RequestStatus.IN_PROGRESS));
        if (Objects.nonNull(requestList) && !requestList.isEmpty()) {
            throw new ActiveRequestFound();
        } else {
            var request = requestService.createEmpty(createRequestDto, authenticatedEmployee, false);
            var requestDto = requestMapper.requestToCreatedRequestDto(request);
            var checks = checkHelper.createChecksTree(request.getChecks());
            return requestDto.withChecks(checks);
        }
    }

    @Override
    @Transactional
    public ChecksTreeDto get(UUID requestId) {
        return requestService.getChecksTreeByRequestId(requestId);
    }

    @Override
    @Transactional
    public RequestDto changeStatus(UUID requestId, RequestStatus newStatus, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        return requestMapper.requestToRequestDto(requestService.changeStatus(requestId, newStatus, authenticatedEmployee));
    }
    
    @Override
    public ResponseEntity<Void> changeStatusForCallTelemechanic(UUID requestId, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        requestService.changeStatusForCallTelemechanic(requestId, userId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    @Transactional
    public List<CheckDto> getCheck(UUID requestId, CheckType checkType) {
        return checkMapper.listCheckToListCheckDto(checkService.get(requestId, checkType));
    }
    
    @Override
    public CheckResponse doCheck(UUID requestId,
                                 CheckType checkType,
                                 MultipartFile[] files,
                                 CheckSafetyRequest request,
                                 @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return checkService.doCheck(requestId, checkType, files, request, userId);
    }
    
    @Override
    @Transactional
    public ActiveResponse getInProgress(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return requestService.getInProgressRequest(userId);
    }

    @Override
    @Transactional
    public RequestOnTheLineDto getOnTheLine(@E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        return requestService.getOnTheLineRequest(authenticatedEmployee);
    }

    @Override
    @Transactional
    public void cancel(UUID requestId, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        var authenticatedEmployee = employeeService.getByUserId(userId);
        requestService.cancel(requestId, authenticatedEmployee);
    }

    @Override
    @Transactional
    public List<RequestHistoryDto> getStatusHistory(UUID requestId) {
        var dtoList = requestService.getStatusHistory(requestId).stream()
                .map(requestMapper::requestHistoryToRequestHistoryDto)
                .collect(Collectors.toCollection(LinkedList::new));
        if (!dtoList.isEmpty()) {
            dtoList.getLast().setType(RequestHistoryDto.TypeEnum.NOW);
        }
        return dtoList;
    }
    
    @Override
    public void close(UUID requestId, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        requestService.close(requestId, userId);
    }
}
