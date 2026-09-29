package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.telemechanic.config.properties.CheckPhotoAutoDeletionProperties;
import ru.sber.transport.telemechanic.database.dao.CheckPhotoRepository;
import ru.sber.transport.telemechanic.database.dao.CheckRepository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.exception.CheckInFinalStatusException;
import ru.sber.transport.telemechanic.exception.CheckNotFoundException;
import ru.sber.transport.telemechanic.exception.FirstCheckNotFinishStatusException;
import ru.sber.transport.telemechanic.exception.NotAuthorException;
import ru.sber.transport.telemechanic.service.CheckPhotoService;
import ru.sber.transport.telemechanic.service.FileService;
import ru.sberbank.ditsib.transport.logging.annotations.E2ELogging;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static ru.sber.transport.telemechanic.enumerate.FileStatus.UPLOADED;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckPhotoServiceImpl implements CheckPhotoService {
    
    private final CheckRepository checkRepository;
    private final CheckPhotoRepository checkPhotoRepository;
    private final CheckPhotoAutoDeletionProperties autoDeletionProps;
    private final FileService fileService;
    private final Clock clock;
    
    @Override
    @Transactional
    public Check validateCheck(UUID requestId, CheckType checkType, UUID authenticatedEmployeeId) {
        if (checkType != CheckType.VEHICLE_NUMBER
            && checkRepository.existsByRequestIdAndCheckTypeAndCheckStatus(requestId, CheckType.VEHICLE_NUMBER, CheckStatus.IN_PROGRESS)) {
            throw new FirstCheckNotFinishStatusException();
        }
        var check = checkRepository.findByRequestIdAndCheckType(requestId, checkType)
                                   .orElseThrow(() -> new CheckNotFoundException(requestId, checkType));
        if (check.getCheckStatus().isFinal()) {
            throw new CheckInFinalStatusException();
        }
        if (!check.getRequest().getAuthor().getId().equals(authenticatedEmployeeId)) {
            throw new NotAuthorException();
        }
        return check;
    }
    
    @Override
    @Transactional
    public CheckPhoto preUploadPhoto(Check check) {
        var checkPhoto = CheckPhoto.builder()
                                   .checkId(check.getId())
                                   .creationTime(LocalDateTime.now(clock))
                                   .fileStatus(FileStatus.NOT_UPLOADED)
                                   .build();
        
        return checkPhotoRepository.save(checkPhoto);
    }
    
    @Override
    @Transactional
    public void uploadPhoto(CheckPhoto checkPhoto) {
        checkPhoto.setFileStatus(UPLOADED);
        checkPhotoRepository.save(checkPhoto);
    }
    
    @Override
    public void deleteOutdatedPhotos() {
        log.info("Deleting new batch of outdated photos");
        final var thresholdDateTime = LocalDateTime.now(clock).minus(autoDeletionProps.expirationDuration());
        final var count = checkPhotoRepository.countAllUploadedByCreationTime(thresholdDateTime);
        if (count == 0) {
            log.info("There are no outdated photos. End of job");
        } else {
            log.info("Outdated photo count total:{}", count);
            final var batchSize = autoDeletionProps.batchSize();
            final var maxQuantity = autoDeletionProps.maxQuantity();
            final var maxCount = count > maxQuantity ? maxQuantity : count;
            log.info("Outdated photo count to delete:{}", maxCount);
            final var totalBatches = (int) Math.ceil((double) maxCount / batchSize);
            var currentQuantity = maxCount;
            for (var i = 0; i < totalBatches; i++) {
                var currentBatchSize = Math.min(currentQuantity, batchSize);
                sendPhotoIdsToDelete(thresholdDateTime, currentBatchSize);
                currentQuantity = currentQuantity - currentBatchSize;
            }
            log.info("Deleted all {} outdated photos in {} batches", maxCount, totalBatches);
        }
    }
    
    @Override
    @Transactional
    public Check saveCheck(boolean predict, Check check) {
        check.setAttempt(check.getAttempt() + 1);
        return predict
               ? changeCheckStatus(check, CheckStatus.DONE)
               : changeCheckStatus(check, CheckStatus.DECLINE);
    }
    
    private Check changeCheckStatus(Check check, CheckStatus newStatus) {
        check.setCheckStatus(newStatus);
        checkRepository.save(check);
        return check;
    }
    
    @E2ELogging
    private void sendPhotoIdsToDelete(LocalDateTime thresholdDateTime, int batchSize) {
        log.debug("Start delete photos in batch, batchSize:{}", batchSize);
        var photoIds = checkPhotoRepository.findAllUploadedIdByCreationTime(thresholdDateTime, batchSize);
        var successFullDeletedPhotoIds = new ArrayList<UUID>();
        for (var photoId : photoIds) {
            try {
                fileService.delete(photoId.toString());
                successFullDeletedPhotoIds.add(photoId);
            } catch (Exception e) {
                log.error("Failed to delete file, fileName:{}", photoId, e);
            }
        }
        if (!successFullDeletedPhotoIds.isEmpty()) {
            checkPhotoRepository.updateStatusesForBatchOfPhotos(successFullDeletedPhotoIds);
        }
        log.debug("{}/{} photos in the batch successfully deleted.", successFullDeletedPhotoIds.size(), photoIds.size());
    }
}