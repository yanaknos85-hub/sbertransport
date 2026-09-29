package ru.sber.transport.telemechanic.service.impl;

import ch.qos.logback.classic.Level;
import org.assertj.core.groups.Tuple;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.config.properties.CheckPhotoAutoDeletionProperties;
import ru.sber.transport.telemechanic.database.dao.CheckPhotoRepository;
import ru.sber.transport.telemechanic.database.dao.CheckRepository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.FileStatus;
import ru.sber.transport.telemechanic.exception.CheckInFinalStatusException;
import ru.sber.transport.telemechanic.exception.CheckNotFoundException;
import ru.sber.transport.telemechanic.exception.FirstCheckNotFinishStatusException;
import ru.sber.transport.telemechanic.exception.NotAuthorException;
import ru.sber.transport.telemechanic.service.FileService;

import java.time.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.telemechanic.enumerate.CheckStatus.*;
import static ru.sber.transport.telemechanic.enumerate.CheckType.OIL_LEVEL;

@ExtendWith(MockitoExtension.class)
class CheckPhotoServiceImplTest {
    
    @Mock
    private CheckRepository checkRepository;
    @Mock
    private CheckPhotoRepository checkPhotoRepository;
    @Mock
    private FileService fileService;
    @Mock
    private CheckPhotoAutoDeletionProperties autoDeletionProperties;
    @Mock
    protected Clock clock;
    @InjectMocks
    private CheckPhotoServiceImpl checkPhotoService;
    
    @Captor
    private ArgumentCaptor<CheckPhoto> checkPhotoCaptor;
    @Captor
    private ArgumentCaptor<Check> checkCaptor;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(CheckPhotoServiceImpl.class);
    
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    
    @Test
    @DisplayName("Валидация проверки")
    void validateCheck() {
        var requestId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var check = Check.builder()
                         .checkType(OIL_LEVEL)
                         .checkStatus(DONE)
                         .request(
                                 Request.builder()
                                        .author(
                                                Employee.builder()
                                                        .id(UUID.randomUUID())
                                                        .build()
                                               )
                                        .build()
                                 )
                         .build();
        
        doReturn(true).when(checkRepository)
                      .existsByRequestIdAndCheckTypeAndCheckStatus(requestId, CheckType.VEHICLE_NUMBER, IN_PROGRESS);
        assertThatExceptionOfType(FirstCheckNotFinishStatusException.class)
                .isThrownBy(() -> checkPhotoService.validateCheck(requestId, OIL_LEVEL, userId))
                .withMessage("Проверка на автомобильный номер не в финальном статусе");
        
        doReturn(false).when(checkRepository)
                       .existsByRequestIdAndCheckTypeAndCheckStatus(requestId, CheckType.VEHICLE_NUMBER, IN_PROGRESS);
        doReturn(Optional.empty()).when(checkRepository).findByRequestIdAndCheckType(requestId, OIL_LEVEL);
        assertThatExceptionOfType(CheckNotFoundException.class)
                .isThrownBy(() -> checkPhotoService.validateCheck(requestId, OIL_LEVEL, userId))
                .withMessage(String.format(CheckNotFoundException.MSG_FORMAT, requestId, OIL_LEVEL));
        
        doReturn(Optional.of(check)).when(checkRepository).findByRequestIdAndCheckType(requestId, OIL_LEVEL);
        assertThatExceptionOfType(CheckInFinalStatusException.class)
                .isThrownBy(() -> checkPhotoService.validateCheck(requestId, OIL_LEVEL, userId))
                .withMessage("Проверка находится в финальном статусе");
        
        doReturn(Optional.of(check.setCheckStatus(IN_PROGRESS))).when(checkRepository).findByRequestIdAndCheckType(requestId, OIL_LEVEL);
        assertThatExceptionOfType(NotAuthorException.class)
                .isThrownBy(() -> checkPhotoService.validateCheck(requestId, OIL_LEVEL, userId))
                .withMessage("Только автор заявки может проходить проверки");
        
        doReturn(Optional.of(check.setRequest(
                Request.builder()
                       .author(
                               Employee.builder()
                                       .id(userId)
                                       .build()
                              )
                       .build()
                                             ))).when(checkRepository).findByRequestIdAndCheckType(requestId, OIL_LEVEL);
        assertThat(checkPhotoService.validateCheck(requestId, OIL_LEVEL, userId)).isEqualTo(check);
    }
    
    @Test
    @DisplayName("Предзагрузка фото проверки")
    void preUploadPhoto() {
        var check = Instancio.create(Check.class);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(CheckPhoto.builder()
                         .id(UUID.randomUUID())
                         .checkId(check.getId())
                         .creationTime(LOCAL_DATE_TIME)
                         .fileStatus(FileStatus.NOT_UPLOADED)
                         .build()
                ).when(checkPhotoRepository).save(any(CheckPhoto.class));
        
        var result = checkPhotoService.preUploadPhoto(check);
        verify(checkPhotoRepository).save(checkPhotoCaptor.capture());
        assertThat(checkPhotoCaptor.getValue())
                .extracting(CheckPhoto::getCheckId, CheckPhoto::getCreationTime, CheckPhoto::getFileStatus)
                .containsExactly(result.getCheckId(), LocalDateTime.now(clock), FileStatus.NOT_UPLOADED);
    }
    
    @Test
    @DisplayName("Загрузка фото проверки")
    void uploadPhoto() {
        var checkPhoto = CheckPhoto.builder()
                                   .id(UUID.randomUUID())
                                   .checkId(UUID.randomUUID())
                                   .creationTime(LOCAL_DATE_TIME)
                                   .fileStatus(FileStatus.NOT_UPLOADED)
                                   .build();
        
        doReturn(checkPhoto).when(checkPhotoRepository).save(any(CheckPhoto.class));
        
        checkPhotoService.uploadPhoto(checkPhoto);
        
        verify(checkPhotoRepository).save(checkPhotoCaptor.capture());
        assertThat(checkPhotoCaptor.getValue())
                .extracting(CheckPhoto::getId, CheckPhoto::getCheckId, CheckPhoto::getCreationTime, CheckPhoto::getFileStatus)
                .containsExactly(checkPhoto.getId(), checkPhoto.getCheckId(), checkPhoto.getCreationTime(), FileStatus.UPLOADED);
    }
    
    @Test
    void deleteOutdatedPhotos() {
        var idList1 = Instancio.ofList(UUID.class)
                               .size(10)
                               .create();
        var idList2 = Instancio.ofList(UUID.class)
                               .size(5)
                               .create();
        var errorId = idList2.getFirst();
        var updateListId2 = new ArrayList<>(idList2);
        updateListId2.remove(errorId);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(Duration.ZERO).when(autoDeletionProperties).expirationDuration();
        doReturn(10).when(autoDeletionProperties).batchSize();
        doReturn(15).when(autoDeletionProperties).maxQuantity();
        doReturn(25, 0).when(checkPhotoRepository).countAllUploadedByCreationTime(LOCAL_DATE_TIME);
        doReturn(idList1).when(checkPhotoRepository).findAllUploadedIdByCreationTime(LOCAL_DATE_TIME, 10);
        doReturn(idList2).when(checkPhotoRepository).findAllUploadedIdByCreationTime(LOCAL_DATE_TIME, 5);
        doNothing().when(checkPhotoRepository).updateStatusesForBatchOfPhotos(idList1);
        doNothing().when(checkPhotoRepository).updateStatusesForBatchOfPhotos(updateListId2);
        for (var id : idList1) {
            doNothing().when(fileService).delete(id.toString());
        }
        for (var id : idList2) {
            if (id.equals(errorId)) {
                doThrow(JUnitException.class).when(fileService).delete(id.toString());
            } else {
                doNothing().when(fileService).delete(id.toString());
            }
        }
        
        checkPhotoService.deleteOutdatedPhotos();
        checkPhotoService.deleteOutdatedPhotos();
        
        verify(autoDeletionProperties, times(2)).expirationDuration();
        verify(autoDeletionProperties).batchSize();
        verify(autoDeletionProperties).maxQuantity();
        verify(checkPhotoRepository, times(2)).countAllUploadedByCreationTime(any(LocalDateTime.class));
        verify(checkPhotoRepository, times(2)).findAllUploadedIdByCreationTime(any(LocalDateTime.class), anyInt());
        verify(fileService, times(15)).delete(anyString());
        verify(checkPhotoRepository, times(2)).updateStatusesForBatchOfPhotos(anyList());
        
        LOGGING_EXTENSION.assertLogEvents(11, new Tuple[] {
                tuple(
                        Level.INFO,
                        "Deleting new batch of outdated photos",
                        false
                     ),
                tuple(
                        Level.INFO,
                        "Outdated photo count total:25",
                        false
                     ),
                tuple(
                        Level.INFO,
                        "Outdated photo count to delete:15",
                        false
                     ),
                
                tuple(
                        Level.DEBUG,
                        "Start delete photos in batch, batchSize:10",
                        false
                     ),
                tuple(
                        Level.DEBUG,
                        "10/10 photos in the batch successfully deleted.",
                        false
                     ),
                tuple(
                        Level.DEBUG,
                        "Start delete photos in batch, batchSize:5",
                        false
                     ),
                tuple(
                        Level.ERROR,
                        "Failed to delete file, fileName:%s".formatted(errorId.toString()),
                        false
                     ),
                tuple(
                        Level.DEBUG,
                        "4/5 photos in the batch successfully deleted.",
                        false
                     ),
                
                tuple(
                        Level.INFO,
                        "Deleted all 15 outdated photos in 2 batches",
                        false
                     ),
                tuple(
                        Level.INFO,
                        "Deleting new batch of outdated photos",
                        false
                     ),
                tuple(
                        Level.INFO,
                        "There are no outdated photos. End of job",
                        false
                     )
        });
    }
    
    @MethodSource
    @ParameterizedTest
    @DisplayName("Сохранение проверки")
    void saveCheck(boolean predict, CheckStatus checkStatus) {
        var check = Check.builder()
                .attempt(0)
                .checkStatus(IN_PROGRESS)
                .build();
        var result = checkPhotoService.saveCheck(predict, check);
        verify(checkRepository).save(checkCaptor.capture());
        assertThat(checkCaptor.getValue())
                .extracting(Check::getAttempt, Check::getCheckStatus)
                .containsExactly(result.getAttempt(), checkStatus);
    }
    
    private static Stream<Arguments> saveCheck() {
        return Stream.of(
                Arguments.of(
                        true,
                        DONE
                            ),
                Arguments.of(
                        false,
                        DECLINE
                            )
                        );
    }
}
