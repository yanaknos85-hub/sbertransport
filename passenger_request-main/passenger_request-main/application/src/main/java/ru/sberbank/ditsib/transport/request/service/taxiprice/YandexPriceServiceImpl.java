package ru.sberbank.ditsib.transport.request.service.taxiprice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.dto.ExternalPriceDTO;
import ru.sberbank.ditsib.transport.request.dto.TaxiPriceDto;
import ru.sberbank.ditsib.transport.request.service.TaxiPriceService;
import ru.sberbank.ditsib.transport.request.service.taxiprice.properties.TaxiProvidersProperties;
import ru.sberbank.ditsib.transport.request.service.taxiprice.struct.YandexCalculateResponseDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Сервис парсинга XML
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexPriceServiceImpl extends TaxiPriceService {

    /**
     *  econom — «Эконом».
     */
    public static final String ECONOMY = "econom";
    /**
     *  business — «Комфорт».
     */
    public static final String BUSINESS = "business";
    /**
     *  vip — «Бизнес».
     */
    public static final String VIP = "vip";
    /**
     *  comfortplus — «Комфорт+».
     */
    public static final String COMFORT_PLUS = "comfortplus";
    private final RestTemplate restTemplate;
    private final TaxiProvidersProperties taxiProvidersProperties;
    private final ObjectMapper objectMapper;
    private final ArrayList<String> taxiClasses = new ArrayList<>(Arrays.asList(ECONOMY, BUSINESS, VIP, COMFORT_PLUS));

    @Override
    public ArrayList<String> getTaxiClasses() {
        return taxiClasses;
    }
    
    @Override
    public String getProvider() {
        return taxiProvidersProperties.getYandex().getProvider();
    }
    
    @Override
    public Boolean isEnabled() {
        return taxiProvidersProperties.getYandex().getEnabled();
    }

    @Override
    public List<TaxiPriceDto> calculate(ExternalPriceDTO externalPriceDto, List<String> taxiClass) {
        var sourceWaypoint = externalPriceDto.getExpected().getWaypoints().getFirst();
        var targetWaypoint = externalPriceDto.getExpected().getWaypoints().getLast();
        
        var httpEntity = new HttpEntity<>(getHeaders());
        var method = HttpMethod.GET;

        var url = taxiProvidersProperties.getYandex().getBaseUrl() +
                "?clid=" +
                taxiProvidersProperties.getYandex().getClientId() +
                "&rll=" +
                sourceWaypoint.getLongitude() +
                "," +
                sourceWaypoint.getLatitude() +
                "~" +
                targetWaypoint.getLongitude() +
                "," +
                targetWaypoint.getLatitude() +
                "&class=" +
                String.join(",", taxiClass);
        
        try {
            log.debug("DEBUGEXTERNALAPI: yandex: request for calculate request is: {}", url);
            var response = restTemplate.exchange(url, method, httpEntity,
                                                                    new ParameterizedTypeReference<String>() {
                                                                    });
            var result = response.getBody();
            log.debug("DEBUGEXTERNALAPI: yandex: request for calculate response is {}", result);
            
            if (StringUtils.hasText(result)) {
                var yandexCalculateResponseDto =
                        objectMapper.readValue(result, new TypeReference<YandexCalculateResponseDto>() {
                        });
                
                var taxiPriceDtoList = new ArrayList<TaxiPriceDto>();
                for (var options : yandexCalculateResponseDto.options()) {
                    var taxiPriceDto = TaxiPriceDto.builder()
                                                   .provider(taxiProvidersProperties.getYandex().getProvider())
                                                   .taxiClass(mapClasses(options.class_name()))
                                                   .tariffId(options.class_name())
                                                   .price(Optional.ofNullable(options.price()).orElse(0))
                                                   .calcHash("")
                                                   .eta(Optional.ofNullable(options.waiting_time()).map(Double::intValue).orElse(0))
                                                   .duration(
                                                           Optional.ofNullable(yandexCalculateResponseDto.time()).map(Double::intValue).orElse(0))
                                                   .build();
                    taxiPriceDtoList.add(taxiPriceDto);
                }
                return taxiPriceDtoList;
            }
        } catch (Exception e) {
            log.error("yandex: request for calculate: unknown error", e);
        }
        return getResponseWithZeroPrice();
    }
    
    public TaxiClass mapClasses(String taxiClassStr) {
        if (taxiClassStr.equalsIgnoreCase(ECONOMY)) {
            return TaxiClass.ECONOMY;
        }
        if (taxiClassStr.equalsIgnoreCase(BUSINESS)) {
            return TaxiClass.COMFORT;
        }
        if (taxiClassStr.equalsIgnoreCase(VIP)) {
            return TaxiClass.BUSINESS;
        }
        if (taxiClassStr.equalsIgnoreCase(COMFORT_PLUS)) {
            return TaxiClass.COMFORT_PLUS;
        }
        return null;
    }
    
    private HttpHeaders getHeaders() {
        var httpHeaders = new HttpHeaders();
        httpHeaders.add("accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(taxiProvidersProperties.getYandex().getAuthHeader(),
                        taxiProvidersProperties.getYandex().getAuthToken());
        return httpHeaders;
    }
}
