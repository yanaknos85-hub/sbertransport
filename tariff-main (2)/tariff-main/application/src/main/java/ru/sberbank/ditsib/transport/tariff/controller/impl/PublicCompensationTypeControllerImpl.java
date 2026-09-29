package ru.sberbank.ditsib.transport.tariff.controller.impl;

import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.tariff.controller.PublicCompensationTypeController;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;

import java.util.Arrays;
import java.util.List;

/**
 * Имплементация контроллера тарифов общественного транспорта
 */
@RestController
public class PublicCompensationTypeControllerImpl implements PublicCompensationTypeController {
    
    @Override
    public List<PublicCompensationType> getAll() {
        return Arrays.asList(PublicCompensationType.values());
    }
}
