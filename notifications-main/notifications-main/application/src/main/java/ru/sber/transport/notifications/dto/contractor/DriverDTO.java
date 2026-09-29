package ru.sber.transport.notifications.dto.contractor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Данные о водителе")
public class DriverDTO {

    @Schema(description = "Фамилия")
    private String lastName;

    @Schema(description = "Имя")
    private String firstName;

    @Schema(description = "Отчество")
    private String patronymic;

    @Schema(description = "Телефон")
    private String phoneNumber;

    @Schema(description = "Собранное ФИО")
    private String driverFio;

}
