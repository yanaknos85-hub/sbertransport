package ru.sberbank.ditsib.transport.request.service.taxiprice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.dto.ExternalPriceDTO;
import ru.sberbank.ditsib.transport.request.dto.TaxiPriceDto;
import ru.sberbank.ditsib.transport.request.service.TaxiPriceService;
import ru.sberbank.ditsib.transport.request.service.taxiprice.properties.TaxiProvidersProperties;
import ru.sberbank.ditsib.transport.request.service.taxiprice.struct.CityMobilCalculateRequestDto;
import ru.sberbank.ditsib.transport.request.service.taxiprice.struct.CityMobilCalculateResponseDto;
import ru.sberbank.ditsib.transport.request.service.taxiprice.struct.CityMobilResultDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Сервис парсинга XML
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityMobilPriceServiceImpl extends TaxiPriceService {

    public static final String ECONOMICAL = "economical";
    public static final String COMFORT = "comfort";
    public static final String BUSINESS = "business";
    public static final String COMFORT_PLUS = "comfort_plus";
    private final RestTemplate restTemplate;
    private final TaxiProvidersProperties taxiProvidersProperties;
    private final ObjectMapper objectMapper;
    private final ArrayList<String> taxiClasses = new ArrayList<>(Arrays.asList(ECONOMICAL, COMFORT, BUSINESS, COMFORT_PLUS));
    
    @Override
    public ArrayList<String> getTaxiClasses() {
        return taxiClasses;
    }
    
    @Override
    public String getProvider() {
        return taxiProvidersProperties.getCitymobil().getProvider();
    }
    
    @Override
    public Boolean isEnabled() {
        return taxiProvidersProperties.getCitymobil().getEnabled();
    }
    
    @Override
    public List<TaxiPriceDto> calculate(ExternalPriceDTO externalPriceDTO, List<String> taxiClass) {
        var sourceWaypoint = externalPriceDTO.getExpected().getWaypoints().getFirst();
        var targetWaypoint = externalPriceDTO.getExpected().getWaypoints().getLast();
        
        var cityMobilCalculateRequestDto = new CityMobilCalculateRequestDto();
        cityMobilCalculateRequestDto.getRoutePoints().setSource(CityMobilCalculateRequestDto.Point.builder()
                                                                                                  .latitude(sourceWaypoint.getLatitude())
                                                                                                  .longitude(sourceWaypoint.getLongitude())
                                                                                                  .build());
        cityMobilCalculateRequestDto.getRoutePoints().setDestination(CityMobilCalculateRequestDto.Point.builder()
                                                                                                       .latitude(targetWaypoint.getLatitude())
                                                                                                       .longitude(targetWaypoint.getLongitude())
                                                                                                       .build());
        cityMobilCalculateRequestDto.getTariffGroups().addAll(taxiClass);
        try {
            var json = objectMapper.writeValueAsString(cityMobilCalculateRequestDto);
            log.debug("DEBUGEXTERNALAPI: citymobil: request for calculate input is {}", json);
            var httpEntity = new HttpEntity<>(json, getHeaders());
            HttpMethod method = HttpMethod.POST;
            var url = taxiProvidersProperties.getCitymobil().getBaseUrl() +
                    "/orders/calculate";
            
            ResponseEntity<String> response = restTemplate.exchange(url, method, httpEntity,
                                                                    new ParameterizedTypeReference<>() {
                                                                    });
            var result = response.getBody();
            log.debug("DEBUGEXTERNALAPI: citymobil: request for calculate output is {}", result);
            if (result == null) {
                throw new RuntimeException("citymobil: request for calculate: empty response");
            }
            var cityMobilResultDto =
                    objectMapper.readValue(result, new TypeReference<CityMobilResultDto>() {
                    });
            if (cityMobilResultDto.isSuccess()) {
                var cityMobilCalculateResponseDto =
                        objectMapper.readValue(result, new TypeReference<CityMobilCalculateResponseDto>() {
                        });
                var taxiPriceDtoList = new ArrayList<TaxiPriceDto>();
                for (var calculation : cityMobilCalculateResponseDto.getCalculations()) {
                    var taxiPriceDto = TaxiPriceDto.builder()
                                                            .provider(taxiProvidersProperties.getCitymobil().getProvider())
                                                            .taxiClass(mapClasses(calculation.tariff().tariffGroup()))
                                                            .tariffId(calculation.tariff().tariffGroup())
                                                            .price(calculation.precalculatedPrice())
                                                            .calcHash(calculation.hash())
                                                            .eta(calculation.eta() * 60)
                                                            .duration(calculation.track().duration().intValue() * 60)
                                                            .build();
                    taxiPriceDtoList.add(taxiPriceDto);
                }
                return taxiPriceDtoList;
            }
        } catch (Exception e) {
            log.error("citymobil: calculate unknown error", e);
        }
        return getResponseWithZeroPrice();
    }
    
    public TaxiClass mapClasses(String taxiClassStr) {
        if (taxiClassStr.equalsIgnoreCase(ECONOMICAL)) {
            return TaxiClass.ECONOMY;
        }
        if (taxiClassStr.equalsIgnoreCase(COMFORT)) {
            return TaxiClass.COMFORT;
        }
        if (taxiClassStr.equalsIgnoreCase(BUSINESS)) {
            return TaxiClass.BUSINESS;
        }
        if (taxiClassStr.equalsIgnoreCase(COMFORT_PLUS)) {
            return TaxiClass.COMFORT_PLUS;
        }
        return null;
    }
    
    private HttpHeaders getHeaders() {
        var httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(taxiProvidersProperties.getCitymobil().getAuthHeader(),
                        taxiProvidersProperties.getCitymobil().getAuthToken());
        return httpHeaders;
    }

}
