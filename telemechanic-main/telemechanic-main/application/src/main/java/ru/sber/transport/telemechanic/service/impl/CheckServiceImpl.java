package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.config.properties.PredictProperties;
import ru.sber.transport.telemechanic.database.dao.CheckRepository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyDto;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyRequest;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.exception.CheckNotFoundException;
import ru.sber.transport.telemechanic.exception.CheckPhotoCountException;
import ru.sber.transport.telemechanic.exception.IncompleteListOfChecksException;
import ru.sber.transport.telemechanic.service.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static org.apache.hc.core5.http.ContentType.APPLICATION_OCTET_STREAM;

@RequiredArgsConstructor
@Service
@Slf4j
public class CheckServiceImpl implements CheckService {
    private static final String START_DO_CHECK_MESSAGE = "start doCheck, requestId:{}, checkType:{}";
    private static final String START_SAVE_PHOTO_MESSAGE = "start savePhoto, checkType:{}";
    private static final String FINISH_SAVE_PHOTO_MESSAGE = "finish savePhoto, checkType:{}";
    private static final String FINISH_DO_CHECK_MESSAGE = "finish doCheck, requestId:{}";
    private final CheckRepository checkRepository;
    private final PredictService predictService;
    private final CheckPhotoService checkPhotoService;
    private final FileService fileService;
    private final PredictProperties properties;
    private final TransactionTemplate transactionTemplate;
    private final EmployeeService employeeService;
    private final EwbPathService ewbPathService;
    private final RequestService requestService;
    
    @Override
    @Transactional
    public List<Check> get(UUID requestId, CheckType checkType) {
        return getChecksTree(requestId, checkRepository.findByRequestIdAndCheckType(requestId, checkType)
                                                       .orElseThrow(() -> new CheckNotFoundException(requestId, checkType)));
    }
    
    @Override
    public CheckResponse doCheck(UUID requestId, CheckType checkType, MultipartFile[] files, CheckSafetyRequest request, UUID userId) {
        log.info(START_DO_CHECK_MESSAGE, requestId, checkType.name());
        var check = checkPhotoService.validateCheck(requestId, checkType, userId);
        var driver = employeeService.getByUserId(userId);
        var ewbPath = ewbPathService.calculatingClientPath(driver);
        switch (checkType) {
            case SAFETY -> {
                return saveSafetyCheck(requestId, check, request, ewbPath);
            }
            case BODY_DAMAGE -> {
                return saveBodyDamageCheck(requestId, check, files, ewbPath);
            }
            case ODOMETER -> throw new UnsupportedOperationException("Проверка одометра не поддерживается.");
            default -> {
                var response = saveCheckWithPhoto(requestId, check, files, ewbPath);
                requestService.setChecksStartedTime(requestId);
                return response;
            }
        }
    }
    
    private CheckResponse saveCheckWithPhoto(UUID requestId, Check check, MultipartFile[] files, boolean ewbPath) {
        if (files.length != 1) {
            throw new CheckPhotoCountException();
        }
        var predict = predictService.predictPhoto(files[0], check);
        savePhoto(files[0], check);
        var updatedCheck = checkPhotoService.saveCheck(predict, check);
        return getNextCheck(requestId, updatedCheck.getCheckStatus(), ewbPath);
    }
    
    private CheckResponse saveBodyDamageCheck(UUID requestId, Check check, MultipartFile[] files, boolean ewbPath) {
        isValidBodyDamageRequest(files);
        if (files != null) {
            stream(files).forEach(file -> this.savePhoto(file, check));
        }
        var checkStatus = files == null ? CheckStatus.DONE : CheckStatus.DECLINE;
        saveCheck(check, checkStatus);
        return getNextCheck(requestId, checkStatus, ewbPath);
    }
    
    @SneakyThrows(IOException.class)
    private void savePhoto(MultipartFile file, Check check) {
        log.info(START_SAVE_PHOTO_MESSAGE, check.getCheckType().name());
        var checkPhoto = checkPhotoService.preUploadPhoto(check);
        if (properties.isNeedFileUpload()) {
            try (var inputStream = file.getInputStream()) {
                final var fileName = checkPhoto.getId().toString();
                fileService.upload(inputStream, fileName, APPLICATION_OCTET_STREAM.getMimeType());
                checkPhotoService.uploadPhoto(checkPhoto);
            }
        }
        log.info(FINISH_SAVE_PHOTO_MESSAGE, check.getCheckType().name());
    }
    
    private void isValidBodyDamageRequest(MultipartFile[] files) {
        if (files != null && files.length > 4) {
            throw new CheckPhotoCountException();
        }
    }
    
    private CheckResponse saveSafetyCheck(UUID requestId, Check check, CheckSafetyRequest request, boolean ewbPath) {
        isValidSafetyRequest(request.checks());
        var status = request.checks().stream()
                            .allMatch(safetyCheck -> safetyCheck.status().equals(CheckStatus.DONE))
                           ? CheckStatus.DONE
                           : CheckStatus.DECLINE;
        saveSafetySubChecks(requestId, request);
        saveCheck(check ,status);
        return getNextCheck(requestId, status, ewbPath);
    }
    
    private void saveSafetySubChecks(UUID requestId, CheckSafetyRequest request) {
        var checksMap = request.checks().stream()
                               .collect(Collectors.toMap(
                                       CheckSafetyDto::checkType,
                                       CheckSafetyDto::status,
                                       (existing, replacement) -> replacement
                                                        ));
        
        checkRepository.findAllByRequestId(requestId).forEach(check -> {
            var newStatus = checksMap.get(check.getCheckType());
            if (newStatus != null) {
                saveCheck(check, newStatus);
            }
        });
    }
    
    private void isValidSafetyRequest(List<CheckSafetyDto> safetyChecks) {
        var safetySubChecks = CheckType.getSafetySubChecks();
        var safetyChecksTypes = safetyChecks.stream()
                                            .map(CheckSafetyDto::checkType)
                                            .collect(Collectors.toSet());
        if (safetyChecks.size() != safetySubChecks.size() ||
            !safetyChecksTypes.containsAll(safetySubChecks) ||
            safetyChecks.stream().anyMatch(check -> check.status().equals(CheckStatus.IN_PROGRESS))
        ) {
            throw new IncompleteListOfChecksException();
        }
    }
    
    private void saveCheck(Check check, CheckStatus checkStatus) {
        transactionTemplate.executeWithoutResult(status -> {
            check.setAttempt(check.getAttempt() + 1);
            check.setCheckStatus(checkStatus);
            checkRepository.save(check);
        });
    }
    
    public CheckResponse getNextCheck(UUID requestId, CheckStatus checkStatus, boolean ewbPath) {
        var nextCheck = checkRepository.findAllByRequestId(requestId).stream()
                                       .filter(check -> check.getCheckType().getParent() == null)
                                       .filter(check -> check.getAttempt() == 0)
                                       .filter(check -> Objects.isNull(check.getCheckType().getInEwbPath()) ||
                                                        check.getCheckType().getInEwbPath().equals(ewbPath))
                                       .min(Comparator.comparing(Check::getCheckType))
                                       .map(Check::getCheckType)
                                       .orElse(null);
        log.info(FINISH_DO_CHECK_MESSAGE, requestId);
        return new CheckResponse(checkStatus, nextCheck);
    }
    
    @Override
    public Check changeCheckStatus(UUID requestId, CheckType checkType, CheckStatus status) {
        var check = checkRepository.findByRequestIdAndCheckType(requestId, checkType)
                                   .orElseThrow(() -> new CheckNotFoundException(requestId, checkType));
        check.setCheckStatus(status);
        check.setAttempt(check.getAttempt() + 1);
        checkRepository.save(check);
        return check;
    }

    private List<Check> getChecksTree(UUID requestId, Check check) {
        var checkType = check.getCheckType();
        if (checkType.getParent() != null || checkType.isMain()) {
            return checkRepository.findAllByRequestIdAndCheckTypeContains(requestId, CheckType.getParentCheckWithSubChecks(checkType));
        }
        return Collections.singletonList(check);
    }
}
