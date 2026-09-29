package ru.sber.transport.telemechanic.dto.ewb;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.With;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@With
@Schema(name = "GetEwbRequestDto", title = "Запрос на получение активной заявки по ЭПЛ")
public record GetEwbRequestDto(
        @Schema(description = "Идентификатор ЭПЛ")
        UUID id,
        @Schema(description = "Статус ЭПЛ")
        EwbStatus status,
        @Schema(description = "Идентификатор заявки медицинского осмотра")
        UUID medicRequestId,
        @Schema(description = "Статус медицинского осмотра")
        TelemedicineStatus medicStatus,
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ")
        String humanReadableId,
        @Schema(description = "Дата и время создания заявки ЭПЛ")
        LocalDateTime creationTime,
        @Schema(description = "Возможность отправки телемеху")
        boolean callTelemech,
        @Schema(description = "Автор заявки телемеханика")
        Author author,
        @Schema(description = "Транспортное средство")
        Vehicle transport,
        @Schema(description = "Информация по заявке телемеханика")
        Telemechanic telemechanic
) {
    @Schema(name = "GetEwbReqeuestDto.Author", title = "Автор заявки телемеханика")
    public record Author(
            @Schema(description = "Идентификатор автора")
            UUID id,
            @Schema(description = "Имя")
            String firstName,
            @Schema(description = "Фамилия")
            String lastName,
            @Schema(description = "Отчество")
            String patronymic,
            @Schema(description = "Табельный номер")
            String personnelNumber
    ) {
    }
    
    @Schema(name = "GetEwbRequestDto.Vehicle", title = "Транспортное средство")
    public record Vehicle(
            @Schema(description = "Идентификатор транспортного средства")
            UUID id,
            @Schema(description = "Государственный номер")
            String stateNumber,
            @Schema(description = "Марка")
            String brand,
            @Schema(description = "Модель")
            String model,
            @Schema(description = "Показания одометра")
            int mileage,
            @Schema(description = "Текущий остаток топлива (литров)")
            int fuelLitreage,
            @Schema(description = "Объем топливного бака")
            int fuelTankVolume
    ) {
    }
    
    @With
    @Schema(name = "GetEwbRequestDto.Telemechanic", title = "Информация по заявке телемеханика")
    public record Telemechanic(
            @Schema(description = "Идентификатор заявки телемеханика")
            UUID requestId,
            @Schema(description = "Статус телемеханика")
            RequestStatus status,
            @Schema(description = "Показания одометра на выезде")
            int odometerOut,
            @Schema(description = "Проверки заявки")
            ChecksTreeDto.ChecksTree checks
    ) {
    }
}
