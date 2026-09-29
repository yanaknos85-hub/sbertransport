package ru.sberbank.ditsib.transport.tariff.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.tariff.controller.ResendController;
import ru.sberbank.ditsib.transport.tariff.service.ContractService;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ResendControllerImpl implements ResendController {
    
    private final ContractService contractService;
    
    private final TariffService tariffService;
    
    @Override
    public void syncContract() {
        contractService.resend();
    }
    
    @Override
    public void syncTariff() {
        tariffService.resend();
    }
}
