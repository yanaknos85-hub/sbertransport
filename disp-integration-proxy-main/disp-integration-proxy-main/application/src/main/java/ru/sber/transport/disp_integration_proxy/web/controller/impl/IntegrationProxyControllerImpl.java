package ru.sber.transport.disp_integration_proxy.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.disp_integration_proxy.web.controller.IntegrationProxyController;
import ru.sber.transport.disp_integration_proxy.web.service.IntegrationProxyService;

@RestController
@RequiredArgsConstructor
public class IntegrationProxyControllerImpl implements IntegrationProxyController {

    private final IntegrationProxyService integrationProxyService;

    @Override
    public Object auth() {
        return integrationProxyService.auth();
    }

    @Override
    public Object createTrip(String transportationType, Object body) {
        return integrationProxyService.createTrip(transportationType, body);
    }

    @Override
    public Object getTrip(String transportationType, String orderId) {
        return integrationProxyService.getTrip(transportationType, orderId);
    }

    @Override
    public Object cancelTrip(String transportationType, String orderId) {
        return integrationProxyService.cancelTrip(transportationType, orderId);
    }
}
