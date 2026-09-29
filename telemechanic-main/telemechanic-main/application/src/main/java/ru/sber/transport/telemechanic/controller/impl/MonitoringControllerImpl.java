package ru.sber.transport.telemechanic.controller.impl;

import io.grpc.StatusRuntimeException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.telemechanic.controller.MonitoringController;
import ru.sber.transport.telemechanic.database.dao.CheckPhotoRepository;
import ru.sber.transport.telemechanic.dto.*;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.helper.UserAuthorizationHelper;
import ru.sber.transport.telemechanic.service.EwbService;
import ru.sber.transport.telemechanic.service.FileService;
import ru.sber.transport.telemechanic.service.RequestService;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@E2EController
public class MonitoringControllerImpl implements MonitoringController {
    private final RequestService requestService;
    private final EwbService ewbService;
    private final FileService fileService;
    private final CheckPhotoRepository checkPhotoRepository;
    
    @Override
    @Transactional
    public Page<MonitoringRequestListDto> searchRequests(RequestSearchDto requestSearchDTO, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return requestService.search(requestSearchDTO, userId);
    }
    
    @Override
    @Transactional
    public PatchMonitoringResponse edit(UUID requestId, MonitoringRequestDto newData, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return requestService.update(requestId, newData, userId);
    }
    
    @Override
    public ResponseEntity<Void> declineEwb(UUID requestId, DeclinedTelemechRequest request, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        ewbService.declineEwb(requestId, request, userId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    @Transactional
    public MonitoringRequestDto get(UUID requestId, @E2EUser("principal") Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return requestService.getForMonitoring(requestId, userId);
    }
    
    @Override
    @Transactional
    public ResponseEntity<byte[]> downloadPhoto(UUID photoId) {
        //TODO add role check, move to service
        var checkPhoto = checkPhotoRepository.findById(photoId);
        if (checkPhoto.isPresent() && checkPhoto.get().getFileStatus().equals(FileStatus.UPLOADED)) {
            try {
                var fileData = fileService.get(photoId.toString());
                return ResponseEntity.ok().contentType(MediaType.valueOf(fileData.contentType())).body(fileData.stream());
            } catch (FileNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } catch (InvalidMediaTypeException e) {
                log.info("Не удалось получить расширение файла из хранилища s3. PhotoId={}", photoId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } catch (EntityNotFoundException | StatusRuntimeException | IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage().getBytes());
            }
        }
        log.info("Фото не было загружено в хранилище s3. PhotoId={}", photoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
