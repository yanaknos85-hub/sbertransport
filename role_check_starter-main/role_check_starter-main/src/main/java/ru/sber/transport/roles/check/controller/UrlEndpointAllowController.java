package ru.sber.transport.roles.check.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.roles.check.dto.UrlAllowDto;

import java.util.List;

/**
 * Контроллер, работающего с назначением ролей на URL.
 */
@RequestMapping
public interface UrlEndpointAllowController {
    
    /**
     * Установка разрешения на вызов URL.
     *
     * @param urlToRole данные по ролям.
     */
    @PutMapping(value = "allow", consumes = MediaType.APPLICATION_JSON_VALUE)
    void allow(@RequestBody List<UrlAllowDto> urlToRole);
    
    /**
     * Получение разрешенных URL.
     *
     * @param role роли для получения URL.
     * @return список URL.
     */
    @GetMapping(value = "allow/{role}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<String> urls(@PathVariable("role") String role);
    
}
