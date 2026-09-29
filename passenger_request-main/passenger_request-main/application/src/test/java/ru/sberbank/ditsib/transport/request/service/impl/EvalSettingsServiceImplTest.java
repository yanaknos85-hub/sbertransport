package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.dao.EvalSettingsRepository;
import ru.sberbank.ditsib.transport.request.database.model.EvalSettings;
import ru.sberbank.ditsib.transport.request.dto.AdvantageDrawbackDTO;
import ru.sberbank.ditsib.transport.request.dto.PassengerEvalSettingsDTO;
import ru.sberbank.ditsib.transport.request.evaluators.RemarkType;
import ru.sberbank.ditsib.transport.request.exceptions.EvalSettingsNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EvalSettingsServiceImplTest {

    @InjectMocks
    private EvalSettingsServiceImpl evalSettingsService;
    @Mock
    private EvalSettingsRepository evalSettingsRepository;

    @Test
    void get() {
        var evalSettingsAdvantagesTaxiList = Instancio.ofList(EvalSettings.class)
                .size(5)
                .set(field(EvalSettings::getTransportType), TransportTypeEnum.TAXI)
                .set(field(EvalSettings::getRemarkType), RemarkType.ADVANTAGES)
                .create();
        var evalSettingsAdvantagesPrivateList = Instancio.ofList(EvalSettings.class)
                .size(5)
                .set(field(EvalSettings::getTransportType), TransportTypeEnum.PRIVATE)
                .set(field(EvalSettings::getRemarkType), RemarkType.ADVANTAGES)
                .create();
        var evalSettingsDrawbacksTaxiList = Instancio.ofList(EvalSettings.class)
                .size(5)
                .set(field(EvalSettings::getTransportType), TransportTypeEnum.TAXI)
                .set(field(EvalSettings::getRemarkType), RemarkType.DRAWBACKS)
                .create();
        var evalSettingsDrawbacksPrivateList = Instancio.ofList(EvalSettings.class)
                .size(5)
                .set(field(EvalSettings::getTransportType), TransportTypeEnum.PRIVATE)
                .set(field(EvalSettings::getRemarkType), RemarkType.DRAWBACKS)
                .create();
        var evalSettingsList = new ArrayList<EvalSettings>();
        evalSettingsList.addAll(evalSettingsAdvantagesPrivateList);
        evalSettingsList.addAll(evalSettingsDrawbacksPrivateList);
        evalSettingsList.addAll(evalSettingsAdvantagesTaxiList);
        evalSettingsList.addAll(evalSettingsDrawbacksTaxiList);
        doReturn(evalSettingsList).when(evalSettingsRepository).findAll();
        var advantagePrivateList = evalSettingsAdvantagesPrivateList.stream()
                .map(evalSettings -> AdvantageDrawbackDTO.builder()
                        .order(evalSettings.getOrder())
                        .code(evalSettings.getCode())
                        .title(evalSettings.getTitle())
                        .image(evalSettings.getImage())
                        .build())
                .toList();
        var advantageTaxiList = evalSettingsAdvantagesTaxiList.stream()
                .map(evalSettings -> AdvantageDrawbackDTO.builder()
                        .order(evalSettings.getOrder())
                        .code(evalSettings.getCode())
                        .title(evalSettings.getTitle())
                        .image(evalSettings.getImage())
                        .build())
                .toList();
        var drawbackPrivateList = evalSettingsDrawbacksPrivateList.stream()
                .map(evalSettings -> AdvantageDrawbackDTO.builder()
                        .order(evalSettings.getOrder())
                        .code(evalSettings.getCode())
                        .title(evalSettings.getTitle())
                        .image(evalSettings.getImage())
                        .build())
                .toList();
        var drawbackTaxiList = evalSettingsDrawbacksTaxiList.stream()
                .map(evalSettings -> AdvantageDrawbackDTO.builder()
                        .order(evalSettings.getOrder())
                        .code(evalSettings.getCode())
                        .title(evalSettings.getTitle())
                        .image(evalSettings.getImage())
                        .build())
                .toList();
        var passengerEvalSettingsTaxiDTO = PassengerEvalSettingsDTO
                .builder()
                .transportType(TransportTypeEnum.TAXI)
                .advantages(advantageTaxiList)
                .drawbacks(drawbackTaxiList)
                .build();
        var passengerEvalSettingsPrivateDTO = PassengerEvalSettingsDTO
                .builder()
                .transportType(TransportTypeEnum.PRIVATE)
                .advantages(advantagePrivateList)
                .drawbacks(drawbackPrivateList)
                .build();
        assertThat(evalSettingsService.get())
                .usingRecursiveComparison()
                .isEqualTo(List.of(passengerEvalSettingsTaxiDTO, passengerEvalSettingsPrivateDTO));
        verify(evalSettingsRepository).findAll();
    }

    @Test
    void getException() {
        doReturn(Collections.emptyList()).when(evalSettingsRepository).findAll();
        assertThatThrownBy(() -> evalSettingsService.get())
                .isInstanceOf(EvalSettingsNotFoundException.class)
                .hasMessage("Настройки для оценки не найдены, либо некорректны");
    }
}