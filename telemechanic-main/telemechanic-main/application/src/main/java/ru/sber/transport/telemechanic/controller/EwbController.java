package ru.sber.transport.telemechanic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.SecondTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.*;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchAllOrganizationsRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchResponseDto;
import ru.sber.transport.telemechanic.dto.ewb.search.EwbSearchSelfOrganizationRequestDto;
import ru.sber.transport.telemechanic.dto.ewb.second_title.SecondTitleForm;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SendAndSaveTitleRequest;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitleSendResponse;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechOutTitlesRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryAllOrganizationsRequest;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistryResponse;
import ru.sber.transport.telemechanic.dto.ewb_report.EwbRegistrySelfOrganizationRequest;

import java.util.UUID;

@Validated
@RequestMapping("ewb")
@Tag(name = "Контроллер по взаимодействия с КОРУС", description = "Набор операция для работы с КОРУС")
public interface EwbController {
    
    @PostMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Авторизация клиента в системе КОРУС", description = "Получение токена для работы в системе КОРУС", hidden = true)
    TokenDto getToken();
    
    @PostMapping(value = "/uuid", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение UUID", description = "Получение UUID по токену авторизации в системе КОРУС")
    UuidDto getUUID();
    
    @PostMapping(value = "/form-title/1", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Формирование и сохранение первого титула",
               description = "Формирование и сохранение первого титула перед отправкой в систему КОРУС")
    FirstTitleResponse generateFirstTitle(
            @RequestBody @Valid FirstTitleRequest request,
            @Parameter(hidden = true) Authentication authentication
                                         );
    
    @PostMapping(value = "/form-title/2", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Формирование и сохранение второго титула", description = "Формирование и сохранение второго титула")
    SecondTitleResponse generateSecondTitle(
            @RequestBody @Valid SecondTitleForm title,
            @Parameter(hidden = true) Authentication authentication
                                           );
    
    @PostMapping(value = "/form-title/telemech-out/{titleType}", produces = MediaType.APPLICATION_JSON_VALUE,
                 consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Формирование третьего и четвертого титулов", description = "Формирование третьего и четвертого титулов")
    TelemechOutTitleResponse generateTelemechOutTitles(
            @PathVariable EwbTitleType titleType,
            @RequestBody @Valid TelemechOutTitlesRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                      );
    
    @PostMapping(value = "/form-title/5", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Формирование пятого титула", description = "Формирование пятого титула")
    FifthTitleResponse generateFifthTitle(
            @RequestBody @Valid FifthTitleRequest request,
            @Parameter(hidden = true) Authentication authentication
                                         );
    
    @PostMapping(value = "/search/self-organization", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение ЭПЛ по параметрам (своя организация)", description = "Получение ЭПЛ по параметрам в рамках своей организации")
    Page<EwbSearchResponseDto> searchSelfOrganization(
            @RequestBody @Valid EwbSearchSelfOrganizationRequestDto request,
            @Parameter(hidden = true) Authentication authentication
                                                     );
    
    @PostMapping(value = "/search/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение ЭПЛ по параметрам (все организации)", description = "Получение ЭПЛ по параметрам в рамках всех организаций")
    Page<EwbSearchResponseDto> searchAllOrganizations(@RequestBody @Valid EwbSearchAllOrganizationsRequestDto request);
    
    @PostMapping(value = "search", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    Page<EwbSearchResponseDto> getAll(@RequestBody @Valid EwbSearchDto request);
    
    @GetMapping("{id}")
    @Operation(summary = "Получение карточки ЭПЛ", description = "Получение карточки ЭПЛ")
    GetEwbDto getEwb(@PathVariable UUID id);
    
    @GetMapping("request")
    @Operation(summary = "Получение заявки ЭПЛ", description = "Получение заявки ЭПЛ")
    GetEwbRequestDto getEwbRequest(@Parameter(hidden = true) Authentication authentication);
    
    @Operation(description = "Отправка подписанного файла")
    @PostMapping(value = "title/send", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendAndSaveFirstTitle(
            @RequestBody @Valid FirstTitleDto request,
            @Parameter(hidden = true) Authentication authentication
                                              );
    
    @Operation(description = "Отправка подписанного второго титула")
    @PostMapping(value = "title/send/2", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendAndSaveSecondTitle(
            @RequestBody @Valid SendAndSaveTitleRequest request,
            @Parameter(hidden = true) Authentication authentication
                                               );
    
    @Operation(description = "Отправка подписанного титула для выпуска на линию")
    @PostMapping(value = "/title/send/telemech-out/{titleType}",
                 produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    TelemechOutTitleSendResponse sendAndSaveTelemechOutTitle(
            @RequestBody @Valid SendAndSaveTitleRequest request,
            @PathVariable EwbTitleType titleType,
            @Parameter(hidden = true) Authentication authentication
                                                            );
    
    @Operation(description = "Отправка подписанного пятого титула")
    @PostMapping(value = "title/send/5", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> sendAndSaveFifthTitle(
            @RequestBody @Valid SendAndSaveTitleRequest request,
            @Parameter(hidden = true) Authentication authentication
                                              );
    
    @Operation(description = "Закрытие ЭПЛ")
    @PostMapping(value = "close", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> closeEwb(
            @RequestBody @Valid EwbCloseRequest request,
            @Parameter(hidden = true) Authentication authentication
                                 );
    
    
    @PostMapping(value = "odometer-out", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление показаний одометра", description = "Добавление показаний одометра")
    CheckResponse addOdometerValue(
            @RequestBody @Valid OdometerValue request,
            @Parameter(hidden = true) Authentication authentication
                                  );
    
    @GetMapping("{ewbId}/qr")
    @Operation(summary = "Получение QR-кода", description = "Получение QR-кода")
    GetQrCodeResponse getQrCode(
            @PathVariable UUID ewbId,
            @Parameter(hidden = true) Authentication authentication
                               );
    
    @Operation(summary = "Получение реестра путевых листов всех организаций",
               description = "Получение списка реестра путевых листов всех организаций")
    @PostMapping(value = "/report/all-organizations", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Valid
    Page<EwbRegistryResponse> getEwbRegistryForAllOrganizations(
            @RequestBody @Valid EwbRegistryAllOrganizationsRequest request
                                                               );
    
    @Operation(summary = "Получение реекстра путевых листов по организации",
               description = "Получение списка реестра путевых листов по организации")
    @PostMapping(value = "/report/self-organization", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Valid
    Page<EwbRegistryResponse> getEwbRegistrySelfOrganization(
            @RequestBody @Valid EwbRegistrySelfOrganizationRequest request,
            @Parameter(hidden = true) Authentication authentication
                                                            );
    
    @GetMapping(value = "detailed", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение детальной карточки ЭПЛ", description = "Получение детальной карточки ЭПЛ")
    GetEwbDetailedDto getEwbDetailed(@Parameter(hidden = true) Authentication authentication);
    
    @Operation(summary = "Отмена создаваемого ЭПЛ",
               description = "Отмена ЭПЛ в случае, если водитель не может выйти на линию")
    @PatchMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> cancelEwb(
            @PathVariable UUID id,
            @RequestBody @Valid EwbCancelRequestDto request,
            @Parameter(hidden = true) Authentication authentication
                                  );
    
    @Operation(summary = "Ввод остатка топлива")
    @PostMapping(value = "litreage-out", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    CheckResponse litreageOut(
            @RequestBody @Valid EwbLitreageOutRequest request,
            @Parameter(hidden = true) Authentication authentication
                             );
}
