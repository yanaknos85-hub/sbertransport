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
public class UberPriceServiceImpl extends TaxiPriceService {

    private static final String UBER_X = "uberx";
    private static final String UBER_SELECT = "uberselect";
    private static final String UBER_SELECT_PLUS = "uberselectplus";
    private static final String UBER_BLACK = "uberblack";
    private final RestTemplate restTemplate;
    private final TaxiProvidersProperties taxiProvidersProperties;
    private final ObjectMapper objectMapper;
    private final ArrayList<String> taxiClasses = new ArrayList<>(Arrays.asList(UBER_X, UBER_SELECT, UBER_SELECT_PLUS, UBER_BLACK));

    @Override
    public ArrayList<String> getTaxiClasses() {
        return taxiClasses;
    }

    @Override
    public String getProvider() {
        return taxiProvidersProperties.getUber().getProvider();
    }

    @Override
    public Boolean isEnabled() {
        return taxiProvidersProperties.getUber().getEnabled();
    }

    @Override
    public List<TaxiPriceDto> calculate(ExternalPriceDTO externalPriceDto, List<String> taxiClass) {
        var sourceWaypoint = externalPriceDto.getExpected().getWaypoints().getFirst();
        var targetWaypoint = externalPriceDto.getExpected().getWaypoints().getLast();

        var httpEntity = new HttpEntity<String>(getHeaders());
        var method = HttpMethod.GET;

        var url = taxiProvidersProperties.getUber().getBaseUrl() +
                "?clid=" +
                taxiProvidersProperties.getUber().getClientId() +
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
            log.debug("DEBUGEXTERNALAPI: uber: request for calculate request is: {}", url);
            var response = restTemplate.exchange(url, method, httpEntity,
                    new ParameterizedTypeReference<String>() {
                    });
            var result = response.getBody();
            log.debug("DEBUGEXTERNALAPI: uber: request for calculate response is {}", result);

            if (StringUtils.hasText(result)) {
                var yandexCalculateResponseDto =
                        objectMapper.readValue(result, new TypeReference<YandexCalculateResponseDto>() {
                        });

                var taxiPriceDtoArrayList = new ArrayList<TaxiPriceDto>();
                for (var options : yandexCalculateResponseDto.options()) {
                    var taxiPriceDto = TaxiPriceDto.builder()
                            .provider(taxiProvidersProperties.getUber().getProvider())
                            .taxiClass(mapClasses(options.class_name()))
                            .tariffId(options.class_name())
                            .price(Optional.ofNullable(options.price()).orElse(0))
                            .calcHash("")
                            .eta(Optional.ofNullable(options.waiting_time())
                                    .map(Double::intValue)
                                    .orElse(0))
                            .duration(Optional.ofNullable(yandexCalculateResponseDto.time())
                                    .map(Double::intValue)
                                    .orElse(0))
                            .build();
                    taxiPriceDtoArrayList.add(taxiPriceDto);
                }
                return taxiPriceDtoArrayList;
            }
        } catch (Exception e) {
            log.error("uber: request for calculate: unknown error", e);
        }
        return getResponseWithZeroPrice();
    }


    public TaxiClass mapClasses(String taxiClassStr) {
        if (taxiClassStr.equalsIgnoreCase(UBER_X)) {
            return TaxiClass.ECONOMY;
        }
        if (taxiClassStr.equalsIgnoreCase(UBER_SELECT)) {
            return TaxiClass.COMFORT;
        }
        if (taxiClassStr.equalsIgnoreCase(UBER_BLACK)) {
            return TaxiClass.BUSINESS;
        }
        if (taxiClassStr.equalsIgnoreCase(UBER_SELECT_PLUS)) {
            return TaxiClass.COMFORT_PLUS;
        }
        return null;
    }

    private HttpHeaders getHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("accept", MediaType.APPLICATION_JSON_VALUE);
        httpHeaders.add(taxiProvidersProperties.getUber().getAuthHeader(),
                taxiProvidersProperties.getUber().getAuthToken());
        return httpHeaders;
    }
}
