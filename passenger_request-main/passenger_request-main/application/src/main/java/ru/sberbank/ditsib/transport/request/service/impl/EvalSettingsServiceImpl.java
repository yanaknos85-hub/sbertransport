package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.EvalSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.EvalSettings;
import ru.sberbank.ditsib.transport.request.dto.AdvantageDrawbackDTO;
import ru.sberbank.ditsib.transport.request.dto.PassengerEvalSettingsDTO;
import ru.sberbank.ditsib.transport.request.evaluators.RemarkType;
import ru.sberbank.ditsib.transport.request.exceptions.EvalSettingsNotFoundException;
import ru.sberbank.ditsib.transport.request.service.EvalSettingsService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvalSettingsServiceImpl implements EvalSettingsService {

    private final EvalSettingsRepository evalSettingsRepository;

    @Override
    public Collection<? extends PassengerEvalSettingsDTO> get() {
        var settings = new ArrayList<PassengerEvalSettingsDTO>();
        var evalSettings = evalSettingsRepository.findAll();
        var transportTypes = evalSettings.stream()
                .map(EvalSettings::getTransportType)
                .distinct()
                .sorted()
                .toList();
        if (transportTypes.isEmpty()) {
            throw new EvalSettingsNotFoundException();
        }
        for (var transportType : transportTypes) {
            settings.add(PassengerEvalSettingsDTO
                    .builder()
                    .transportType(transportType)
                    .advantages(getRemarks(evalSettings, transportType, RemarkType.ADVANTAGES))
                    .drawbacks(getRemarks(evalSettings, transportType, RemarkType.DRAWBACKS))
                    .build());
        }
        log.debug("Найдены настройки для следующих видов транспорта: {}", transportTypes.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", ")));
        return settings;
    }

    private List<AdvantageDrawbackDTO> getRemarks(List<EvalSettings> evalSettings, TransportTypeEnum transportType, RemarkType remarkType) {
        var remarks = new ArrayList<AdvantageDrawbackDTO>();
        var settings = evalSettings.stream()
                .filter(s -> transportType.equals(s.getTransportType()) && remarkType.equals(s.getRemarkType()))
                .toList();
        for (var setting : settings) {
            remarks.add(AdvantageDrawbackDTO.builder()
                    .order(setting.getOrder())
                    .code(setting.getCode())
                    .title(setting.getTitle())
                    .image(setting.getImage())
                    .build()
            );
        }
        return remarks;
    }
}
