package ru.sberbank.ditsib.transport.reports.dto;

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
    
    @Schema(description = "Идентификатор должности. Если должность используется только для показа, идентификатор " +
                          "можно удалить", deprecated = true)
    private UUID positionId;
    
    @Schema(description = "Название должности")
    private String positionName;
    
    @Schema(description = "Идентификатор организации")
    private UUID organizationId;
    
    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;
    
    @Schema(description = "Мобильный телефон")
    private String phone;
    
    public EmployeeDTO() {
    
    }
    
    public EmployeeDTO(
            UUID id, String humanReadableId, UUID userId, String personnelNumber, String firstName, String lastName, String patronymic,
            String itinerantType, String marriageCertificateNumber, UUID positionId, String positionName,
            UUID organizationId, UUID departmentId, String phone
                      ) {
        this.id = id;
        this.humanReadableId = humanReadableId;
        this.userId = userId;
        this.personnelNumber = personnelNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.itinerantType = itinerantType;
        this.marriageCertificateNumber = marriageCertificateNumber;
        this.positionId = positionId;
        this.positionName = positionName;
        this.organizationId = organizationId;
        this.departmentId = departmentId;
        this.phone = phone;
    }
    
    public String getFIO() {
        return getLastName() + " " + getFirstName() + (getPatronymic() == null ? "" : (" " + getPatronymic()));
    }
}
