package ru.sber.transport.notifications.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.NotNull;

/**
 * Контроллер для работы с уведомлениями по лимитам
 */
@RequestMapping({"/send/{transportType}/", "/send/{transportType}"})
@Validated
@Tag(name = "Отправка уведомления", description = "Отправка уведомления через push или sms или email")
public interface NotificationController {
    
    /**
     * Отправление уведомления о низком лимите
     */
    @ResponseBody
    @Operation(summary = "Низкий лимит", description = "Отправить уведомление о низком лимите с учетом типа транспорта")
    @PutMapping(value = {"lowLimit", "lowLimit/"})
    void notificationLimit(@Parameter(hidden = true) Authentication authentication,
                           @PathVariable("transportType") @NotNull TransportTypeEnum type) throws JsonProcessingException;
}
