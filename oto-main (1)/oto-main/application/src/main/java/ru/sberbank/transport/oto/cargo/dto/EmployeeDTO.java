package ru.sberbank.transport.oto.cargo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Employee DTO
 */
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@JsonPropertyOrder({ "id" })
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
public class EmployeeDTO {
    
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;

    @Schema(description = "Идентификатор пользователя")
    private UUID userId;

    @Schema(description = "Табельный номер")
    private String personnelNumber;

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Фамилия")
    private String lastName;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "Тип разъездного характера сотрудника")
    private String itinerantType;

    @Schema(description = "Номер свидетельства о браке")
    private String marriageCertificateNumber;

    @Schema(description = "Место возникновения затрат, МВЗ")
    private String costCenter;

    @Deprecated
    @Schema(description = "Идентификатор должности. Если должность используется только для показа, идентификатор " +
                          "можно удалить", deprecated = true)
    private UUID positionId;

    @Schema(description = "Нзвание должности")
    private String positionName;

    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;
    
    @Schema(description = "Мобильный телефон")
    private String phone;

}
