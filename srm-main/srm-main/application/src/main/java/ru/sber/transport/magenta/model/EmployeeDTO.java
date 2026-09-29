package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Jacksonized
@Getter
@Schema(title = "Информация о сотруднике", description = "Данные сотрудника")
@SuperBuilder
public class EmployeeDTO {

    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Фамилия")
    private String lastName;
    
    @Schema(description = "Отчество")
    private String patronymic;
    
    @Schema(description = "Табельный номер")
    private String personnelNumber;
    
    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;
    
    @Schema(description = "Идентификатор пользователя")
    private UUID userId;
    
    @Schema(description = "Идентификатор должности")
    private UUID positionId;
    
    @Setter
    @Schema(description = "Название должности")
    private String positionName;
    
    @Schema(description = "Рук-ль")
    private UUID supervisorId;
    
    @Setter
    @Schema(description = "Организация")
    private UUID organizationId;
    
    @Schema(description = "Название подразделения")
    private String departmentName;
    
    @Schema(description = "Место возникновения затрат")
    private String mvz;
    
    @Schema(description = "Человекочитаемый идентификатор")
    private String humanReadableId;
    
    @Schema(description = "Номер телефона")
    private String mobilePhone;
    
}