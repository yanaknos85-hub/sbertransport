package ru.sber.transport.disp_integration_proxy.controller;

import io.qameta.allure.Feature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.disp_integration_proxy.web.controller.impl.IntegrationProxyControllerImpl;
import ru.sber.transport.disp_integration_proxy.web.service.impl.IntegrationProxyServiceImpl;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_disp_integration_proxy")
@ActiveProfiles({"test"})
public class IntegrationProxyControllerTest {

    @Test
    @SneakyThrows
    void testAuth() {
        var service = mock(IntegrationProxyServiceImpl.class);
        var integrationProxyController = new IntegrationProxyControllerImpl(service);
        var obj = new ResponseEntity<>("ok", HttpStatusCode.valueOf(200));
        when(service.auth()).thenReturn(obj.getBody());
        var resp = integrationProxyController.auth();
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCreate() {
        var service = mock(IntegrationProxyServiceImpl.class);
        var integrationProxyController = new IntegrationProxyControllerImpl(service);
        var obj = new ResponseEntity<>("ok", HttpStatusCode.valueOf(200));
        when(service.createTrip("PASSENGER", "body")).thenReturn(obj.getBody());
        var resp = integrationProxyController.createTrip("PASSENGER", "body");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testGet() {
        var service = mock(IntegrationProxyServiceImpl.class);
        var integrationProxyController = new IntegrationProxyControllerImpl(service);
        var obj = new ResponseEntity<>("ok", HttpStatusCode.valueOf(200));
        when(service.getTrip("PASSENGER", "id")).thenReturn(obj.getBody());
        var resp = integrationProxyController.getTrip("PASSENGER", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

    @Test
    @SneakyThrows
    void testCancel() {
        var service = mock(IntegrationProxyServiceImpl.class);
        var integrationProxyController = new IntegrationProxyControllerImpl(service);
        var obj = new ResponseEntity<>("ok", HttpStatusCode.valueOf(200));
        when(service.cancelTrip("PASSENGER", "id")).thenReturn(obj.getBody());
        var resp = integrationProxyController.cancelTrip("PASSENGER", "id");
        Assertions.assertEquals(obj.getBody(), resp);
    }

}
