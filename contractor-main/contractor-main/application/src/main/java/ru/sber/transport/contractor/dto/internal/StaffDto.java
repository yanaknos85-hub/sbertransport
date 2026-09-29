package ru.sber.transport.contractor.dto.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StaffDto {

    @JsonAlias(value = "id")
    @Schema(description = "Идентификатор сотрудника во внешней системе")
    private UUID externalId;

    @Schema(description = "Имя сотрудника")
    private String firstName;

    @Schema(description = "Фамилия сотрудника")
    private String lastName;

    @Schema(description = "Отчество сотрудника")
    private String patronymic;

    @Schema(description = "Электронная почта сотрудника")
    private String email;

    @JsonAlias(value = {"phone", "contactPhone"})
    @Schema(description = "Телефон сотрудника")
    private String phone;

    @JsonAlias(value = "autoparkId")
    @Schema(description = "Идентификатор филиала")
    private UUID branchId;

    @JsonAlias(value = "autoparkName")
    @Schema(description = "Наименование филиала")
    private String branchName;

    @Schema(description = "Флаг возможности создания ЭПЛ")
    private boolean ewbCreationPossibility;

    @Schema(description = "Табельный номер")
    private String personnelNumber;

    @Schema(description = "Номер доверенности")
    private String attorneyNumber;

    @Schema(description = "Дата выдачи")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate issueDate;

    @Schema(description = "Дата окончания срока действия")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate expiryDate;

    @Schema(description = "Система создания")
    private String creationSystem;

    @Schema(description = "СНИЛС")
    private String snils;

    @Schema(description = "ИНН")
    private String tin;

    @Schema(description = "Номер водительского удостоверения")
    private String driverLicenseNumber;

    @Schema(description = "Идентификаторы категорий")
    private Set<String> driverLicenses;

    @Schema(description = "Специальность водителя")
    private String driverSpeciality;

    @Schema(title = "Идентификатор пользователя в СберТранспорт")
    private UUID oauthId;
}
