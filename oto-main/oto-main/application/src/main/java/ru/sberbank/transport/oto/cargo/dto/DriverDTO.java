package ru.sberbank.transport.oto.cargo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.DriverLicenseClass;
import ru.sberbank.ditsib.transport.constants.DrivingExperience;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO - Информация о новом водителе
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Информация о водителе", description = "Информация о водителе")
public class DriverDTO {
    

    /** Имя */
    @Schema(description = "Имя")
    private String firstName;

    /** Фамилия */
    @Schema(description = "Фамилия")
    private String lastName;

    /** Отчество */
    @Schema(description = "Отчество")
    private String patronymic;

    /** Телефонный номер */
    @Schema(description = "Контактный номер телефона")
    private String contactPhone;

    /** Рейтинг */
    @Schema(description = "Рейтинг водителя 0-500")
    private Integer rating;

}
