package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.contractor.dto.enums.StaffSpeciality;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverStaffDto {

    @Schema(title = "Фамилия")
    private String lastName;

    @Schema(title = "Имя")
    private String firstName;

    @Schema(title = "Отчество")
    private String patronymic;

    @Schema(title = "Номер телефона")
    private String contactPhone;

    @Schema(title = "E-Mail")
    private String email;

    @Schema(title = "Иденификатор пользователя в СберТранспорт")
    private UUID oauthId;

    @Schema(title = "Специализация водителя")
    private StaffSpeciality.DriverStaffSpeciality driverSpeciality;

    @Schema(title = "Признак активности")
    private boolean active;

    @Schema(title = "Иденификатор филиала")
    private UUID autoparkId;

    @Schema(title = "Табельный номер")
    private String personnelNumber;

    @Schema(title = "СНИЛС")
    private String snils;

    @Schema(title = "ИНН")
    private String tin;

    @Schema(title = "Дата выдачи")
    private String issueDate;

    @Schema(title = "Дата окончания срока действия")
    private String expiryDate;

    @Schema(title = "Номер водительского удостоверения")
    private String driverLicenseNumber;

    @Schema(description = "Идентификаторы категорий")
    private Set<String> driverLicenses;
}
