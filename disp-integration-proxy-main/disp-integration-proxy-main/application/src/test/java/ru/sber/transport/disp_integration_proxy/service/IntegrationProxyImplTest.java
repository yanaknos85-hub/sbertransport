package ru.sber.transport.disp_integration_proxy.service;

import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.disp_integration_proxy.web.service.impl.IntegrationProxyServiceImpl;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_disp_integration_proxy")
@ActiveProfiles({"test"})
public class IntegrationProxyImplTest {

    @Test
    @SneakyThrows
    void testAuth() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var clazz= integrationProxyServiceImpl.getClass();
        var field = clazz.getDeclaredField("authUrl");
        field.setAccessible(true);
        field.set(integrationProxyServiceImpl, "http://authentication:8080/login");
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(obj);
        var resp = integrationProxyServiceImpl.auth();
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCreatePassenger() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var clazz= integrationProxyServiceImpl.getClass();
        var field = clazz.getDeclaredField("tripsUrl");
        field.setAccessible(true);
        field.set(integrationProxyServiceImpl, "http://trips:8080/integration");
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(obj);
        var resp = integrationProxyServiceImpl.createTrip("PASSENGER", "body");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCreateCargo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var clazz= integrationProxyServiceImpl.getClass();
        var field = clazz.getDeclaredField("tripsCargoUrl");
        field.setAccessible(true);
        field.set(integrationProxyServiceImpl, "http://trips-cargo:8080/integration");
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(obj);
        var resp = integrationProxyServiceImpl.createTrip("CARGO", "body");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testGetPassenger() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class))).thenReturn(obj);
        var resp = integrationProxyServiceImpl.getTrip("PASSENGER", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testGetCargo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class))).thenReturn(obj);
        var resp = integrationProxyServiceImpl.getTrip("CARGO", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCancelPassenger() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(obj);
        var resp = integrationProxyServiceImpl.cancelTrip("PASSENGER", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCancelCargo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var restTemplate = mock(RestTemplate.class);
        var integrationProxyServiceImpl = new IntegrationProxyServiceImpl(restTemplate);
        var obj = new ResponseEntity<Object>("ok", HttpStatusCode.valueOf(200));
        when(restTemplate.postForEntity(anyString(), any(), any())).thenReturn(obj);
        var resp = integrationProxyServiceImpl.cancelTrip("CARGO", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

}
