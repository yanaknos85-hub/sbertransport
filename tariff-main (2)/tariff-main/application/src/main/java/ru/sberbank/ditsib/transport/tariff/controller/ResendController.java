package ru.sberbank.ditsib.transport.tariff.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping({"resend","resend/"})
@Tag(name = "Тарифы", description = "Контроллер для работы с тарифами")
public interface ResendController {
    
    @GetMapping(value = {"contract","contract/"})
    @Operation(summary = "Синхронизация контрактов", description = "Синхронизация контрактов")
    void syncContract();
    
    @GetMapping(value = {"tariff","tariff/"})
    @Operation(summary = "Синхронизация тарифов", description = "Синхронизация тарифов")
    void syncTariff();
    
}
