package ru.sberbank.ditsib.transport.request.service;

import ru.sberbank.ditsib.transport.request.dto.PassengerEvalSettingsDTO;

import java.util.Collection;

public interface EvalSettingsService {
    Collection<? extends PassengerEvalSettingsDTO> get();
}
