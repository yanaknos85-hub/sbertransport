package ru.sber.transport.telemechanic.dto.telemedicine;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.With;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@With
@Schema(title = "Получение информации по телемедицине", description = "Получение информации по телемедицине")
public record GetTelemedicineDto(
        @Schema(description = "Идентификатор телемедицины")
        UUID id,
        @Schema(description = "Идентификатор ЭПЛ")
        UUID ewbId,
        @Schema(description = "Человекочитаемый идентификатор ЭПЛ")
        String ewbHumanReadableId,
        @Schema(description = "Уникальный идентификатор ГИС ЭПД")
        UUID ewbUuid,
        @Schema(description = "Дата начала ЭПЛ")
        LocalDate startDate,
        @Schema(description = "Номер телемедицины")
        String humanReadableId,
        @Schema(description = "Вид телемедицины", example = "1-Предрейсовый")
        String medCheckupType,
        @Schema(description = "Статус")
        TelemedicineStatus status,
        @Schema(description = "Данные медика")
        Medic medic,
        @Schema(description = "Данные водителя")
        Driver driver,
        @Schema(description = "Данные заявки")
        Request request
) {
    @With
    @Schema(name = "MedicTelemedicineCard", title = "Медик для карточки заявки телемедицины")
    public record Medic(
            @Schema(description = "Идентификатор медика")
            UUID id,
            @Schema(description = "Организация")
            String organizationName,
            @Schema(description = "Должность")
            String position,
            @Schema(description = "ФИО")
            String fullName,
            @Schema(description = "Серия")
            String series,
            @Schema(description = "Номер")
            String number,
            @Schema(description = "Дата выдачи лицензии")
            LocalDate issueDate,
            @Schema(description = "Дата окончания срока действия лицензии")
            LocalDate expiryDate,
            @Schema(description = "Дата и время медицинского осмотра")
            LocalDateTime decisionTime
    ) {
    }
    
    @With
    @Schema(name = "DriverTelemedicineCard", title = "Водитель для карточки заявки телемедицины")
    public record Driver(
            @Schema(description = "Идентификатор водителя")
            UUID id,
            @Schema(description = "ИНН водителя")
            String tin,
            @Schema(description = "Организация")
            String organizationName,
            @Schema(description = "ФИО водителя")
            String fullName,
            @Schema(description = "Идентификатор водительских прав")
            UUID drivingLicenseId,
            @Schema(description = "Серия")
            String series,
            @Schema(description = "Номер")
            String number,
            @Schema(description = "Дата выдачи")
            LocalDate issueDate
    ) {
    }
    
    @With
    @Schema(name = "TelemedicineRequest")
    public record Request(
            @Positive
            @Min(40)
            @Max(300)
            @Schema(description = "Систолическое артериальное давление (мм рт. ст)", example = "120")
            Integer systPressure,
            
            @Positive
            @Min(40)
            @Max(300)
            @Schema(description = "Диастолическое артериальное давление (мм рт. ст)", example = "80")
            Integer dyastPressure,
            
            @Min(0)
            @Max(300)
            @Positive
            @Schema(description = "Пульс (уд./мин)", example = "80")
            Integer pulse,
            
            @Min(30)
            @Max(47)
            @Positive
            @Schema(description = "Температура (°С)", example = "36.6")
            BigDecimal temperature,
            
            @Min(0)
            @Max(1)
            @Schema(description = "Алкоголь в крови (Промилле)", example = "0.003")
            BigDecimal bloodAlcohol,
            
            @Size(min = 1, max = 255)
            @Schema(description = "Комментарий", example = "Пройден")
            String comment
    ) {}
}
