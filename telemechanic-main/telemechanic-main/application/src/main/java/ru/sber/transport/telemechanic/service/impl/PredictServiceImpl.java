package ru.sber.transport.telemechanic.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.config.properties.PredictProperties;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.dto.predict.CarPlateResponseDto;
import ru.sber.transport.telemechanic.dto.predict.InstrumentalPanelResponseDto;
import ru.sber.transport.telemechanic.dto.predict.PredictResponseDto;
import ru.sber.transport.telemechanic.helper.TransliterationHelper;
import ru.sber.transport.telemechanic.service.PredictService;
import ru.sberbank.ditsib.transport.exceptions.NotImplementedException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class PredictServiceImpl implements PredictService {
    private static final String PREDICT_LOG_MESSAGE = "При проверке файла возникла ошибка. method={}, filename={}, message={}";
    private static final String START_PREDICT_MESSAGE = "start predict, checkType:{}";
    private static final String FINISH_PREDICT_MESSAGE = "finish predict, checkType:{}";
    private final RestTemplate restTemplate;
    private final PredictProperties properties;
    private final ObjectMapper objectMapper;
    private static final String CAR_PLATE_METHOD = "car-plates";
    private static final String CAR_WIPERS_METHOD = "car-wipers";
    private static final String WASHER_LIQUID_METHOD = "washer-liquid";
    private static final String FRONT_LIGHTS_METHOD = "front-light";
    private static final String BACK_LIGHTS_METHOD = "back-lights";
    private static final String WIND_SCREEN_METHOD = "windshield";
    private static final String SIDE_MIRRORS_METHOD = "side-mirrors";
    private static final String SPLASH_GUARDS_METHOD = "mudguards";
    private static final String POWER_STEERING_LIQUID_LEVEL_METHOD = "gur";
    private static final String COOLANT_LEVEL_METHOD = "cooling";
    private static final String OIL_LEVEL_METHOD = "oil";
    private static final String INSTRUMENT_PANEL_METHOD = "dashboard";
    
    @Override
    public boolean predictPhoto(MultipartFile file, Check check) {
        log.info(START_PREDICT_MESSAGE, check.getCheckType().name());
        var result = switch (check.getCheckType()) {
            case VEHICLE_NUMBER -> predictNumber(file, properties.isNeedPlateNumber(), check);
            case HEADLAMPS_LF,
                 HEADLAMPS_RF -> predictOther(file, properties.isNeedFrontLights(), FRONT_LIGHTS_METHOD);
            case HEADLAMPS_LR,
                 HEADLAMPS_RR -> predictOther(file, properties.isNeedBackLights(), BACK_LIGHTS_METHOD);
            case SIDE_MIRRORS_L,
                 SIDE_MIRRORS_R -> predictOther(file, properties.isNeedSideMirrors(), SIDE_MIRRORS_METHOD);
            case SPLASH_GUARDS_LF,
                 SPLASH_GUARDS_LR,
                 SPLASH_GUARDS_RF,
                 SPLASH_GUARDS_RR -> predictOther(file, properties.isNeedSplashGuards(), SPLASH_GUARDS_METHOD);
            case WIND_SCREEN -> predictOther(file, properties.isNeedWindScreen(), WIND_SCREEN_METHOD);
            case POWER_STEERING_LIQUID_LEVEL -> predictOther(file, properties.isNeedPowerSteeringLiquidLevel(), POWER_STEERING_LIQUID_LEVEL_METHOD);
            case COOLANT_LEVEL -> predictOther(file, properties.isNeedCoolantLevel(), COOLANT_LEVEL_METHOD);
            case OIL_LEVEL -> predictOther(file, properties.isNeedOilLevel(), OIL_LEVEL_METHOD);
            case INSTRUMENT_PANEL -> predictInstrumentalPanel(file, properties.isNeedInstrumentPanel());
            case WINDSHIELD_WIPERS_AND_LIQUID -> checkWindShieldWipersAndWasherLiquid(file);
            default -> throw new NotImplementedException();
        };
        log.info(FINISH_PREDICT_MESSAGE, check.getCheckType().name());
        return result;
    }
    
    private boolean checkWindShieldWipersAndWasherLiquid(MultipartFile file) {
        return predictOther(file, properties.isNeedCarWipers(), CAR_WIPERS_METHOD)
               && predictOther(file, properties.isNeedWasherLiquid(), WASHER_LIQUID_METHOD);
    }
    
    private boolean predictNumber(MultipartFile file, boolean properties, Check check) {
        if (!properties) {
            return true;
        }
        CarPlateResponseDto response;
        try {
            response = makeRequest(file, CAR_PLATE_METHOD, CarPlateResponseDto.class);
        } catch (Exception e) {
            return logAndReturnFalse(CAR_PLATE_METHOD, file, e);
        }
        if (response == null || response.detail() == null || response.detail().isEmpty()) {
            return false;
        }
        var stateNumber = check.getRequest().getTransport().getStateNumber().toUpperCase().replace(" ", "");
        
        return response.detail().stream()
                       .map(carNumberDto -> TransliterationHelper.transliterateNumber(carNumberDto.carNumber()))
                       .anyMatch(carNumber -> carNumber.equals(stateNumber));
    }
    
    private boolean predictInstrumentalPanel(MultipartFile file, boolean properties) {
        if (!properties) {
            return true;
        }
        InstrumentalPanelResponseDto response;
        try {
            response = makeRequest(file, INSTRUMENT_PANEL_METHOD, InstrumentalPanelResponseDto.class);
        } catch (Exception e) {
            return logAndReturnFalse(INSTRUMENT_PANEL_METHOD, file, e);
        }
        if (!response.result()) {
            var errors = "";
            for (var errorPredictDto : response.detail()) {
                for (var error : errorPredictDto.errors()) {
                    errors = errors.concat(error).concat(";");
                }
            }
            log.info("Получен неуспешный ответ от модели dashboard:{}", errors);
        }
        return response.result();
    }
    
    private boolean predictOther(MultipartFile file, boolean properties, String method) {
        if (!properties) {
            return true;
        }
        try {
            var response = makeRequest(file, method, PredictResponseDto.class);
            return response != null && response.result();
        } catch (Exception e) {
            return logAndReturnFalse(method, file, e);
        }
    }
    
    private boolean logAndReturnFalse(String method, MultipartFile file, Exception e) {
        log.info(PREDICT_LOG_MESSAGE, method, file.getName(), e.getMessage());
        log.debug(e.getMessage(), e);
        return false;
    }
    
    private <T> T makeRequest(MultipartFile file, String method, Class<T> tClass) throws IOException {
        var headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var params = new LinkedHashMap<>();
        params.put("photo", Base64.encodeBase64String(file.getBytes()));
        var body = new LinkedMultiValueMap<>();
        body.add("photo", objectMapper.writeValueAsString(params));
        var requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(properties.getHostAi() + method + "/predict", requestEntity, tClass).getBody();
    }
}
