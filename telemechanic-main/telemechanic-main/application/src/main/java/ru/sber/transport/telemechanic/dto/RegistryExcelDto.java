package ru.sber.transport.telemechanic.dto;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "Реестр - Телемеханик excel", description = "Реестр - Телемеханик excel")
public class RegistryExcelDto {

    /**
     * ID заявки
     */
    @Schema(description = "ID заявки")
    @NotBlank
    @Size(max = 100)
    private String humanReadableId;
    /**
     * Организация
     */
    @Schema(description = "Организация")
    @NotBlank
    private String officialName;
    /**
     * Дата и время создания заявки
     */
    @Schema(description = "Дата и время создания заявки")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime creationTime;
    
    /**
     * Дата и время начала прохождения проверок
     */
    @Schema(description = "Дата и время начала прохождения проверок")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime checksStartedTime;
    
    /**
     * Дата и время завершения прохождения проверок
     */
    @Schema(description = "Дата и время завершения прохождения проверок")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime checksFinishedTime;
    /**
     * Дата и время выполнения заявки
     */
    @Schema(description = "Дата и время проведения контроля", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime inspectionTime;
    /**
     * Статус заявки
     */
    @Schema(description = "Отметка о прохождении контроля", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inspectionMark;
    /**
     * Государственный номер
     */
    @NotBlank(message = "Номер должен быть задан")
    @Pattern(regexp = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
            message = "Регистрационный знак не прошел проверку")
    @Schema(description = "Государственный номер",
            pattern = "^[АВЕКМНОРСТУХ]\\d{3}(?<!000)[АВЕКМНОРСТУХ]{2}\\d{2,3}$",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String stateNumber;
    /**
     * Марка
     */
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Марка",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Lada",
            maximum = "255")
    private String brand;
    /**
     * Модель
     */
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Модель",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "Kalina",
            maximum = "255")
    private String model;
    /**
     * Табельный номер водителя
     */
    @NotBlank(message = "Табельный номер водителя должен быть задан")
    @Schema(description = "Табельный номер водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private String personnelNumber;
    /**
     * ФИО водителя
     */
    @NotBlank(message = "ФИО водителя должно быть задано")
    @Schema(description = "ФИО водителя", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;
    /**
     * Табельный номер сотрудника, проводившего контроль
     */
    @NotBlank(message = "Табельный номер сотрудника, проводившего контроль, должен быть задан")
    @Schema(description = "Табельный номер сотрудника, проводившего контроль", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inspectorPersonnelNumber;
    /**
     * ФИО сотрудника, проводившего контроль
     */
    @NotBlank(message = "ФИО сотрудника, проводившего контроль, должно быть задано")
    @Schema(description = "ФИО сотрудника, проводившего контроль", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inspectorFullName;
    /**
     * Комментарий
     */
    @Schema(description = "Комментарий", maxLength = 255, requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(max = 255)
    private String comment;

    /**
     * ИД департамента для вычисленя цепочки департаментов и организации
     */
    @Hidden
    private UUID departmentId;

    /**
     * Подразделение
     */
    @Schema(description = "Подразделение", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgStructureChain;
}
