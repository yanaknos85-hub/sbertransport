package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DispatcherStaffDto {

    @Schema(title = "Фамилия")
    private String lastName;

    @Schema(title = "Имя")
    private String firstName;

    @Schema(title = "Отчество")
    private String patronymic;

    @Schema(title = "Номер телефона")
    private String phone;

    @Schema(title = "E-Mail")
    private String email;

    @Schema(title = "Иденификатор пользователя в СберТранспорт")
    private UUID oauthId;

    @Schema(title = "Иденификатор филиала")
    private UUID autoparkId;

    @Schema(description = "Флаг возможности создания ЭПЛ")
    private boolean ewbCreationPossibility;

    @Schema(description = "Табельный номер")
    private String personnelNumber;

    @Schema(description = "Номер доверенности")
    private String attorneyNumber;

    @Schema(description = "Дата выдачи")
    private String issueDate;

    @Schema(description = "Дата окончания срока действия")
    private String expiryDate;

    @Schema(description = "Система создания")
    private String creationSystem;
}
