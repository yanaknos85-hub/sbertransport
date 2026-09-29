package ru.sber.transport.telemechanic.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.LoggingExtension;
import ru.sber.transport.telemechanic.config.properties.PredictProperties;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.predict.*;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
class PredictServiceImplTest {
    private MultipartFile file;
    private MultipartFile files;
    private Employee author;
    private final static String URI = "https://some-ai-host.sbrf.ru/";
    @InjectMocks
    private PredictServiceImpl predictService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private PredictProperties predictProperties;
    @Mock
    private ObjectMapper objectMapper;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(PredictServiceImpl.class);

    @BeforeEach
    public void initMocks() throws IOException {
        file = new MockMultipartFile("file", "1111.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                getClass().getClassLoader().getResourceAsStream("load/1111.jpg"));
        files = new MockMultipartFile("file", "bytes".getBytes());
        var organization = Instancio.create(Organization.class);
        var department = Instancio.create(Department.class);
        var position = Instancio.of(Position.class)
                                .set(field(Position::getOrganization), organization).create();
        author = Instancio.of(Employee.class)
                          .set(field(Employee::getOrganization), organization)
                          .set(field(Employee::getDepartment), department)
                          .set(field(Employee::getPosition), position)
                          .create();
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-plates, результат успех")
    void predictNumber() {
        var check = createCheck(CheckType.VEHICLE_NUMBER);
        var carNumber = new CarNumberDto("a010eК50", 111);
        var numbers = List.of(carNumber);
        var carPlateResponse = new CarPlateResponseDto(200, true, numbers);
        var url = URI + "car-plates/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedPlateNumber()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<CarPlateResponseDto>>any())).thenReturn(ResponseEntity.ok(carPlateResponse));
        assertTrue(predictService.predictPhoto(file, check));
    }

    @Test
    @DisplayName("Запрос к модели car-plates, не требуется запуск проверки моделью, результат успех")
    void predictNumberNoNeed() {
        var check = createCheck(CheckType.VEHICLE_NUMBER);
        when(predictProperties.isNeedPlateNumber()).thenReturn(false);
        verify(restTemplate, never()).postForEntity(any(String.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<CarPlateResponseDto>>any());
        assertTrue(predictService.predictPhoto(file, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-plates, ошибка модели")
    void predictNumberException() {
        var method = "car-plates";
        var check = createCheck(CheckType.VEHICLE_NUMBER);
        var url = URI + method + "/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedPlateNumber()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<CarPlateResponseDto>>any())).thenThrow(new RuntimeException("internal error"));
        assertFalse(predictService.predictPhoto(files, check));
        checkLogs(method, CheckType.VEHICLE_NUMBER.name());
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-plates, результат вернул пустой список detail")
    void predictNumberResponseDetailNull() {
        var check = createCheck(CheckType.VEHICLE_NUMBER);
        var url = URI + "car-plates/predict";
        var carPlateResponse = new CarPlateResponseDto(200, true, null);
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedPlateNumber()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<CarPlateResponseDto>>any())).thenReturn(ResponseEntity.ok(carPlateResponse));
        assertFalse(predictService.predictPhoto(files, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-plates, результат вернул пустой список detail")
    void predictNumberResponseDetailEmpty() {
        var check = createCheck(CheckType.VEHICLE_NUMBER);
        var url = URI + "car-plates/predict";
        var carPlateResponse = new CarPlateResponseDto(200, true, Collections.emptyList());
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedPlateNumber()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<CarPlateResponseDto>>any())).thenReturn(ResponseEntity.ok(carPlateResponse));
        assertFalse(predictService.predictPhoto(files, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели dashboard, результат успех ")
    void predictInstrumentalPanel() {
        var check = createCheck(CheckType.INSTRUMENT_PANEL);
        var errors = Collections.singletonList(new ErrorPredictDto(Collections.emptyList()));
        var responseDto = new InstrumentalPanelResponseDto(200, true, errors);
        var url = URI + "dashboard/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedInstrumentPanel()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<InstrumentalPanelResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto));
        assertTrue(predictService.predictPhoto(file, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели dashboard, результат не успех, логируем ошибки")
    void predictInstrumentalPanelHaveErrors() {
        var check = createCheck(CheckType.INSTRUMENT_PANEL);
        var errors = Collections.singletonList(new ErrorPredictDto(
                List.of("brake_error", "cooling_liquid_temp_warning")));
        var responseDto = new InstrumentalPanelResponseDto(200, false, errors);
        var url = URI + "dashboard/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedInstrumentPanel()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<InstrumentalPanelResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto));
        assertFalse(predictService.predictPhoto(file, check));
        assertThat(LOGGING_EXTENSION.getEvents())
                .hasSize(3)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getFormattedMessage
                           )
                .containsExactly(
                        tuple(
                                Level.INFO,
                                "start predict, checkType:%s".formatted(CheckType.INSTRUMENT_PANEL.name())
                             ),
                        tuple(
                                Level.INFO,
                                "Получен неуспешный ответ от модели dashboard:brake_error;cooling_liquid_temp_warning;"
                             ),
                        tuple(
                                Level.INFO,
                                "finish predict, checkType:%s".formatted(CheckType.INSTRUMENT_PANEL.name())
                             )
                                );
    }

    @Test
    @DisplayName("Запрос к модели dashboard, не требуется запуск проверки моделью, результат успех")
    void predictInstrumentalPanelNoNeed() {
        var check = createCheck(CheckType.INSTRUMENT_PANEL);
        when(predictProperties.isNeedInstrumentPanel()).thenReturn(false);
        verify(restTemplate, never()).postForEntity(any(String.class),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<InstrumentalPanelResponseDto>>any());
        assertTrue(predictService.predictPhoto(file, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели dashboard, ошибка модели")
    void predictInstrumentalPanelException() {
        var method = "dashboard";
        var check = createCheck(CheckType.INSTRUMENT_PANEL);
        var url = URI + method + "/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedInstrumentPanel()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<InstrumentalPanelResponseDto>>any())).thenThrow(new RuntimeException("internal error"));
        assertFalse(predictService.predictPhoto(files, check));
        checkLogs(method, CheckType.INSTRUMENT_PANEL.name());
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = CheckType.class,
            names = {"VEHICLE_NUMBER", "HEADLAMPS_LF", "HEADLAMPS_RF", "HEADLAMPS_LR", "HEADLAMPS_RR",
                    "SIDE_MIRRORS_L", "SIDE_MIRRORS_R", "SPLASH_GUARDS_LF", "SPLASH_GUARDS_LR", "SPLASH_GUARDS_RF",
                    "SPLASH_GUARDS_RR", "WIND_SCREEN", "POWER_STEERING_LIQUID_LEVEL", "COOLANT_LEVEL", "OIL_LEVEL",
                    "INSTRUMENT_PANEL", "WINDSHIELD_WIPERS_AND_LIQUID"},
            mode = EnumSource.Mode.EXCLUDE)
    @DisplayName("Запрос к моделям, нет реализации")
    void predictNotImplemented(CheckType checkType) {
        var check = createCheck(checkType);
        assertThrows(NotImplementedException.class, () -> predictService.predictPhoto(file, check));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = CheckType.class,
            names = {
                    "OIL_LEVEL", "POWER_STEERING_LIQUID_LEVEL", "COOLANT_LEVEL", "WIND_SCREEN", "SPLASH_GUARDS_LF", "SIDE_MIRRORS_L",
                    "SPLASH_GUARDS_LR", "HEADLAMPS_LR", "HEADLAMPS_RR", "SPLASH_GUARDS_RR", "SIDE_MIRRORS_R", "SPLASH_GUARDS_RF",
                    "HEADLAMPS_RF", "HEADLAMPS_LF"
            },
            mode = EnumSource.Mode.INCLUDE)
    @DisplayName("Запрос к моделям, не требуется запуск проверки моделью, результат успех")
    void predictOtherNoNeed(CheckType checkType) {
        var check = createCheck(checkType);
        if (!checkType.equals(CheckType.ODOMETER)) {
            var result = predictService.predictPhoto(files, check);
            assertTrue(result);
            verify(restTemplate, never()).postForEntity(any(String.class),
                                                        any(HttpEntity.class),
                                                        ArgumentMatchers.<Class<PredictResponseDto>>any());
        } else {
            assertThrows(NotImplementedException.class, () -> predictService.predictPhoto(file,check));
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к моделям car-wipers и washer-liquid, требуется запуск проверки моделью, результат успех")
    void predictCarWipersAndWasherLiquid() {
        when(predictProperties.isNeedCarWipers()).thenReturn(true);
        when(predictProperties.isNeedWasherLiquid()).thenReturn(true);
        var check = createCheck(CheckType.WINDSHIELD_WIPERS_AND_LIQUID);
        var responseDto1 = new PredictResponseDto(200, true, Collections.emptyList());
        var responseDto2 = new PredictResponseDto(200, true, Collections.emptyList());
        var url1 = URI + "car-wipers/predict";
        var url2 = URI + "washer-liquid/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedCarWipers()).thenReturn(true);
        when(predictProperties.isNeedWasherLiquid()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url1),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto1));
        when(restTemplate.postForEntity(eq(url2),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto2));
        assertTrue(predictService.predictPhoto(files, check));
    }

    @SneakyThrows
    @ParameterizedTest(name = "{0}")
    @EnumSource(
            value = CheckType.class,
            names = {
                    "OIL_LEVEL", "POWER_STEERING_LIQUID_LEVEL", "COOLANT_LEVEL", "WIND_SCREEN", "SPLASH_GUARDS_LF", "SIDE_MIRRORS_L",
                    "SPLASH_GUARDS_LR", "HEADLAMPS_LR", "HEADLAMPS_RR", "SPLASH_GUARDS_RR", "SIDE_MIRRORS_R", "SPLASH_GUARDS_RF",
                    "HEADLAMPS_RF", "HEADLAMPS_LF"
            },
            mode = EnumSource.Mode.INCLUDE)
    @DisplayName("Запрос к моделям, требуется запуск проверки моделью, результат успех")
    void predictOther(CheckType checkType) {
        var check = createCheck(checkType);
        if (!checkType.equals(CheckType.ODOMETER)) {
            var method = "";
            switch (checkType) {
                case HEADLAMPS_LF,
                     HEADLAMPS_RF -> {
                    method = "front-light";
                    when(predictProperties.isNeedFrontLights()).thenReturn(true);
                }
                case HEADLAMPS_LR,
                     HEADLAMPS_RR -> {
                    method = "back-lights";
                    when(predictProperties.isNeedBackLights()).thenReturn(true);
                }
                case SIDE_MIRRORS_L,
                     SIDE_MIRRORS_R -> {
                    method = "side-mirrors";
                    when(predictProperties.isNeedSideMirrors()).thenReturn(true);
                }
                case SPLASH_GUARDS_LF,
                     SPLASH_GUARDS_RF,
                     SPLASH_GUARDS_LR,
                     SPLASH_GUARDS_RR -> {
                    method = "mudguards";
                    when(predictProperties.isNeedSplashGuards()).thenReturn(true);
                }
                case WIND_SCREEN -> {
                    method = "windshield";
                    when(predictProperties.isNeedWindScreen()).thenReturn(true);
                }
                case POWER_STEERING_LIQUID_LEVEL -> {
                    method = "gur";
                    when(predictProperties.isNeedPowerSteeringLiquidLevel()).thenReturn(true);
                }
                case COOLANT_LEVEL -> {
                    method = "cooling";
                    when(predictProperties.isNeedCoolantLevel()).thenReturn(true);
                }
                case OIL_LEVEL -> {
                    method = "oil";
                    when(predictProperties.isNeedOilLevel()).thenReturn(true);
                }
                case WINDSHIELD_WIPERS_AND_LIQUID -> {
                    when(predictProperties.isNeedCarWipers()).thenReturn(true);
                    when(predictProperties.isNeedWasherLiquid()).thenReturn(true);
                }
            }
            var predictResponseDto = new PredictResponseDto(200, true, Collections.emptyList());
            var url = URI + method + "/predict";
            when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
            when(predictProperties.getHostAi()).thenReturn(URI);
            when(restTemplate.postForEntity(eq(url),
                                            any(HttpEntity.class),
                                            ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(predictResponseDto));
            var result = predictService.predictPhoto(files, check);
            assertTrue(result);
            if (checkType == CheckType.WINDSHIELD_WIPERS_AND_LIQUID) {
                verify(restTemplate, times(2)).postForEntity(any(String.class),
                                                             any(HttpEntity.class),
                                                             ArgumentMatchers.<Class<PredictResponseDto>>any());
            } else {
                verify(restTemplate).postForEntity(any(String.class),
                                                   any(HttpEntity.class),
                                                   ArgumentMatchers.<Class<PredictResponseDto>>any());
            }
        } else {
            assertThrows(NotImplementedException.class, () -> predictService.predictPhoto(file, check));
        }
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-wipers and washer-liquid, результат успех по car-wipers")
    void predictCarWipersAndWasherLiquidNotSuccessWipers() {
        var check = createCheck(CheckType.WINDSHIELD_WIPERS_AND_LIQUID);
        var responseDto1 = new PredictResponseDto(200, true, Collections.emptyList());
        var responseDto2 = new PredictResponseDto(200, false, Collections.emptyList());
        var url1 = URI + "car-wipers/predict";
        var url2 = URI + "washer-liquid/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedCarWipers()).thenReturn(true);
        when(predictProperties.isNeedWasherLiquid()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url1),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto1));
        when(restTemplate.postForEntity(eq(url2),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto2));
        assertFalse(predictService.predictPhoto(file, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели car-wipers and washer-liquid, результат успех по washer-liquid")
    void predictCarWipersAndWasherLiquidNotSuccessLiquid() {
        var check = createCheck(CheckType.WINDSHIELD_WIPERS_AND_LIQUID);
        var responseDto1 = new PredictResponseDto(200, false, Collections.emptyList());
        var url1 = URI + "car-wipers/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedCarWipers()).thenReturn(true);
        when(predictProperties.getHostAi()).thenReturn(URI);
        when(restTemplate.postForEntity(eq(url1),
                any(HttpEntity.class),
                ArgumentMatchers.<Class<PredictResponseDto>>any())).thenReturn(ResponseEntity.ok(responseDto1));
        assertFalse(predictService.predictPhoto(file, check));
    }

    @SneakyThrows
    @Test
    @DisplayName("Запрос к модели, результат ошибка")
    void predictOtherResponseException() {
        var method = "mudguards";
        var check = createCheck(CheckType.SPLASH_GUARDS_LF);
        var url = URI + method + "/predict";
        when(objectMapper.writeValueAsString(any(LinkedHashMap.class))).thenReturn("params");
        when(predictProperties.isNeedSplashGuards()).thenReturn(true);
        when(restTemplate.postForEntity(eq(url), any(), any())).thenThrow(new RuntimeException("internal error"));
        when(predictProperties.getHostAi()).thenReturn(URI);
        assertFalse(predictService.predictPhoto(files, check));
        checkLogs(method, CheckType.SPLASH_GUARDS_LF.name());
    }
    
    private void checkLogs(String method, String checkTypeName) {
        assertThat(LOGGING_EXTENSION.getEvents())
                .hasSize(4)
                .extracting(
                        ILoggingEvent::getLevel,
                        ILoggingEvent::getFormattedMessage
                           )
                .containsExactly(
                        tuple(
                                Level.INFO,
                                "start predict, checkType:%s".formatted(checkTypeName)
                             ),
                        tuple(
                                Level.INFO,
                                String.format("При проверке файла возникла ошибка. method=%s, filename=file, message=internal error", method)
                             ),
                        tuple(
                                Level.DEBUG,
                                "internal error"
                             ),
                        tuple(
                                Level.INFO,
                                "finish predict, checkType:%s".formatted(checkTypeName)
                             )
                                );
    }

    private Check createCheck(CheckType checkType) {
        var transport1Instance = Instancio.of(Transport.class)
                                          .set(field(Transport::getStatus), TransportStatus.IN_USE)
                                          .set(field(Transport::getStateNumber), "А010ЕК50")
                                          .create();
        return Check.builder()
                .checkType(checkType)
                .checkStatus(CheckStatus.IN_PROGRESS)
                .attempt(0)
                .request(Request.builder()
                        .id(UUID.randomUUID())
                        .author(author)
                        .creationTime(LocalDateTime.now())
                        .humanReadableId("OT-768")
                        .status(RequestStatus.IN_PROGRESS)
                        .transport(transport1Instance)
                        .build())
                .build();
    }
}