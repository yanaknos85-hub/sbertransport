package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.DriverLicenseClass;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Категория прав", description = "Новая категория прав водителя")
public class NewDriverLicenseDTO {

    @NotNull
    @Schema(description = "Категория прав")
    private DriverLicenseClass licenseClass;
}
