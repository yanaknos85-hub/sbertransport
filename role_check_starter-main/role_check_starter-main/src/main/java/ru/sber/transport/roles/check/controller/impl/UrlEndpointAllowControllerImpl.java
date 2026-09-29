package ru.sber.transport.roles.check.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.roles.check.controller.UrlEndpointAllowController;
import ru.sber.transport.roles.check.dto.UrlAllowDto;
import ru.sber.transport.roles.check.services.RoleProvider;

import java.util.List;

/**
 * Реализация контроллера, работающего с назначением ролей на URL.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
class UrlEndpointAllowControllerImpl implements UrlEndpointAllowController {
    
    private final RoleProvider roleProvider;
    
    @Override
    public void allow(List<UrlAllowDto> urlToRole) {
        for (var item : urlToRole) {
            var role = item.getRole();
            roleProvider.clearRole(role);
            for (var url : item.getUrl()) {
                var urlParts = url.split("\\s");
                roleProvider.save(role, HttpMethod.valueOf(urlParts[0]), urlParts[1]);
            }
        }
    }
    
    @Override
    public List<String> urls(String role) {
        return roleProvider.getUrls(role);
    }
}
