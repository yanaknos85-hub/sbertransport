package ru.sber.transport.telemechanic.dto.ewb_report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

@Schema(name = "EwbRegistryDto", title = "Данные для таблицы реестра ЭПЛ", description = "Ответ на запрос получения данных для таблицы реестра ЭПЛ")
public record EwbRegistryResponse(
        @Valid
        @Schema(description = "Информация о путевом листе")
        EwbRegistryInfo ewb,
        
        @Valid
        @Schema(description = "Информация об авторе")
        AuthorRegistryInfo author,
        
        @Valid
        @Schema(description = "Информация о МЧД")
        AttorneyRegistryInfo attorney,
        
        @Valid
        @Schema(description = "Информация о водителе")
        DriverRegistryInfo driver,
        
        @Valid
        @Schema(description = "Информация о водительском удостоверении")
        DrivingLicenseRegistryInfo drivingLicense,
        
        @Valid
        @Schema(description = "Информация о транспортном средстве")
        TransportRegistryInfo transport,
        
        @Valid
        @Schema(description = "Информация о медике")
        MedicRegistryInfo medic,
        
        @Valid
        @Schema(description = "Информация о заявке медецинского осмотра")
        MedicRequestRegistryInfo medicRequest,
        
        @Valid
        @Schema(description = "Информация о телемеханике на выезд")
        TelemechOutRegistryInfo telemechOut,
        
        @Valid
        @Schema(description = "Информация о заявке телемеханика")
        RequestRegistryInfo request,
        
        @Valid
        @Schema(description = "Информация о телемеханике на въезд")
        TelemechInRegistryInfo telemechIn
) {}
