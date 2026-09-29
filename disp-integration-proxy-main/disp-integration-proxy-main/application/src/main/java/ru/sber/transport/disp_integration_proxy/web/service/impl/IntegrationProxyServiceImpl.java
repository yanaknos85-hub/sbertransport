package ru.sber.transport.disp_integration_proxy.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.sber.transport.disp_integration_proxy.enums.TransportationTypeEnum;
import ru.sber.transport.disp_integration_proxy.web.service.IntegrationProxyService;

@Component
@RequiredArgsConstructor
public class IntegrationProxyServiceImpl implements IntegrationProxyService {

    private final RestTemplate restTemplate;

    @Value("${integration-proxy.auth-url:http://authentication:8080/login}")
    private String authUrl;

    @Value("${integration-proxy.trips-url:http://trips:8080/integration}")
    private String tripsUrl;

    @Value("${integration-proxy.trips-cargo-url:http://trips-cargo:8080/integration}")
    private String tripsCargoUrl;

    @Override
    public Object auth() {
        var httpEntity = new HttpEntity<>(null, getHeaders());
        var authResponse = restTemplate.postForEntity(authUrl, httpEntity, Object.class);
        return authResponse.getBody();
    }

    @Override
    public Object createTrip(String transportationType, Object body) {
        var httpEntity = new HttpEntity<>(body, getHeaders());
        var baseUrl = switch (TransportationTypeEnum.valueOf(transportationType)) {
            case CARGO -> tripsCargoUrl;
            case PASSENGER -> tripsUrl;
        };
        var createTripResponse = restTemplate.postForEntity(baseUrl, httpEntity, Object.class);
        return createTripResponse.getBody();
    }

    @Override
    public Object getTrip(String transportationType, String orderId) {
        var httpEntity = new HttpEntity<>(null, getHeaders());
        var baseUrl = switch (TransportationTypeEnum.valueOf(transportationType)) {
            case CARGO -> tripsCargoUrl;
            case PASSENGER -> tripsUrl;
        };
        var getTripResponse = restTemplate.exchange(baseUrl + "/" + orderId, HttpMethod.GET, httpEntity, Object.class);
        return getTripResponse.getBody();
    }

    @Override
    public Object cancelTrip(String transportationType, String orderId) {
        var httpEntity = new HttpEntity<>(null, getHeaders());
        var baseUrl = switch (TransportationTypeEnum.valueOf(transportationType)) {
            case CARGO -> tripsCargoUrl;
            case PASSENGER -> tripsUrl;
        };
        var cancelTripResponse = restTemplate.postForEntity(baseUrl+ "/" + orderId + "/cancel", httpEntity, Object.class);
        return cancelTripResponse.getBody();
    }

    private HttpHeaders getHeaders() {
        var servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var request = servletRequestAttributes != null ? servletRequestAttributes.getRequest() : null;
        assert request != null;
        var httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", request.getHeader("Authorization"));
        return httpHeaders;
    }
}
