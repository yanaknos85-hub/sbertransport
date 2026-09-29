package ru.sber.transport.telemechanic.service.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.config.properties.PredictProperties;
import ru.sber.transport.telemechanic.database.dao.CheckPhotoRepository;
import ru.sber.transport.telemechanic.database.dao.CheckRepository;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyDto;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyRequest;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.exception.CheckPhotoCountException;
import ru.sber.transport.telemechanic.exception.IncompleteListOfChecksException;
import ru.sber.transport.telemechanic.mapper.EwbMapper;
import ru.sber.transport.telemechanic.mapper.RequestMapper;
import ru.sber.transport.telemechanic.service.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Проверка сервиса проверок")
class CheckServiceImplTest {
    
    @InjectMocks
    private CheckServiceImpl checkService;
    @Mock
    private CheckRepository checkRepository;
    @Mock
    private PredictService predictService;
    @Mock
    private FileService fileService;
    @Mock
    private CheckPhotoRepository checkPhotoRepository;
    @Mock
    private Clock clock;
    @Mock
    private PredictProperties properties;
    @Mock
    private CheckPhotoService checkPhotoService;
    @Mock
    private EwbService ewbService;
    @Mock
    private EwbMapper ewbMapper;
    @Mock
    private EwbPathService ewbPathService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private RequestService requestService;
    @Mock
    private RequestMapper requestMapper;
    @Mock
    private TransactionTemplate transactionTemplate;
    
    private static final LocalDateTime LOCAL_DATE = LocalDateTime.of(2022, 11, 30, 10, 11);
    
    @Captor
    private ArgumentCaptor<Check> checkCaptor;
    
    @BeforeEach
    void initMocks() {
        MockitoAnnotations.openMocks(this);
        var fixedClock = Clock.fixed(LOCAL_DATE.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
    }
    
    
    @Test
    void get() {
        var check = Check.builder()
                         .checkType(CheckType.OIL_LEVEL)
                         .checkStatus(CheckStatus.IN_PROGRESS)
                         .attempt(0)
                         .request(Request.builder()
                                         .id(UUID.randomUUID())
                                         .build())
                         .build();
        var requestId = check.getRequest().getId();
        when(checkRepository.findByRequestIdAndCheckType(requestId, check.getCheckType()))
                .thenReturn(Optional.of(check));
        var actual = checkService.get(requestId, check.getCheckType());
        assertEquals(actual.getFirst().getId(), check.getId());
        assertEquals(actual.getFirst().getCheckType(), check.getCheckType());
        assertEquals(actual.getFirst().getAttempt(), check.getAttempt());
        assertEquals(actual.getFirst().getCheckStatus(), check.getCheckStatus());
        assertEquals(actual.getFirst().getRequest().getId(), check.getRequest().getId());
        
        var safetyCheck = Check.builder()
                               .checkType(CheckType.SAFETY)
                               .attempt(0)
                               .request(check.getRequest())
                               .build();
        var listOfChecks = safetySubChecks();
        
        doReturn(Optional.of(safetyCheck)).when(checkRepository).findByRequestIdAndCheckType(requestId, CheckType.SAFETY);
        doReturn(safetySubChecks()).when(checkRepository).findAllByRequestIdAndCheckTypeContains(requestId,
                                                                                                 CheckType.getParentCheckWithSubChecks(
                                                                                                         CheckType.SAFETY));
        actual = checkService.get(requestId, safetyCheck.getCheckType());
        assertEquals(listOfChecks.size(), actual.size());
        assertTrue(listOfChecks.containsAll(actual));
    }
    
    private List<Check> safetySubChecks() {
        return List.of(
                Check.builder().checkType(CheckType.BRAKE_SYSTEM).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
                Check.builder().checkType(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.STEERING).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.WHEELS_AND_TIRES).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.HORN).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.SATELLITE_NAVIGATION).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.BODY_LOCKS_FUEL_TANK_CAPS).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
                Check.builder().checkType(CheckType.WINDOW_HEATING_AND_DEFROSTER).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
                Check.builder().checkType(CheckType.TOW_HITCHES_AND_CABLES).attempt(1).checkStatus(CheckStatus.DECLINE).build(),
                Check.builder().checkType(CheckType.SPARE_WHEEL_HOLDER).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.SEAT_BELTS).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.EXHAUST_SYSTEM).attempt(1).checkStatus(CheckStatus.DONE).build(),
                Check.builder().checkType(CheckType.FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK).attempt(1).checkStatus(CheckStatus.DONE)
                     .build()
                      );
    }
    
    @ParameterizedTest
    @MethodSource
    @SneakyThrows
    void checkSafetyValidationFail(CheckSafetyRequest request) {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.SAFETY;
        var userId = UUID.randomUUID();
        var check = Check.builder()
                         .attempt(0)
                         .checkStatus(CheckStatus.IN_PROGRESS)
                         .build();
        
        doReturn(check).when(checkPhotoService).validateCheck(requestId, checkType, userId);
        
        assertThatExceptionOfType(IncompleteListOfChecksException.class)
                .isThrownBy(() -> checkService.doCheck(requestId, checkType, null, request, userId))
                .withMessage("Получен не полный список проверок безопасности.");
    }
    
    static Stream<Arguments> checkSafetyValidationFail() {
        return Stream.of(
                Arguments.of(
                        new CheckSafetyRequest(
                                List.of(
                                        new CheckSafetyDto(CheckType.BRAKE_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.STEERING, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WHEELS_AND_TIRES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SATELLITE_NAVIGATION, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.BODY_LOCKS_FUEL_TANK_CAPS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WINDOW_HEATING_AND_DEFROSTER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.TOW_HITCHES_AND_CABLES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SPARE_WHEEL_HOLDER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SEAT_BELTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.EXHAUST_SYSTEM, CheckStatus.DONE)
                                       )
                        )
                            ),
                Arguments.of(
                        new CheckSafetyRequest(
                                List.of(
                                        new CheckSafetyDto(CheckType.BRAKE_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.STEERING, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WHEELS_AND_TIRES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SATELLITE_NAVIGATION, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.BODY_LOCKS_FUEL_TANK_CAPS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WINDOW_HEATING_AND_DEFROSTER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.TOW_HITCHES_AND_CABLES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SPARE_WHEEL_HOLDER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SEAT_BELTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.EXHAUST_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE)
                                       )
                        )
                            ),
                Arguments.of(
                        new CheckSafetyRequest(
                                List.of(
                                        new CheckSafetyDto(CheckType.BRAKE_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.STEERING, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WHEELS_AND_TIRES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SATELLITE_NAVIGATION, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.BODY_LOCKS_FUEL_TANK_CAPS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WINDOW_HEATING_AND_DEFROSTER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.TOW_HITCHES_AND_CABLES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SPARE_WHEEL_HOLDER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SEAT_BELTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.EXHAUST_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK, CheckStatus.IN_PROGRESS)
                                       )
                        )
                            )
                        );
    }
    
    @ParameterizedTest
    @MethodSource
    @SneakyThrows
    void checkSafety(CheckSafetyRequest request, CheckStatus expectedStatus) {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.SAFETY;
        var userId = UUID.randomUUID();
        var user = Instancio.of(Employee.class)
                            .set(Select.field(Employee::getId), userId)
                            .create();
        var check = Check.builder()
                         .attempt(0)
                         .checkStatus(CheckStatus.IN_PROGRESS)
                         .build();
        
        doReturn(check).when(checkPhotoService).validateCheck(requestId, checkType, userId);
        
        doReturn(safetySubChecks()).when(checkRepository).findAllByRequestId(requestId);
        when(transactionTemplate.execute(any())).thenAnswer(invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0)
                                                                                                .doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(true).when(ewbPathService).calculatingClientPath(user);
        doReturn(
                List.of(
                        Check.builder()
                             .checkType(CheckType.ODOMETER)
                             .attempt(0)
                             .build()
                       )
                ).when(checkRepository).findAllByRequestId(requestId);
        
        var result = checkService.doCheck(requestId, checkType, null, request, userId);
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting(CheckResponse::checkStatus, CheckResponse::nextCheck)
                .containsExactly(expectedStatus, CheckType.ODOMETER);
    }
    
    static Stream<Arguments> checkSafety() {
        return Stream.of(
                Arguments.of(
                        new CheckSafetyRequest(
                                List.of(
                                        new CheckSafetyDto(CheckType.BRAKE_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.STEERING, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WHEELS_AND_TIRES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SATELLITE_NAVIGATION, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.BODY_LOCKS_FUEL_TANK_CAPS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WINDOW_HEATING_AND_DEFROSTER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.TOW_HITCHES_AND_CABLES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SPARE_WHEEL_HOLDER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SEAT_BELTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.EXHAUST_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK, CheckStatus.DONE)
                                       )
                        ),
                        CheckStatus.DONE
                            ),
                Arguments.of(
                        new CheckSafetyRequest(
                                List.of(
                                        new CheckSafetyDto(CheckType.BRAKE_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SIDE_LIGHTS_HIGH_BEAM_HEADLIGHTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.STEERING, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WHEELS_AND_TIRES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.HORN, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SATELLITE_NAVIGATION, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.BODY_LOCKS_FUEL_TANK_CAPS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.DRIVER_SEAT_CUSHION_AND_BACKREST, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.WINDOW_HEATING_AND_DEFROSTER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.TOW_HITCHES_AND_CABLES, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SPARE_WHEEL_HOLDER, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.SEAT_BELTS, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.EXHAUST_SYSTEM, CheckStatus.DONE),
                                        new CheckSafetyDto(CheckType.FIRST_AID_KIT_FIRE_EXTINGUISHER_ANTI_ROLLBACK, CheckStatus.DECLINE)
                                       )
                        ),
                        CheckStatus.DECLINE
                            )
                        );
    }
    
    @Test
    @SneakyThrows
    void checkBodyDamage() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.BODY_DAMAGE;
        var file = new MockMultipartFile(
                "file",
                "file.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test".getBytes()
        );
        var userId = UUID.randomUUID();
        var user = Instancio.of(Employee.class)
                            .set(Select.field(Employee::getId), userId)
                            .create();
        var check = Check.builder()
                         .attempt(0)
                         .checkType(CheckType.BODY_DAMAGE)
                         .checkStatus(CheckStatus.IN_PROGRESS)
                         .build();
        var checkPhoto = CheckPhoto.builder()
                                   .id(UUID.randomUUID())
                                   .build();
        
        doReturn(check).when(checkPhotoService).validateCheck(requestId, checkType, userId);
        
        assertThatExceptionOfType(CheckPhotoCountException.class)
                .isThrownBy(() -> checkService.doCheck(requestId, checkType, new MultipartFile[] { file, file, file, file, file }, null, userId))
                .withMessage("Тип проверки не поддерживает количество фото, которое было передано в запросе.");
        
        doReturn(checkPhoto).when(checkPhotoService).preUploadPhoto(check);
        doReturn(true).when(properties).isNeedFileUpload();
        doNothing().when(fileService).upload(any(), any(), any());
        doNothing().when(checkPhotoService).uploadPhoto(any());
        when(transactionTemplate.execute(any())).thenAnswer(invocationOnMock -> invocationOnMock.<TransactionCallback<Object>>getArgument(0)
                                                                                                .doInTransaction(null));
        doCallRealMethod().when(transactionTemplate).executeWithoutResult(any());
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(true).when(ewbPathService).calculatingClientPath(user);
        doReturn(
                List.of(
                        Check.builder()
                             .checkType(CheckType.ODOMETER)
                             .attempt(0)
                             .build()
                       )
                ).when(checkRepository).findAllByRequestId(requestId);
        
        var result = checkService.doCheck(requestId, checkType, new MultipartFile[] { file }, null, userId);
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting(CheckResponse::checkStatus, CheckResponse::nextCheck)
                .containsExactly(CheckStatus.DECLINE, CheckType.ODOMETER);
        
        verify(checkRepository).save(checkCaptor.capture());
        assertThat(checkCaptor.getValue().getCheckStatus()).isEqualTo(CheckStatus.DECLINE);
        
        var result2 = checkService.doCheck(requestId, checkType, null, null, userId);
        assertThat(result2).isNotNull();
        assertThat(result2)
                .extracting(CheckResponse::checkStatus, CheckResponse::nextCheck)
                .containsExactly(CheckStatus.DONE, CheckType.ODOMETER);
        
        verify(checkRepository, times(2)).save(checkCaptor.capture());
        assertThat(checkCaptor.getValue().getCheckStatus()).isEqualTo(CheckStatus.DONE);
        
    }
    
    @Test
    @SneakyThrows
    void checkOdometer() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.ODOMETER;
        var userId = UUID.randomUUID();
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> checkService.doCheck(requestId, checkType, null, null, userId))
                .withMessage("Проверка одометра не поддерживается.");
    }
    
    @Test
    @SneakyThrows
    void checkPhoto() {
        var requestId = UUID.randomUUID();
        var checkType = CheckType.VEHICLE_NUMBER;
        var file = new MockMultipartFile(
                "file",
                "file.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test".getBytes()
        );
        var userId = UUID.randomUUID();
        var user = Instancio.of(Employee.class)
                            .set(Select.field(Employee::getId), userId)
                            .create();
        var check = Check.builder()
                         .checkType(CheckType.VEHICLE_NUMBER)
                         .checkStatus(CheckStatus.IN_PROGRESS)
                         .build();
        var checkPhoto = CheckPhoto.builder()
                                   .id(UUID.randomUUID())
                                   .build();
        
        doReturn(check).when(checkPhotoService).validateCheck(requestId, checkType, userId);
        
        assertThatExceptionOfType(CheckPhotoCountException.class)
                .isThrownBy(() -> checkService.doCheck(requestId, checkType, new MultipartFile[] {}, null, userId))
                .withMessage("Тип проверки не поддерживает количество фото, которое было передано в запросе.");
        
        doReturn(true).when(predictService).predictPhoto(any(MultipartFile.class), any(Check.class));
        doReturn(checkPhoto).when(checkPhotoService).preUploadPhoto(check);
        doReturn(true).when(properties).isNeedFileUpload();
        doNothing().when(fileService).upload(any(), any(), any());
        doNothing().when(checkPhotoService).uploadPhoto(any());
        doReturn(check.setCheckStatus(CheckStatus.DONE)).when(checkPhotoService).saveCheck(true, check);
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(true).when(ewbPathService).calculatingClientPath(user);
        doReturn(
                List.of(
                        Check.builder()
                             .checkType(CheckType.ODOMETER)
                             .attempt(0)
                             .build()
                       )
                ).when(checkRepository).findAllByRequestId(requestId);
        
        var result = checkService.doCheck(requestId, checkType, new MultipartFile[] { file }, null, userId);
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting(CheckResponse::checkStatus, CheckResponse::nextCheck)
                .containsExactly(CheckStatus.DONE, CheckType.ODOMETER);
        
        doReturn(false).when(predictService).predictPhoto(any(MultipartFile.class), any(Check.class));
        doReturn(check.setCheckStatus(CheckStatus.DECLINE)).when(checkPhotoService).saveCheck(false, check);
        
        var result2 = checkService.doCheck(requestId, checkType, new MultipartFile[] { file }, null, userId);
        assertThat(result2).isNotNull();
        assertThat(result2)
                .extracting(CheckResponse::checkStatus, CheckResponse::nextCheck)
                .containsExactly(CheckStatus.DECLINE, CheckType.ODOMETER);
    }
    
    @Test
    void shouldChangeStatus() {
        doReturn(Optional.of(Instancio.create(Check.class))).when(checkRepository).findByRequestIdAndCheckType(any(UUID.class), any(CheckType.class));
        doReturn(Instancio.create(Check.class)).when(checkRepository).save(any(Check.class));
        
        assertDoesNotThrow(
                () -> checkService.changeCheckStatus(UUID.randomUUID(), Instancio.create(CheckType.class), Instancio.create(CheckStatus.class)));
    }
}